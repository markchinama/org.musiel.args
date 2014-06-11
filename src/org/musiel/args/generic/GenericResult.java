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
import java.util.List;
import java.util.Map;

import org.musiel.args.ArgumentException;
import org.musiel.args.ArgumentExceptions;
import org.musiel.args.Result;
import org.musiel.args.syntax.Syntax.SyntaxResult;

public class GenericResult extends GenericAccessor implements Result< GenericResult> {

	private final Collection< ? extends ArgumentException> exceptions;

	public GenericResult( final SyntaxResult syntaxResult, final Map< String, List< String>> operandMap,
			final Collection< ? extends ArgumentException> exceptions) {
		super( syntaxResult, operandMap);
		this.exceptions = exceptions;
	}

	@ Override
	public Collection< ? extends ArgumentException> getErrors() {
		return this.exceptions;
	}

	@ Override
	public GenericResult check( final Collection< Class< ? extends ArgumentException>> exceptionTypes) throws ArgumentExceptions {
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
	public GenericResult check( final Class< ? extends ArgumentException> exceptionType) throws ArgumentExceptions {
		final LinkedList< ArgumentException> exceptions = new LinkedList<>();
		for( final ArgumentException exception: this.exceptions)
			if( exceptionType.isInstance( exception))
				exceptions.add( exception);
		if( !exceptions.isEmpty())
			throw new ArgumentExceptions( exceptions);
		return this;
	}

	@ Override
	public GenericResult check() throws ArgumentExceptions {
		if( !this.exceptions.isEmpty())
			throw new ArgumentExceptions( this.exceptions);
		return this;
	}

	@ Override
	public GenericResult getAccessor() {
		return this;
	}
}
