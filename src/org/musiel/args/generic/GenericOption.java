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
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.musiel.args.Option;

/**
 * An implementation of {@link Option}.
 * 
 * @author Bagana
 */
class GenericOption implements Option {

	private final boolean required;
	private final boolean repeatable;
	private final boolean acceptsArgument;
	private final boolean requiresArgument;

	private final Set< String> names;

	@ Override
	public boolean isRequired() {
		return this.required;
	}

	@ Override
	public boolean isRepeatable() {
		return this.repeatable;
	}

	@ Override
	public boolean isArgumentAccepted() {
		return this.acceptsArgument;
	}

	@ Override
	public boolean isArgumentRequired() {
		return this.requiresArgument;
	}

	@ Override
	public String getName() {
		return this.names.iterator().next();
	}

	@ Override
	public Set< String> getNames() {
		return this.names;
	}

	/**
	 * Construct an instance with names specified.
	 * 
	 * <p>
	 * If <code>acceptsArgument</code> is <code>false</code>, <code>requirsArgument</code> must be <code>false</code>; if
	 * <code>requiresArgument</code> is <code>true</code>, <code>acceptsArgument</code> must be <code>true</code>.
	 * </p>
	 * 
	 * <ul>
	 * <li><code>names</code> must not be empty and must not contain <code>null</code>.</li>
	 * <li>The names are ordered according to the iterator of the collection. And the first element becomes the return value of
	 * {@link #getName()}.</li>
	 * <li>Duplicate elements, if any, are simply skipped.</li>
	 * </ul>
	 * 
	 * @param name
	 * @param additionalNames
	 */
	public GenericOption( final boolean required, final boolean repeatable, final boolean acceptsArgument, final boolean requiresArgument,
			final Collection< String> names) {
		if( !acceptsArgument && requiresArgument)
			throw new IllegalArgumentException( "requiresArgument must be false when acceptsArgument is false");
		final Set< String> nameSet = new LinkedHashSet<>( names);
		if( nameSet.isEmpty())
			throw new IllegalArgumentException( "name set must not be empty");
		if( nameSet.contains( null))
			throw new NullPointerException( "name must not be null");

		this.required = required;
		this.repeatable = repeatable;
		this.acceptsArgument = acceptsArgument;
		this.requiresArgument = requiresArgument;

		this.names = Collections.unmodifiableSet( nameSet);
	}

	public GenericOption( final boolean required, final boolean repeatable, final boolean acceptsArgument, final boolean requiresArgument,
			final String name, final String... aliases) {
		this( required, repeatable, acceptsArgument, requiresArgument, GenericOption.nameSet( name, aliases));
	}

	private static Collection< String> nameSet( final String primaryName, final String[] additionalNames) {
		final List< String> names = new LinkedList<>();
		names.add( primaryName);
		Collections.addAll( names, additionalNames);
		return names;
	}
}
