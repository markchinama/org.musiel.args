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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;

public class TooManyOccurrenceException extends OptionException {

	private static final long serialVersionUID = 9108502009738456795L;

	private final Collection< String> additionalOptionNames = new HashSet<>();

	public TooManyOccurrenceException( final String optionName, final List< String> additionalOptionNames) {
		super( optionName, TooManyOccurrenceException.class.getPackage().getName() + ".exceptions", TooManyOccurrenceException.class
				.getSimpleName(), TooManyOccurrenceException.optionNamesToString( optionName, additionalOptionNames));
		this.additionalOptionNames.addAll( additionalOptionNames);
		this.additionalOptionNames.remove( optionName);
	}

	public Collection< String> getAdditionalOptionNames() {
		return Collections.unmodifiableCollection( this.additionalOptionNames);
	}

	private static String optionNamesToString( final String main, final Collection< String> additional) {
		final Collection< String> inParentheses = new LinkedHashSet<>( additional);
		inParentheses.remove( main);
		return inParentheses.isEmpty()? main: main + "(" + inParentheses.toString().substring( 1).replaceFirst( "\\]$", ")");
	}
}
