/*
 * Copyright 2014 Bagana <bagana@musiel.org>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You 
 * may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing 
 * permissions and limitations under the License.
 */
package org.musiel.args.syntax;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.musiel.args.ArgumentPolicy;
import org.musiel.args.Option;
import org.musiel.args.syntax.Syntax.SyntaxResult;
import org.musiel.args.syntax.SyntaxException.Reason;

public abstract class AbstractPosixSyntaxTest {

	@ Rule
	public final ExpectedException exceptions = ExpectedException.none();

	protected PosixSyntax syntax;

	@ Before
	public abstract void setup();

	protected Option option( final String firstName, final String... additionalNames) {
		return this.option( false, true, ArgumentPolicy.NONE, firstName, additionalNames);
	}

	protected Option option( final boolean required, final boolean repeatable, final ArgumentPolicy argumentPolicy,
			final String firstName, final String... additionalNames) {
		final Set< String> nameSet = new HashSet<>();
		nameSet.add( firstName);
		Collections.addAll( nameSet, additionalNames);
		return new Option() {

			@ Override
			public boolean isRequired() {
				return required;
			}

			@ Override
			public boolean isRepeatable() {
				return repeatable;
			}

			@ Override
			public ArgumentPolicy getArgumentPolicy() {
				return argumentPolicy;
			}

			@ Override
			public Set< String> getNames() {
				return nameSet;
			}

			@ Override
			public String getName() {
				return firstName;
			}
		};
	}

	@ Test
	public void testOptionalArgumentsAllowed() {
		this.syntax.setOptionalArgumentsAllowed( true);
		Assert.assertTrue( this.syntax.isOptionalArgumentsAllowed());
		this.syntax.setOptionalArgumentsAllowed( false);
		Assert.assertFalse( this.syntax.isOptionalArgumentsAllowed());
	}

	@ Test
	public void testJointArgumentsAllowed() {
		this.syntax.setJointArgumentsAllowed( true);
		Assert.assertTrue( this.syntax.isJointArgumentsAllowed());
		this.syntax.setJointArgumentsAllowed( false);
		Assert.assertFalse( this.syntax.isJointArgumentsAllowed());
	}

	@ Test
	public void testLateOptionsAllowed() {
		this.syntax.setLateOptionsAllowed( true);
		Assert.assertTrue( this.syntax.isLateOptionsAllowed());
		this.syntax.setLateOptionsAllowed( false);
		Assert.assertFalse( this.syntax.isLateOptionsAllowed());
	}

	@ Test
	public void testConfigurationAutoChange() {
		this.syntax.setOptionalArgumentsAllowed( true);
		Assert.assertTrue( this.syntax.isOptionalArgumentsAllowed());
		Assert.assertTrue( this.syntax.isJointArgumentsAllowed());
		this.syntax.setJointArgumentsAllowed( false);
		Assert.assertFalse( this.syntax.isOptionalArgumentsAllowed());
		Assert.assertFalse( this.syntax.isJointArgumentsAllowed());
	}

	protected void testInvalidOption( final Option option) {
		try {
			this.syntax.validate( option);
			Assert.fail();
		} catch( final IllegalArgumentException exception) {
		}
	}

	@ Test
	public void testValidNames() {
		this.syntax.validate( this.option( "-a", "-3", "-A"));
	}

	@ Test
	public void testInvalidNames() {
		for( final String invalidName: new String[]{ "-?", "--", "t", "-ab"})
			this.testInvalidOption( this.option( invalidName));
	}

	@ Test
	public void testOptionalArgumentDisallowed() {
		this.testInvalidOption( this.option( false, true, ArgumentPolicy.OPTIONAL, "-a", "-3"));
		this.syntax.validate( this.option( false, true, ArgumentPolicy.REQUIRED, "-a", "-3"));
	}

	@ Test
	public void testOptionalArgumentAllowed() {
		this.syntax.setOptionalArgumentsAllowed( true);
		this.syntax.validate( this.option( false, true, ArgumentPolicy.OPTIONAL, "-a", "-3"));
		this.syntax.validate( this.option( false, true, ArgumentPolicy.REQUIRED, "-a", "-3"));
	}

	protected final Option optionA = this.option( "-a");
	protected final Option optionB = this.option( "-b");
	protected final Option optionO = this.option( false, true, ArgumentPolicy.REQUIRED, "-o");
	protected final Set< Option> options = new HashSet<>();
	{
		this.options.add( this.optionA);
		this.options.add( this.optionB);
		this.options.add( this.optionO);
	}

	protected void verifyException( final Collection< ? extends SyntaxException> errors, final String message) {
		for( final SyntaxException exception: errors)
			if( exception.getMessage().contains( message))
				return;
		Assert.fail();
	}

	protected void verifyException( final Collection< ? extends SyntaxException> errors, final Reason reason) {
		for( final SyntaxException exception: errors)
			if( reason.equals( exception.getReason()))
				return;
		Assert.fail();
	}

