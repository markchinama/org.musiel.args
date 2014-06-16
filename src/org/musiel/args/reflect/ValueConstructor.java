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

	public Object decode( String overrideDefaultValue, String environmentVariableName, String... stringValues) throws DecoderExceptions;
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
	public Object decode( final String overrideDefaultValue, final String environmentVariableName, final String... stringValues) {
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
	public Boolean decode( final String overrideDefaultValue, final String environmentVariableName, final String... stringValues) {
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
	public Object decode( final String overrideDefaultValue, final String environmentVariableName, final String... stringValues)
			throws DecoderExceptions {
		try {
			if( stringValues.length >= 1 && stringValues[ 0] != null)
				return this.decoder.decode( stringValues[ 0]);
			final String environmentVariableValue = environmentVariableName == null? null: System.getenv( environmentVariableName);
			if( environmentVariableValue != null)
				try {
					return this.decoder.decode( environmentVariableValue);
				} catch( final DecoderException exception) {
					throw new DecoderException( exception.getMessage()); // TODO explain in the message that the value was from env. var.
				}
			if( overrideDefaultValue != null)
				return this.decoder.decode( overrideDefaultValue);
			return this.defaultValue;
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
	public Object decode( final String overrideDefaultValue, final String environmentVariableName, final String... stringValues)
			throws DecoderExceptions {
		final LinkedList< DecoderException> decoderExceptions = new LinkedList<>();
		final Object array = Array.newInstance( this.componentType, stringValues.length);
		for( int index = 0; index < stringValues.length; ++index)
			try {
				if( stringValues[ index] != null)
					Array.set( array, index, this.decoder.decode( stringValues[ index]));
				else {
					final String environmentVariableValue = environmentVariableName == null? null: System.getenv( environmentVariableName);
					if( environmentVariableValue != null)
						try {
							Array.set( array, index, this.decoder.decode( environmentVariableValue));
						} catch( final DecoderException exception) {
							throw new DecoderException( exception.getMessage()); // TODO explain in the msg. that the val. was from env. var.
						}
					else if( overrideDefaultValue != null)
						Array.set( array, index, this.decoder.decode( overrideDefaultValue));
					else
						Array.set( array, index, this.defaultValue);
				}
			} catch( final DecoderException exception) {
				decoderExceptions.add( exception);
			}
		if( !decoderExceptions.isEmpty())
			throw new DecoderExceptions( decoderExceptions);
		return array;
	}
}
