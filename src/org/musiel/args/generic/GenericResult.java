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

import org.musiel.args.Result;
import org.musiel.args.syntax.Syntax.ParseResult;

public class GenericResult implements Result {

	private final ParseResult syntaxResult;
	private final Map< String, ? extends LinkedList< String>> operandMap;

	public GenericResult( final ParseResult syntaxResult, final Map< String, ? extends LinkedList< String>> operandMap) {
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
	public List< String> getArguments( final String option) {
		return this.syntaxResult.getArguments( option);
	}

	@ Override
	public String getFirstArgument( final String option) {
		final LinkedList< String> arguments = this.syntaxResult.getArguments( option);
		return arguments.isEmpty()? null: arguments.getFirst();
	}

	@ Override
	public String getLastArgument( final String option) {
		final LinkedList< String> arguments = this.syntaxResult.getArguments( option);
		return arguments.isEmpty()? null: arguments.getLast();
	}

	@ Override
	public List< String> getOperands() {
		return this.syntaxResult.getOperands();
	}

	private LinkedList< String> checkAndGetOperands( final String operandName) {
		if( this.operandMap == null)
			return null;
		final LinkedList< String> list = this.operandMap.get( operandName);
		if( list == null)
			throw new IllegalArgumentException( "unknown operand name: " + operandName);
		return list;
	}

	@ Override
	public List< String> getOperands( final String operandName) {
		return this.checkAndGetOperands( operandName);
	}

	@ Override
	public String getFirstOperand( final String operandName) {
		final LinkedList< String> list = this.checkAndGetOperands( operandName);
		return list.isEmpty()? null: list.getFirst();
	}

	@ Override
	public String getLastOperand( final String operandName) {
		final LinkedList< String> list = this.checkAndGetOperands( operandName);
		return list.isEmpty()? null: list.getLast();
	}
}
