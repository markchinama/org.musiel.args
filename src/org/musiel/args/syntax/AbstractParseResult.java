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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.musiel.args.ArgumentException;
import org.musiel.args.Option;
import org.musiel.args.syntax.Syntax.SyntaxResult;

public abstract class AbstractParseResult implements SyntaxResult {

	protected final LinkedList< ArgumentException> errors = new LinkedList<>();

	@ Override
	public Collection< ? extends ArgumentException> getErrors() {
		return Collections.unmodifiableCollection( this.errors);
	}

	private final Set< Option> options;
	protected final Map< String, Option> optionDictionary = new TreeMap<>();
	protected List< String> operands = new LinkedList<>();

	protected AbstractParseResult( final Set< Option> options) {
		this.options = options;
		for( final Option option: this.options)
			for( final String name: option.getNames())
				if( this.optionDictionary.containsKey( name))
					throw new IllegalArgumentException( "duplicate name: " + name);
				else
					this.optionDictionary.put( name, option);
	}

	private final Map< String, List< String>> optionNames = new TreeMap<>();
	private final Map< String, List< String>> optionArguments = new TreeMap<>();

	private String getCanonicalName( final String optionName) {
		final Option option = this.optionDictionary.get( optionName);
		return option != null? option.getName(): optionName;
	}

	private List< String> getNamesInternal( final String optionName) {
		final String canonicalName = this.getCanonicalName( optionName);
		List< String> list = this.optionNames.get( canonicalName);
		if( list == null)
			this.optionNames.put( canonicalName, list = new LinkedList<>());
		return list;
	}

	private List< String> getArgumentsInternal( final String optionName) {
		final String canonicalName = this.getCanonicalName( optionName);
		List< String> list = this.optionArguments.get( canonicalName);
		if( list == null)
			this.optionArguments.put( canonicalName, list = new LinkedList<>());
		return list;
	}

	protected void push( final String optionName, final String optionArgument) {
		this.getNamesInternal( optionName).add( optionName);
		this.getArgumentsInternal( optionName).add( optionArgument);
	}

	@ Override
	public List< String> getNames( final String optionName) {
		return Collections.unmodifiableList( this.getNamesInternal( optionName));
	}

	@ Override
	public List< String> getArguments( final String optionName) {
		return Collections.unmodifiableList( this.getArgumentsInternal( optionName));
	}

	@ Override
	public List< String> getOperands() {
		return Collections.unmodifiableList( this.operands);
	}

	protected void build() {
		for( final Option option: this.options) {
			final List< String> names = this.getNamesInternal( option.getName());
			final List< String> arguments = this.getArgumentsInternal( option.getName());

			if( option.isRequired() && ( names == null || names.isEmpty()))
				this.errors.add( new MissingOptionException( option.getName()));

			if( !option.isRepeatable() && names != null && names.size() > 1)
				this.errors.add( new TooManyOccurrenceException( names.get( 1), names));

			if( !option.getArgumentPolicy().isAccepted()) {
				final Iterator< String> nameIterator = names.iterator();
				final Iterator< String> argumentIterator = arguments.iterator();
				while( nameIterator.hasNext()) {
					final String name = nameIterator.next();
					final String argument = argumentIterator.next();
					if( argument != null)
						this.errors.add( new UnexpectedArgumentException( name));
				}
			}

			if( option.getArgumentPolicy().isRequired()) {
				final Iterator< String> nameIterator = names.iterator();
				final Iterator< String> argumentIterator = arguments.iterator();
				while( nameIterator.hasNext()) {
					final String name = nameIterator.next();
					final String argument = argumentIterator.next();
					if( argument == null)
						this.errors.add( new ArgumentRequiredException( name));
				}
			}
		}

		this.toArrayLists( this.optionNames);
		this.toArrayLists( this.optionArguments);
		this.operands = new ArrayList<>( this.operands);
	}

	private void toArrayLists( final Map< String, List< String>> map) {
		for( final Entry< String, List< String>> entry: map.entrySet())
			entry.setValue( new ArrayList<>( entry.getValue()));
	}
}
