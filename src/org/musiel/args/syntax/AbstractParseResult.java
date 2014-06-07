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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.musiel.args.Option;
import org.musiel.args.syntax.Syntax.ParseResult;

public abstract class AbstractParseResult implements ParseResult {

	protected final Map< String, Option> optionDictionary = new TreeMap<>();

	protected final Map< Option, LinkedList< String>> optionNames = new HashMap<>();
	protected final Map< Option, LinkedList< String>> optionArguments = new HashMap<>();
	protected final List< String> operands = new LinkedList<>();

	private Option getOption( final String optionName) {
		final Option option = this.optionDictionary.get( optionName);
		if( option == null)
			throw new IllegalArgumentException( "option \"" + optionName + "\" does not exist");
		return option;
	}

	@ Override
	public LinkedList< String> getNames( final String option) {
		return new LinkedList<>( this.optionNames.get( this.getOption( option)));
	}

	@ Override
	public LinkedList< String> getArguments( final String option) {
		return new LinkedList<>( this.optionArguments.get( this.getOption( option)));
	}

	@ Override
	public LinkedList< String> getOperands() {
		return new LinkedList<>( this.operands);
	}
}
