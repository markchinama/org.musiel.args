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
import java.util.LinkedHashSet;

import org.musiel.args.ParserException;

public class TooManyOccurrenceException extends ParserException {

	private static final long serialVersionUID = -1928726195159739885L;

	private final String optionName;
	private final Collection< String> usedAliases;

	public TooManyOccurrenceException( final String optionName, final Collection< String> usedAliases) {
		super( "option " + ParserException.optionNamesToString( optionName, usedAliases) + " is specified more than once");
		this.optionName = optionName;
		this.usedAliases = Collections.unmodifiableCollection( new LinkedHashSet<>( usedAliases));
	}

	public String getOptionName() {
		return this.optionName;
	}

	public Collection< String> getUsedAliases() {
		return this.usedAliases;
	}
}
