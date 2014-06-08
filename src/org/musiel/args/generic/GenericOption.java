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

import org.musiel.args.ArgumentPolicy;
import org.musiel.args.Option;

/**
 * An implementation of {@link Option}.
 * 
 * @author Bagana
 */
class GenericOption implements Option {

	private final Set< String> names;
	private final boolean required;
	private final boolean repeatable;
	private final ArgumentPolicy argumentPolicy;

	@ Override
	public String getName() {
		return this.names.iterator().next();
	}

	@ Override
	public Set< String> getNames() {
		return this.names;
	}

	@ Override
	public boolean isRequired() {
		return this.required;
	}

	@ Override
	public boolean isRepeatable() {
		return this.repeatable;
	}

	@ Override
	public ArgumentPolicy getArgumentPolicy() {
		return this.argumentPolicy;
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
	public GenericOption( final boolean required, final boolean repeatable, final ArgumentPolicy argumentPolicy,
			final Collection< String> names) {
		if( argumentPolicy == null)
			throw new NullPointerException();
		final Set< String> nameSet = new LinkedHashSet<>( names);
		if( nameSet.isEmpty())
			throw new IllegalArgumentException( "name set must not be empty");
		if( nameSet.contains( null))
			throw new NullPointerException( "name must not be null");

		this.names = Collections.unmodifiableSet( nameSet);
		this.required = required;
		this.repeatable = repeatable;
		this.argumentPolicy = argumentPolicy;
	}

	public GenericOption( final boolean required, final boolean repeatable, final ArgumentPolicy argumentPolicy, final String name,
			final String... aliases) {
		this( required, repeatable, argumentPolicy, GenericOption.nameSet( name, aliases));
	}

	private static Collection< String> nameSet( final String primaryName, final String[] additionalNames) {
		final List< String> names = new LinkedList<>();
		names.add( primaryName);
		Collections.addAll( names, additionalNames);
		return names;
	}
}
