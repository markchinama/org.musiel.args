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
import java.util.LinkedList;

import org.musiel.args.ArgumentException;
import org.musiel.args.ArgumentExceptions;
import org.musiel.args.Result;

public class AbstractResult< ACCESSOR> implements Result< ACCESSOR> {

	private final Collection< ? extends ArgumentException> exceptions;
	private final ACCESSOR accessor;

	public AbstractResult( final Collection< ? extends ArgumentException> exceptions, final ACCESSOR accessor) {
		super();
		this.exceptions = exceptions;
		this.accessor = accessor;
	}

	@ Override
	public Collection< ? extends ArgumentException> getErrors() {
		return this.exceptions;
	}

	@ Override
	public AbstractResult< ACCESSOR> check( final Collection< Class< ? extends ArgumentException>> exceptionTypes)
			throws ArgumentExceptions {
		final LinkedList< ArgumentException> exceptions = new LinkedList<>();
		for( final Class< ? extends ArgumentException> exceptionType: exceptionTypes)
			for( final ArgumentException exception: this.exceptions)
				if( exceptionType.isInstance( exception))
					exceptions.add( exception);
		if( !exceptions.isEmpty())
			throw new ArgumentExceptions( exceptions);
		return this;
	}

	@ Override
	public AbstractResult< ACCESSOR> check( final Class< ? extends ArgumentException> exceptionType) throws ArgumentExceptions {
		final LinkedList< ArgumentException> exceptions = new LinkedList<>();
		for( final ArgumentException exception: this.exceptions)
			if( exceptionType.isInstance( exception))
				exceptions.add( exception);
		if( !exceptions.isEmpty())
			throw new ArgumentExceptions( exceptions);
		return this;
	}

	@ Override
	public ACCESSOR check() throws ArgumentExceptions {
		if( !this.exceptions.isEmpty())
			throw new ArgumentExceptions( this.exceptions);
		return this.accessor;
	}

	@ Override
	public ACCESSOR getAccessor() {
		return this.accessor;
	}
}
