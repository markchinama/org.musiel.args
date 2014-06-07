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
package org.musiel.args.reflect;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;
import org.musiel.args.ParserException;

public class ReflectParserTest {

	@ OperandPattern( "[INPUT... OUTPUT]")
	private static interface Options {

		public boolean help();

		public boolean version();

		@ Option( { "-v", "--verbose"})
		public boolean verbose();

		public Integer logLevel();

		public boolean r();

		public int[] index();

		@ Operands( "INPUT")
		public File[] inputFiles();

		@ Operands( "OUTPUT")
		public File outputFile();
	}

	@ Test
	public void test() throws ParserException {
		final Options options = ReflectParser.parse( Options.class, "--help", "input", "output", "--index", "3", "--index", "9");
		Assert.assertTrue( options.help());
		Assert.assertArrayEquals( new File[]{ new File( "input")}, options.inputFiles());
		Assert.assertEquals( new File( "output"), options.outputFile());
		Assert.assertArrayEquals( new int[]{ 3, 9}, options.index());
	}
}
