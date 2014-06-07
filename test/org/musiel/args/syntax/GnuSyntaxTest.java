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
import org.musiel.args.Option;
import org.musiel.args.ParserException;
import org.musiel.args.syntax.Syntax.ParseResult;

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
	public void testDefaultLateOptionAllowed() throws ParserException {
		this.syntax.parse( this.options, "-a", "file1", "-a");
	}

	@ Test
	public void testLateOptionDisabled() throws ParserException {
		this.syntax.setLateOptionsAllowed( false);
		this.testExceptionalParse( LateOptionException.class, "options must precede operands", this.options, "-a", "file1", "-a");
	}

	@ Test
	public void testUnknownLongName() throws ParserException {
		final Set< Option> options = new HashSet<>( this.options);
		options.add( this.option( false, true, true, true, "--ignore", "-I"));
		options.add( this.option( false, true, true, true, "--ignore-file", "-F"));
		this.syntax.parse( options, "--ignore", "ignored", "--ignore-file=ignored-file");
		this.testExceptionalParse( UnknownOptionException.class, "unknown option", options, "--ignored", "ignored", "--ignor=ignored-file");
	}

	@ Test
	public void testUnknownLongName2() throws ParserException {
		final Set< Option> options = new HashSet<>( this.options);
		options.add( this.option( false, true, true, true, "--ignore", "-I"));
		options.add( this.option( false, true, true, true, "--ignore-file", "-F"));
		this.syntax.parse( options, "--ignore", "ignored", "--ignore-file=ignored-file");
		this.testExceptionalParse( UnknownOptionException.class, "unknown option", options, "--ignore", "ignored", "--ignor?=ignored-file");
	}

	@ Test
	public void testAbbreviationDisabled() throws ParserException {
		final Set< Option> options = new HashSet<>( this.options);
		options.add( this.option( false, true, true, true, "--ignore", "-I"));
		this.syntax.parse( options, "--ign", "ignored");
		( ( GnuSyntax) this.syntax).setAbbreviationAllowed( false);
		this.testExceptionalParse( UnknownOptionException.class, "unknown option", options, "--ign", "ignored");
	}

	@ Test
	public void testAmbiguous() throws ParserException {
		final Set< Option> options = new HashSet<>( this.options);
		options.add( this.option( false, true, true, true, "--ignore", "-I"));
		options.add( this.option( false, true, true, true, "--ignore-file", "-F"));

		ParseResult result = this.syntax.parse( options, "--ignore", "ignored", "--ignore-file=ignored-file");
		Assert.assertArrayEquals( new String[]{ "ignored"}, result.getArguments( "-I").toArray());
		Assert.assertArrayEquals( new String[]{ "ignored-file"}, result.getArguments( "-F").toArray());

		result = this.syntax.parse( options, "--ignore", "ignored", "--ignore-=ignored-file");
		Assert.assertArrayEquals( new String[]{ "ignored"}, result.getArguments( "-I").toArray());
		Assert.assertArrayEquals( new String[]{ "ignored-file"}, result.getArguments( "-F").toArray());
		Assert.assertArrayEquals( new String[]{ "--ignore-file"}, result.getNames( "-F").toArray());

		this.testExceptionalParse( AmbiguousOptionException.class, "ambiguous", options, "--ignore", "ignored", "--ignor=ignored-file");
	}

	@ Test
	public void testParseOptionalGnu() throws ParserException {
		final Set< Option> options = new HashSet<>( this.options);
		options.add( this.option( false, true, true, false, "--profile", "-p"));
		options.add( this.option( false, true, true, true, "--ignore", "-I"));
		this.syntax.setOptionalArgumentsAllowed( true);
		final ParseResult result =
				this.syntax.parse( options, "-a", "-abpp1", "-o", "file1", "-p", "--profile", "--ignore", "ignored", "--ign=ignored2", "-",
						"xyz", "--profile=profile1", "-o-", "--", "-a", "-a");
		Assert.assertTrue( result.isOccurred( "-a"));
		Assert.assertTrue( result.isOccurred( "-b"));
		Assert.assertTrue( result.isOccurred( "-o"));
		Assert.assertTrue( result.isOccurred( "-p"));
		Assert.assertTrue( result.isOccurred( "-I"));
		Assert.assertEquals( 2, result.getOccurrences( "-a"));
		Assert.assertEquals( 1, result.getOccurrences( "-b"));
		Assert.assertEquals( 2, result.getOccurrences( "-o"));
		Assert.assertEquals( 4, result.getOccurrences( "-p"));
		Assert.assertEquals( 2, result.getOccurrences( "-I"));
		Assert.assertArrayEquals( new String[]{ "file1", "-"}, result.getArguments( "-o").toArray());
		Assert.assertArrayEquals( new String[]{ "p1", null, null, "profile1"}, result.getArguments( "-p").toArray());
		Assert.assertArrayEquals( new String[]{ "ignored", "ignored2"}, result.getArguments( "-I").toArray());
		Assert.assertArrayEquals( new String[]{ "--ignore", "--ignore"}, result.getNames( "-I").toArray());
		Assert.assertArrayEquals( new String[]{ "-", "xyz", "-a", "-a"}, result.getOperands().toArray());
	}
}
