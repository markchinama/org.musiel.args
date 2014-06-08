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
package org.musiel.args.generic;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.musiel.args.ParserException;
import org.musiel.args.Result;

public abstract class AbstractParserTest {

	@ Rule
	public final ExpectedException exceptions = ExpectedException.none();

	private AbstractParser< Result> parser;

	@ Before
	public void setup() {
		this.parser = this.newParser();
		this.parser.newOption( "-a", "--all");
	}

	protected abstract AbstractParser< Result> newParser();

	@ Test
	public void options() {
		Assert.assertEquals( 1, this.parser.getOptions().size());
		Assert.assertFalse( this.parser.getOption( "-a").isRequired());
		Assert.assertTrue( this.parser.getOption( "-a").isRepeatable());
		Assert.assertFalse( this.parser.getOption( "-a").isArgumentAccepted());
		Assert.assertFalse( this.parser.getOption( "-a").isArgumentRequired());
		Assert.assertArrayEquals( new String[]{ "-a", "--all"}, this.parser.getOption( "-a").getNames().toArray());
		this.exceptions.expect( UnsupportedOperationException.class);
		this.parser.getOptions().iterator().remove();
	}

	@ Test
	public void unsupportedOption() {
		this.exceptions.expect( IllegalArgumentException.class);
		this.exceptions.expectMessage( "optional option-argument is not allowed");
		this.parser.newOption( false, true, true, false, "-m");
	}

	@ Test
	public void duplicateOptionName() {
		this.exceptions.expect( IllegalArgumentException.class);
		this.exceptions.expectMessage( "duplicate option name");
		this.parser.newOption( "-a");
	}

	@ Test( expected = ArrayIndexOutOfBoundsException.class)
	public void outOfRange() throws ParserException {
		this.parser.parse( new String[]{ "-!!==", "-a", "file1"}, 4);
	}

	@ Test( expected = ArrayIndexOutOfBoundsException.class)
	public void outOfRange2() throws ParserException {
		this.parser.parse( new String[]{ "-!!==", "-a", "file1"}, 1, 3);
	}

	@ Test
	public void normalPath() throws ParserException {
		Result result = this.parser.parse( new String[]{ "-!!==", "-a", "file1"}, 1);
		Assert.assertArrayEquals( new String[]{ "file1"}, result.getOperands().toArray());
		result = this.parser.parse( new String[]{ "-!!==", "-a", "file1", "wontsee", null}, 1, 2);
		Assert.assertArrayEquals( new String[]{ "file1"}, result.getOperands().toArray());
	}
}
