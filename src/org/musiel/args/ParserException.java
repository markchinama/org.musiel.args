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
package org.musiel.args;

import java.util.Collection;
import java.util.LinkedHashSet;

/**
 * Thrown on user errors during a parsing task, such as unknown options, missing options, unexpected option-arguments.
 * 
 * @author Bagana
 */
public class ParserException extends Exception {

	private static final long serialVersionUID = 2574553544635903474L;

	public ParserException( final String message) {
		super( message);
	}

	protected static String optionNamesToString( final String main, final Collection< String> additional) {
		final Collection< String> inParentheses = new LinkedHashSet<>( additional);
		inParentheses.remove( main);
		return inParentheses.isEmpty()? main: main + "(" + inParentheses.toString().substring( 1).replaceFirst( "\\]$", ")");
	}
}