	@ Test
	public void testParsingUnsupportedOption() {
		final Set< Option> options = new HashSet<>( this.options);
		Assert.assertTrue( this.syntax.parse( options).getErrors().isEmpty());
		options.add( this.option( "-?"));
		try {
			this.syntax.parse( options);
			Assert.fail();
		} catch( final IllegalArgumentException exception) {
			Assert.assertTrue( exception.getMessage().contains( "not a valid"));
		}
	}

	@ Test
	public void testParsingDuplicateNames() {
		final Set< Option> options = new HashSet<>( this.options);
		Assert.assertTrue( this.syntax.parse( options).getErrors().isEmpty());
		options.add( this.option( "-x", "-a"));
		try {
			this.syntax.parse( options);
			Assert.fail();
		} catch( final IllegalArgumentException exception) {
			Assert.assertTrue( exception.getMessage().contains( "duplicate name"));
		}
	}

	@ Test
	public void testMissingArgument() {
		this.verifyException( this.syntax.parse( this.options, "-o").getErrors(), Reason.ARGUMENT_REQUIRED);
	}

	@ Test
	public void testMissingArgument2() {
		this.verifyException( this.syntax.parse( this.options, "-oa").getErrors(), Reason.ARGUMENT_REQUIRED);
	}

	@ Test
	public void testUnknownOption() {
		this.verifyException( this.syntax.parse( this.options, "-x").getErrors(), Reason.UNKNOWN_OPTION);
	}

	@ Test
	public void testParse() {
		final SyntaxResult result = this.syntax.parse( this.options, "-a", "-o", "file1", "-o", "-", "-", "xyz", "--", "-a", "-a");
		Assert.assertTrue( result.getErrors().isEmpty());
		Assert.assertFalse( result.getNames( "-a").isEmpty());
		Assert.assertTrue( result.getNames( "-b").isEmpty());
		Assert.assertFalse( result.getNames( "-o").isEmpty());
		Assert.assertEquals( 1, result.getNames( "-a").size());
		Assert.assertEquals( 0, result.getNames( "-b").size());
		Assert.assertEquals( 2, result.getNames( "-o").size());
		Assert.assertArrayEquals( new String[]{ "file1", "-"}, result.getArguments( "-o").toArray());
		Assert.assertArrayEquals( new String[]{ "-", "xyz", "-a", "-a"}, result.getOperands().toArray());
	}

	@ Test
	public void testParseJoint() {
		this.syntax.setJointArgumentsAllowed( true);
		final SyntaxResult result = this.syntax.parse( this.options, "-a", "-o", "file1", "-ooutput2", "-", "xyz", "--", "-a", "-a");
		Assert.assertTrue( result.getErrors().isEmpty());
		Assert.assertFalse( result.getNames( "-a").isEmpty());
		Assert.assertTrue( result.getNames( "-b").isEmpty());
		Assert.assertFalse( result.getNames( "-o").isEmpty());
		Assert.assertEquals( 1, result.getNames( "-a").size());
		Assert.assertEquals( 0, result.getNames( "-b").size());
		Assert.assertEquals( 2, result.getNames( "-o").size());
		Assert.assertArrayEquals( new String[]{ "file1", "output2"}, result.getArguments( "-o").toArray());
		Assert.assertArrayEquals( new String[]{ "-", "xyz", "-a", "-a"}, result.getOperands().toArray());
	}

	@ Test
	public void parseOptional() {
		final Set< Option> options = new HashSet<>( this.options);
		options.add( this.option( false, true, ArgumentPolicy.OPTIONAL, "-p"));
		this.syntax.setOptionalArgumentsAllowed( true);
		this.syntax.setLateOptionsAllowed( true);
		final SyntaxResult result =
				this.syntax.parse( options, "-a", "-abpp1", "-o", "file1", "-p", "-", "xyz", "-pprofile1", "-o-", "--", "-a", "-a");
		Assert.assertTrue( result.getErrors().isEmpty());
		Assert.assertFalse( result.getNames( "-a").isEmpty());
		Assert.assertFalse( result.getNames( "-b").isEmpty());
		Assert.assertFalse( result.getNames( "-o").isEmpty());
		Assert.assertFalse( result.getNames( "-p").isEmpty());
		Assert.assertEquals( 2, result.getNames( "-a").size());
		Assert.assertEquals( 1, result.getNames( "-b").size());
		Assert.assertEquals( 2, result.getNames( "-o").size());
		Assert.assertEquals( 3, result.getNames( "-p").size());
		Assert.assertArrayEquals( new String[]{ "file1", "-"}, result.getArguments( "-o").toArray());
		Assert.assertArrayEquals( new String[]{ "p1", null, "profile1"}, result.getArguments( "-p").toArray());
		Assert.assertArrayEquals( new String[]{ "-", "xyz", "-a", "-a"}, result.getOperands().toArray());
	}
}
