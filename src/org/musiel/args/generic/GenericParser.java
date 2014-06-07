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

import org.musiel.args.Option;
import org.musiel.args.Result;
import org.musiel.args.syntax.GnuSyntax;
import org.musiel.args.syntax.Syntax;

public class GenericParser extends AbstractParser< Result> {

	public GenericParser() {
		this( new GnuSyntax());
	}

	public GenericParser( final Syntax syntax) {
		super( syntax);
	}

	@ Override
	protected Option newOption( final String name, final String... aliases) {
		return super.newOption( name, aliases);
	}

	@ Override
	protected Option newOption( final boolean required, final boolean repeatable, final boolean acceptsArgument,
			final boolean requiresArgument, final String name, final String... aliases) {
		return super.newOption( required, repeatable, acceptsArgument, requiresArgument, name, aliases);
	}

	@ Override
	protected void setOperandPattern( final String operandPattern) {
		super.setOperandPattern( operandPattern);
	}

	@ Override
	protected Result postProcess( final Result result) {
		return result;
	}
}
