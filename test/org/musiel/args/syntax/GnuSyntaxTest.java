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

import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.musiel.args.ArgumentPolicy;
import org.musiel.args.Option;
import org.musiel.args.syntax.Syntax.SyntaxResult;

public class GnuSyntaxTest extends AbstractPosixSyntaxTest {

	@ Override
	public void setup() {
		this.syntax = new GnuSyntax();
	}

	@ Test
	public void testLongNames() {
		this.syntax.validate( this.option( "--all", "--3", "--A", "--ignore-pattern"));
	}

	@ Test
	public void testIllegalNameWithLegalNames() {
		this.testInvalidOption( this.option( "-a", "-3", "--!"));
	}

	@ Test
	public void testAbbreviationAllowed() {
		final GnuSyntax syntax = new GnuSyntax();
		syntax.setAbbreviationAllowed( false);
		Assert.assertFalse( syntax.isAbbreviationAllowed());
		syntax.setAbbreviationAllowed( true);
		Assert.assertTrue( syntax.isAbbreviationAllowed());
	}

	@ Test
	public void testDefaultLateOptionAllowed() {
		Assert.assertTrue( this.syntax.parse( this.options, "-a", "file1", "-a").getErrors().isEmpty());
	}

	@ Test
	public void testLateOptionDisabled() {
		this.syntax.setLateOptionsAllowed( false);
		this.verifyException( this.syntax.parse( this.options, "-a", "file1", "-a").getErrors(), "options must precede operands: -a");
	}

	@ Test
	public void testUnknownLongName() {
		final Set< Option> options = new HashSet<>( this.options);
		options.add( this.option( false, true, ArgumentPolicy.REQUIRED, "--ignore", "-I"));
		options.add( this.option( false, true, ArgumentPolicy.REQUIRED, "--ignore-file", "-F"));
		Assert.assertTrue( this.syntax.parse( options, "--ignore", "ignored", "--ignore-file=ignored-file").getErrors().isEmpty());
		this.verifyException( this.syntax.parse( options, "--ignored", "ignored", "--ignor=ignored-file").getErrors(),
				"unknown option: --ignored");
	}

	@ Test
	public void testUnknownLongName2() {
		final Set< Option> options = new HashSet<>( this.options);
		options.add( this.option( false, true, ArgumentPolicy.REQUIRED, "--ignore", "-I"));
		options.add( this.option( false, true, ArgumentPolicy.REQUIRED, "--ignore-file", "-F"));
		Assert.assertTrue( this.syntax.parse( options, "--ignore", "ignored", "--ignore-file=ignored-file").getErrors().isEmpty());
		this.verifyException( this.syntax.parse( options, "--ignore", "ignored", "--ignor?=ignored-file").getErrors(),
				"unknown option: --ignor?");
	}

	@ Test
	public void testAbbreviationDisabled() {
		final Set< Option> options = new HashSet<>( this.options);
		options.add( this.option( false, true, ArgumentPolicy.REQUIRED, "--ignore", "-I"));
		Assert.assertTrue( this.syntax.parse( options, "--ign", "ignored").getErrors().isEmpty());
		( ( GnuSyntax) this.syntax).setAbbreviationAllowed( false);
		this.verifyException( this.syntax.parse( options, "--ign", "ignored").getErrors(), "unknown option: --ign");
	}

	@ Test
	public void testAmbiguous() {
		final Set< Option> options = new HashSet<>( this.options);
		options.add( this.option( false, true, ArgumentPolicy.REQUIRED, "--ignore", "-I"));
		options.add( this.option( false, true, ArgumentPolicy.REQUIRED, "--ignore-file", "-F"));

		SyntaxResult result = this.syntax.parse( options, "--ignore", "ignored", "--ignore-file=ignored-file");
		Assert.assertTrue( result.getErrors().isEmpty());
		Assert.assertArrayEquals( new String[]{ "ignored"}, result.getArguments( "-I").toArray());
		Assert.assertArrayEquals( new String[]{ "ignored-file"}, result.getArguments( "-F").toArray());

		result = this.syntax.parse( options, "--ignore", "ignored", "--ignore-=ignored-file");
		Assert.assertTrue( result.getErrors().isEmpty());
		Assert.assertArrayEquals( new String[]{ "ignored"}, result.getArguments( "-I").toArray());
		Assert.assertArrayEquals( new String[]{ "ignored-file"}, result.getArguments( "-F").toArray());
		Assert.assertArrayEquals( new String[]{ "--ignore-file"}, result.getNames( "-F").toArray());

		this.verifyException( this.syntax.parse( options, "--ignore", "ignored", "--ignor=ignored-file").getErrors(),
				"ambiguous option name: --ignor");
	}

	@ Test
	public void testParseOptionalGnu() {
		final Set< Option> options = new HashSet<>( this.options);
		options.add( this.option( false, true, ArgumentPolicy.OPTIONAL, "--profile", "-p"));
		options.add( this.option( false, true, ArgumentPolicy.REQUIRED, "--ignore", "-I"));
		this.syntax.setOptionalArgumentsAllowed( true);
		final SyntaxResult result =
				this.syntax.parse( options, "-a", "-abpp1", "-o", "file1", "-p", "--profile", "--ignore", "ignored", "--ign=ignored2", "-",
						"xyz", "--profile=profile1", "-o-", "--", "-a", "-a");
		Assert.assertTrue( result.getErrors().isEmpty());
		Assert.assertFalse( result.getNames( "-a").isEmpty());
		Assert.assertFalse( result.getNames( "-b").isEmpty());
		Assert.assertFalse( result.getNames( "-o").isEmpty());
		Assert.assertFalse( result.getNames( "-p").isEmpty());
		Assert.assertFalse( result.getNames( "-I").isEmpty());
		Assert.assertEquals( 2, result.getNames( "-a").size());
		Assert.assertEquals( 1, result.getNames( "-b").size());
		Assert.assertEquals( 2, result.getNames( "-o").size());
		Assert.assertEquals( 4, result.getNames( "-p").size());
		Assert.assertEquals( 2, result.getNames( "-I").size());
		Assert.assertArrayEquals( new String[]{ "file1", "-"}, result.getArguments( "-o").toArray());
		Assert.assertArrayEquals( new String[]{ "p1", null, null, "profile1"}, result.getArguments( "-p").toArray());
		Assert.assertArrayEquals( new String[]{ "ignored", "ignored2"}, result.getArguments( "-I").toArray());
		Assert.assertArrayEquals( new String[]{ "--ignore", "--ignore"}, result.getNames( "-I").toArray());
		Assert.assertArrayEquals( new String[]{ "-", "xyz", "-a", "-a"}, result.getOperands().toArray());
	}
}
