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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.musiel.args.Option;
import org.musiel.args.ParserException;
import org.musiel.args.syntax.Syntax.ParseResult;

public abstract class AbstractPosixSyntaxTest {

	@ Rule
	public final ExpectedException exceptions = ExpectedException.none();

	protected PosixSyntax syntax;

	@ Before
	public abstract void setup();

	protected Option option( final String firstName, final String... additionalNames) {
		return this.option( false, true, false, false, firstName, additionalNames);
	}

	protected Option option( final boolean required, final boolean repeatable, final boolean argumentAccepted,
			final boolean argumentRequired, final String firstName, final String... additionalNames) {
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
			public boolean isArgumentAccepted() {
				return argumentAccepted;
			}

			@ Override
			public boolean isArgumentRequired() {
				return argumentRequired;
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
		this.testInvalidOption( this.option( false, true, true, false, "-a", "-3"));
		this.syntax.validate( this.option( false, true, true, true, "-a", "-3"));
	}

	@ Test
	public void testOptionalArgumentAllowed() {
		this.syntax.setOptionalArgumentsAllowed( true);
		this.syntax.validate( this.option( false, true, true, false, "-a", "-3"));
		this.syntax.validate( this.option( false, true, true, true, "-a", "-3"));
	}

	protected final Option optionA = this.option( "-a");
	protected final Option optionB = this.option( "-b");
	protected final Option optionO = this.option( false, true, true, true, "-o");
	protected final Set< Option> options = new HashSet<>();
	{
		this.options.add( this.optionA);
		this.options.add( this.optionB);
		this.options.add( this.optionO);
	}

	protected void testExceptionalParse( final Class< ? extends Throwable> exceptionType, final String message,
			final Set< Option> options, final String... args) throws ParserException {
		this.exceptions.expect( exceptionType);
		this.exceptions.expectMessage( message);
		this.syntax.parse( options, args);
	}

	@ Test
	public void testParsingUnsupportedOption() throws ParserException {
		final Set< Option> options = new HashSet<>( this.options);
		this.syntax.parse( options);
		options.add( this.option( "-?"));
		this.testExceptionalParse( IllegalArgumentException.class, "not a valid", options);
	}

	@ Test
	public void testParsingDuplicateNames() throws ParserException {
		final Set< Option> options = new HashSet<>( this.options);
		this.syntax.parse( options);
		options.add( this.option( "-x", "-a"));
		this.testExceptionalParse( IllegalArgumentException.class, "duplicate option name", options);
	}

	@ Test
	public void testMissingArgument() throws ParserException {
		this.testExceptionalParse( ArgumentRequiredException.class, "requires an argument", this.options, "-o");
	}

	@ Test
	public void testMissingArgument2() throws ParserException {
		this.testExceptionalParse( ArgumentRequiredException.class, "requires an argument", this.options, "-oa");
	}

	@ Test
	public void testUnknownOption() throws ParserException {
		this.testExceptionalParse( UnknownOptionException.class, "unknown option", this.options, "-x");
	}

	@ Test
	public void testParse() throws ParserException {
		final ParseResult result = this.syntax.parse( this.options, "-a", "-o", "file1", "-o", "-", "-", "xyz", "--", "-a", "-a");
		Assert.assertFalse( result.getOptionNames( this.optionA).isEmpty());
		Assert.assertTrue( result.getOptionNames( this.optionB).isEmpty());
		Assert.assertFalse( result.getOptionNames( this.optionO).isEmpty());
		Assert.assertFalse( result.getOptionArguments( this.optionA).isEmpty());
		Assert.assertTrue( result.getOptionArguments( this.optionB).isEmpty());
		Assert.assertFalse( result.getOptionArguments( this.optionO).isEmpty());
		Assert.assertEquals( 1, result.getOptionNames( this.optionA).size());
		Assert.assertEquals( 0, result.getOptionNames( this.optionB).size());
		Assert.assertEquals( 2, result.getOptionNames( this.optionO).size());
		Assert.assertEquals( 1, result.getOptionArguments( this.optionA).size());
		Assert.assertEquals( 0, result.getOptionArguments( this.optionB).size());
		Assert.assertEquals( 2, result.getOptionArguments( this.optionO).size());
		Assert.assertArrayEquals( new String[]{ "file1", "-"}, result.getOptionArguments( this.optionO).toArray());
		Assert.assertArrayEquals( new String[]{ "-", "xyz", "-a", "-a"}, result.getOperands().toArray());
	}

	@ Test
	public void testParseJoint() throws ParserException {
		this.syntax.setJointArgumentsAllowed( true);
		final ParseResult result = this.syntax.parse( this.options, "-a", "-o", "file1", "-ooutput2", "-", "xyz", "--", "-a", "-a");
		Assert.assertFalse( result.getOptionNames( this.optionA).isEmpty());
		Assert.assertTrue( result.getOptionNames( this.optionB).isEmpty());
		Assert.assertFalse( result.getOptionNames( this.optionO).isEmpty());
		Assert.assertFalse( result.getOptionArguments( this.optionA).isEmpty());
		Assert.assertTrue( result.getOptionArguments( this.optionB).isEmpty());
		Assert.assertFalse( result.getOptionArguments( this.optionO).isEmpty());
		Assert.assertEquals( 1, result.getOptionNames( this.optionA).size());
		Assert.assertEquals( 0, result.getOptionNames( this.optionB).size());
		Assert.assertEquals( 2, result.getOptionNames( this.optionO).size());
		Assert.assertEquals( 1, result.getOptionArguments( this.optionA).size());
		Assert.assertEquals( 0, result.getOptionArguments( this.optionB).size());
		Assert.assertEquals( 2, result.getOptionArguments( this.optionO).size());
		Assert.assertArrayEquals( new String[]{ "file1", "output2"}, result.getOptionArguments( this.optionO).toArray());
		Assert.assertArrayEquals( new String[]{ "-", "xyz", "-a", "-a"}, result.getOperands().toArray());
	}

	@ Test
	public void parseOptional() throws ParserException {
		final Set< Option> options = new HashSet<>( this.options);
		final Option optionP = this.option( false, true, true, false, "-p");
		options.add( optionP);
		this.syntax.setOptionalArgumentsAllowed( true);
		this.syntax.setLateOptionsAllowed( true);
		final ParseResult result =
				this.syntax.parse( options, "-a", "-abpp1", "-o", "file1", "-p", "-", "xyz", "-pprofile1", "-o-", "--", "-a", "-a");
		Assert.assertFalse( result.getOptionNames( this.optionA).isEmpty());
		Assert.assertFalse( result.getOptionNames( this.optionB).isEmpty());
		Assert.assertFalse( result.getOptionNames( this.optionO).isEmpty());
		Assert.assertFalse( result.getOptionNames( optionP).isEmpty());
		Assert.assertFalse( result.getOptionArguments( this.optionA).isEmpty());
		Assert.assertFalse( result.getOptionArguments( this.optionB).isEmpty());
		Assert.assertFalse( result.getOptionArguments( this.optionO).isEmpty());
		Assert.assertFalse( result.getOptionArguments( optionP).isEmpty());
		Assert.assertEquals( 2, result.getOptionNames( this.optionA).size());
		Assert.assertEquals( 1, result.getOptionNames( this.optionB).size());
		Assert.assertEquals( 2, result.getOptionNames( this.optionO).size());
		Assert.assertEquals( 3, result.getOptionNames( optionP).size());
		Assert.assertEquals( 2, result.getOptionArguments( this.optionA).size());
		Assert.assertEquals( 1, result.getOptionArguments( this.optionB).size());
		Assert.assertEquals( 2, result.getOptionArguments( this.optionO).size());
		Assert.assertEquals( 3, result.getOptionArguments( optionP).size());
		Assert.assertArrayEquals( new String[]{ "file1", "-"}, result.getOptionArguments( this.optionO).toArray());
		Assert.assertArrayEquals( new String[]{ "p1", null, "profile1"}, result.getOptionArguments( optionP).toArray());
		Assert.assertArrayEquals( new String[]{ "-", "xyz", "-a", "-a"}, result.getOperands().toArray());
	}
}
