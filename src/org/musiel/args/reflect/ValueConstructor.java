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

	public abstract Object decode( ExceptionHandler< ? super DecoderException> exceptionHandler, String overrideDefaultValue,
			String environmentVariableName, String... stringValues);

	protected Object decodeSingle( final String overrideDefaultValue, final String environmentVariableName, final String value,
			final ExceptionHandler< ? super DecoderException> exceptionHandler) {
		if( value != null)
			try {
				return this.decoder.decode( value);
			} catch( final DecoderException exception) {
				exceptionHandler.handle( exception);
				return this.defaultValue;
			}
		final String envVarValue = environmentVariableName == null? null: System.getenv( environmentVariableName);
		if( envVarValue != null)
			try {
				return this.decoder.decode( envVarValue);
			} catch( final DecoderException exception) {
				exceptionHandler.handle( new DecoderException( exception, ValueConstructor.class.getPackage().getName() + ".exceptions",
						"illegal-value.from-env-var", environmentVariableName)); // continue decoding with default values
				return this.defaultValue;
			}
		if( overrideDefaultValue != null)
			try {
				return this.decoder.decode( overrideDefaultValue);
			} catch( final DecoderException exception) {
				exceptionHandler.handle( exception);
				return this.defaultValue;
			}
		return this.defaultValue;
	}
}

class NullConstructor extends ValueConstructor {

	public NullConstructor() {
		super( false, false, null, null);
	}

	@ Override
	public Object decode( final ExceptionHandler< ? super DecoderException> exceptionHandler, final String overrideDefaultValue,
			final String environmentVariableName, final String... stringValues) {
		return null;
	}
}

class ExistenceIndicator extends ValueConstructor {

	public ExistenceIndicator() {
		super( false, false, null, null);
	}

	@ Override
	public Object decode( final ExceptionHandler< ? super DecoderException> exceptionHandler, final String overrideDefaultValue,
			final String environmentVariableName, final String... stringValues) {
		return Boolean.valueOf( stringValues.length > 0);
	}
}

class ObjectConstructor extends ValueConstructor {

	public ObjectConstructor( final Decoder< ?> decoder, final Object defaultValue) {
		super( false, true, decoder, defaultValue);
	}

	@ Override
	public Object decode( final ExceptionHandler< ? super DecoderException> exceptionHandler, final String overrideDefaultValue,
			final String environmentVariableName, final String... stringValues) {
		return this.decodeSingle( overrideDefaultValue, environmentVariableName, stringValues.length < 1? null: stringValues[ 0],
				exceptionHandler);
	}
}

class ArrayConstructor extends ValueConstructor {

	private final Class< ?> componentType;

	public ArrayConstructor( final Decoder< ?> decoder, final Class< ?> componentType, final Object defaultValue) {
		super( true, true, decoder, defaultValue);
		this.componentType = componentType;
	}

	@ Override
	public Object decode( final ExceptionHandler< ? super DecoderException> exceptionHandler, final String overrideDefaultValue,
			final String environmentVariableName, final String... stringValues) {
		final Object array = Array.newInstance( this.componentType, stringValues.length);
		for( int index = 0; index < stringValues.length; ++index)
			Array.set( array, index,
					this.decodeSingle( overrideDefaultValue, environmentVariableName, stringValues[ index], exceptionHandler));
		return array;
	}
}
