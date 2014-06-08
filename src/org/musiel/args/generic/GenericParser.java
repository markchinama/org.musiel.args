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

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.musiel.args.Option;
import org.musiel.args.ParserException;
import org.musiel.args.syntax.GnuSyntax;
import org.musiel.args.syntax.Syntax;
import org.musiel.args.syntax.Syntax.SyntaxResult;

public class GenericParser extends AbstractParser< GenericResult> {

	public GenericParser() {
		this( new GnuSyntax());
	}

	public GenericParser( final Syntax syntax) {
		super( syntax);
	}

	@ Override
	public Option newOption( final String name, final String... aliases) {
		return super.newOption( name, aliases);
	}

	@ Override
	public Option newOption( final boolean required, final boolean repeatable, final boolean acceptsArgument,
			final boolean requiresArgument, final String name, final String... aliases) {
		return super.newOption( required, repeatable, acceptsArgument, requiresArgument, name, aliases);
	}

	@ Override
	public void setOperandPattern( final String operandPattern) {
		super.setOperandPattern( operandPattern);
	}

	@ Override
	protected GenericResult adapt( final SyntaxResult syntaxResult, final Map< String, ? extends List< String>> operandMap,
			final Collection< ? extends ParserException> exceptions) {
		return new GenericResult( syntaxResult, operandMap, exceptions);
	}
}
