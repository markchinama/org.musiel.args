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

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.musiel.args.DefaultAccessor;
import org.musiel.args.syntax.Syntax.SyntaxResult;

public class GenericAccessor implements DefaultAccessor {

	private final SyntaxResult syntaxResult;
	private final Map< String, List< String>> operandMap;

	public GenericAccessor( final SyntaxResult syntaxResult, final Map< String, List< String>> operandMap) {
		super();
		this.syntaxResult = syntaxResult;
		this.operandMap = operandMap;
	}

	@ Override
	public boolean isOccurred( final String optionName) {
		return !this.getNames( optionName).isEmpty();
	}

	@ Override
	public int getOccurrences( final String optionName) {
		return this.getNames( optionName).size();
	}

	@ Override
	public List< String> getNames( final String optionName) {
		return this.syntaxResult.getNames( optionName);
	}

	@ Override
	public String[] getNamesAsArray( final String optionName) {
		final List< String> list = this.getNames( optionName);
		return list.toArray( new String[ list.size()]);
	}

	@ Override
	public String getName( final String optionName) {
		return this.getSingle( this.getNames( optionName));
	}

	@ Override
	public List< String> getArguments( final String optionName) {
		return this.syntaxResult.getArguments( optionName);
	}

	@ Override
	public String[] getArgumentsAsArray( final String optionName) {
		final List< String> list = this.getArguments( optionName);
		return list.toArray( new String[ list.size()]);
	}

	@ Override
	public String getArgument( final String optionName) {
		return this.getSingle( this.syntaxResult.getArguments( optionName));
	}

	@ Override
	public List< String> getOperands() {
		return this.syntaxResult.getOperands();
	}

	@ Override
	public String[] getOperandsAsArray() {
		final List< String> list = this.getOperands();
		return list.toArray( new String[ list.size()]);
	}

	@ Override
	public List< String> getOperands( final String operandName) {
		List< String> list = this.operandMap.get( operandName);
		if( list == null)
			this.operandMap.put( operandName, list = new LinkedList<>());
		return list;
	}

	@ Override
	public String[] getOperandsAsArray( final String operandName) {
		final List< String> list = this.getOperands( operandName);
		return list.toArray( new String[ list.size()]);
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
