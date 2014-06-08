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

import java.util.List;
import java.util.Map;

import org.musiel.args.DefaultAccessor;
import org.musiel.args.syntax.Syntax.SyntaxResult;

public class GenericAccessor implements DefaultAccessor {

	private final SyntaxResult syntaxResult;
	private final Map< String, ? extends List< String>> operandMap;

	public GenericAccessor( final SyntaxResult syntaxResult, final Map< String, ? extends List< String>> operandMap) {
		super();
		this.syntaxResult = syntaxResult;
		this.operandMap = operandMap;
	}

	@ Override
	public boolean isOccurred( final String option) {
		return !this.getNames( option).isEmpty();
	}

	@ Override
	public int getOccurrences( final String option) {
		return this.getNames( option).size();
	}

	@ Override
	public List< String> getNames( final String option) {
		return this.syntaxResult.getNames( option);
	}

	@ Override
	public String getName( final String option) {
		return this.getSingle( this.getNames( option));
	}

	@ Override
	public List< String> getArguments( final String option) {
		return this.syntaxResult.getArguments( option);
	}

	@ Override
	public String getArgument( final String option) {
		return this.getSingle( this.syntaxResult.getArguments( option));
	}

	@ Override
	public List< String> getOperands() {
		return this.syntaxResult.getOperands();
	}

	@ Override
	public List< String> getOperands( final String operandName) {
		return this.operandMap.get( operandName);
	}

	@ Override
	public String getOperand( final String operandName) {
		return this.getSingle( this.getOperands( operandName));
	}

	private < E>E getSingle( final List< E> list) {
		if( list == null)
			return null;
		return list.isEmpty()? null: list.get( 0);
	}
}
