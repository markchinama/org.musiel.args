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

abstract class ValueConstructor {

	private final boolean expectsMany;
	private final boolean dependsOnContent;
	private final Decoder< ?> decoder;
	private final Object defaultValue;

	public ValueConstructor( final boolean expectsMany, final boolean dependsOnContent, final Decoder< ?> decoder,
			final Object defaultValue) {
		super();
		this.expectsMany = expectsMany;
		this.dependsOnContent = dependsOnContent;
		this.decoder = decoder;
		this.defaultValue = defaultValue;
	}

	public boolean expectsMany() {
		return this.expectsMany;
	}

	public boolean dependsOnContent() {
		return this.dependsOnContent;
	}

	public abstract Object decode( String overrideDefaultValue, String environmentVariableName, String... stringValues)
			throws DecoderExceptions;

	protected Object decodeSingle( final String overrideDefaultValue, final String environmentVariableName, final String value)
			throws DecoderException {
		if( value != null)
			return this.decoder.decode( value);
		final String envVarValue = environmentVariableName == null? null: System.getenv( environmentVariableName);
		if( envVarValue != null)
			try {
				return this.decoder.decode( envVarValue);
			} catch( final DecoderException exception) {
				throw new DecoderException( exception, ValueConstructor.class.getPackage().getName() + ".exceptions",
						"illegal-value.from-env-var", environmentVariableName);
			}
		if( overrideDefaultValue != null)
			return this.decoder.decode( overrideDefaultValue);
		return this.defaultValue;
	}
}

class NullConstructor extends ValueConstructor {

	public NullConstructor() {
		super( false, false, null, null);
	}

	@ Override
	public Object decode( final String overrideDefaultValue, final String environmentVariableName, final String... stringValues) {
		return null;
	}
}

class ExistenceIndicator extends ValueConstructor {

	public ExistenceIndicator() {
		super( false, false, null, null);
	}

	@ Override
	public Boolean decode( final String overrideDefaultValue, final String environmentVariableName, final String... stringValues) {
		return Boolean.valueOf( stringValues.length > 0);
	}
}

class ObjectConstructor extends ValueConstructor {

	public ObjectConstructor( final Decoder< ?> decoder, final Object defaultValue) {
		super( false, true, decoder, defaultValue);
	}

	@ Override
	public Object decode( final String overrideDefaultValue, final String environmentVariableName, final String... stringValues)
			throws DecoderExceptions {
		try {
			return this.decodeSingle( overrideDefaultValue, environmentVariableName, stringValues.length < 1? null: stringValues[ 0]);
		} catch( final DecoderException exception) {
			throw new DecoderExceptions( exception);
		}
	}
}

class ArrayConstructor extends ValueConstructor {

	private final Class< ?> componentType;

	public ArrayConstructor( final Decoder< ?> decoder, final Class< ?> componentType, final Object defaultValue) {
		super( true, true, decoder, defaultValue);
		this.componentType = componentType;
	}

	@ Override
	public Object decode( final String overrideDefaultValue, final String environmentVariableName, final String... stringValues)
			throws DecoderExceptions {
		final LinkedList< DecoderException> decoderExceptions = new LinkedList<>();
		final Object array = Array.newInstance( this.componentType, stringValues.length);
		for( int index = 0; index < stringValues.length; ++index)
			try {
				Array.set( array, index, this.decodeSingle( overrideDefaultValue, environmentVariableName, stringValues[ index]));
			} catch( final DecoderException exception) {
				decoderExceptions.add( exception);
			}
		if( !decoderExceptions.isEmpty())
			throw new DecoderExceptions( decoderExceptions);
		return array;
	}
}
