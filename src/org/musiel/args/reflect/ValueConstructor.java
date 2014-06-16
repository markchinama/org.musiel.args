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
package org.musiel.args.reflect;

import java.lang.reflect.Array;
import java.util.LinkedList;

interface ValueConstructor {

	public boolean expectsMany();

	public boolean dependsOnContent();

	public Object decode( String overrideDefaultValue, String... stringValues) throws DecoderExceptions;
}

class NullConstructor implements ValueConstructor {

	@ Override
	public boolean expectsMany() {
		return false;
	}

	@ Override
	public boolean dependsOnContent() {
		return false;
	}

	@ Override
	public Object decode( final String overrideDefaultValue, final String... stringValues) {
		return null;
	}
}

class ExistenceIndicator implements ValueConstructor {

	@ Override
	public boolean expectsMany() {
		return false;
	}

	@ Override
	public boolean dependsOnContent() {
		return false;
	}

	@ Override
	public Boolean decode( final String overrideDefaultValue, final String... stringValues) {
		return Boolean.valueOf( stringValues.length > 0);
	}
}

class ObjectConstructor implements ValueConstructor {

	private final Decoder< ?> decoder;
	private final Object defaultValue;

	public ObjectConstructor( final Decoder< ?> decoder, final Object defaultValue) {
		super();
		this.decoder = decoder;
		this.defaultValue = defaultValue;
	}

	@ Override
	public boolean expectsMany() {
		return false;
	}

	@ Override
	public boolean dependsOnContent() {
		return true;
	}

	@ Override
	public Object decode( final String overrideDefaultValue, final String... stringValues) throws DecoderExceptions {
		try {
			return stringValues.length >= 1 && stringValues[ 0] != null? this.decoder.decode( stringValues[ 0])
					: overrideDefaultValue != null? this.decoder.decode( overrideDefaultValue): this.defaultValue;
		} catch( final DecoderException exception) {
			throw new DecoderExceptions( exception);
		}
	}
}

class ArrayConstructor implements ValueConstructor {

	private final Decoder< ?> decoder;
	private final Class< ?> componentType;
	private final Object defaultValue;

	public ArrayConstructor( final Decoder< ?> decoder, final Class< ?> componentType, final Object defaultValue) {
		super();
		this.decoder = decoder;
		this.componentType = componentType;
		this.defaultValue = defaultValue;
	}

	@ Override
	public boolean expectsMany() {
		return true;
	}

	@ Override
	public boolean dependsOnContent() {
		return true;
	}

	@ Override
	public Object decode( final String overrideDefaultValue, final String... stringValues) throws DecoderExceptions {
		final LinkedList< DecoderException> decoderExceptions = new LinkedList<>();
		final Object array = Array.newInstance( this.componentType, stringValues.length);
		for( int index = 0; index < stringValues.length; ++index)
			try {
				Array.set( array, index, stringValues[ index] != null? this.decoder.decode( stringValues[ index])
						: overrideDefaultValue != null? this.decoder.decode( overrideDefaultValue): this.defaultValue);
			} catch( final DecoderException exception) {
				decoderExceptions.add( exception);
			}
		if( !decoderExceptions.isEmpty())
			throw new DecoderExceptions( decoderExceptions);
		return array;
	}
}
