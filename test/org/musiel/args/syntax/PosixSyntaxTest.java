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

import org.junit.Assert;
import org.junit.Test;
import org.musiel.args.ParserException;
import org.musiel.args.syntax.SyntaxException.Reason;

public class PosixSyntaxTest extends AbstractPosixSyntaxTest {

	@ Override
	public void setup() {
		this.syntax = new PosixSyntax();
	}

	@ Test
	public void testDefaultConfigurations() {
		Assert.assertFalse( this.syntax.isOptionalArgumentsAllowed());
		Assert.assertFalse( this.syntax.isJointArgumentsAllowed());
		Assert.assertFalse( this.syntax.isLateOptionsAllowed());
	}

	@ Test
	public void testIllegalNameWithLegalNames() {
		this.testInvalidOption( this.option( "-a", "-3", "--A"));
	}

	@ Test
	public void testLateOption() throws ParserException {
		this.syntax.parse( this.options, "-a", "file1");
		this.testExceptionalParse( Reason.LATE_OPTION, this.options, "-a", "file1", "-a");
	}
}
