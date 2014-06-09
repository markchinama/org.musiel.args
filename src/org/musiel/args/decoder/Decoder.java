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
package org.musiel.args.decoder;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * An interface that decodes {@link String} representations into other object types.
 * 
 * @author Bagana
 * 
 * @param <T>
 */
public interface Decoder< T> {

	/**
	 * Decode the string representation.
	 * 
	 * <p>
	 * If the given string is <code>null</code>, an implementation MUST return <code>null</code>.
	 * </p>
	 * 
	 * @param stringRepresentation
	 * @return
	 * @throws DecoderException
	 */
	public T decode( String stringRepresentation) throws DecoderException;

	public static final Decoder< Void> VOID_DECODER = new Decoder< Void>() {

		@ Override
		public Void decode( final String stringRepresentation) {
			return null;
		}
	};

	public static final Decoder< Byte> BYTE_DECODER = new NumberDecoder< Byte>( "byte") {

		@ Override
		protected Byte valueOf( final String stringRepresentation) throws NumberFormatException {
			return Byte.valueOf( ( byte) Integer.parseInt( stringRepresentation, 16));
		}
	};

	public static final Decoder< Short> SHORT_DECODER = new NumberDecoder< Short>( "short") {

		@ Override
		protected Short valueOf( final String stringRepresentation) {
			return Short.valueOf( stringRepresentation);
		}
	};

	public static final Decoder< Integer> INTEGER_DECODER = new NumberDecoder< Integer>( "integer") {

		@ Override
		protected Integer valueOf( final String stringRepresentation) {
			return Integer.valueOf( stringRepresentation);
		}
	};

	public static final Decoder< Long> LONG_DECODER = new NumberDecoder< Long>( "long") {

		@ Override
		protected Long valueOf( final String stringRepresentation) {
			return Long.valueOf( stringRepresentation);
		}
	};

	public static final Decoder< Float> FLOAT_DECODER = new NumberDecoder< Float>( "float") {

		@ Override
		protected Float valueOf( final String stringRepresentation) {
			return Float.valueOf( stringRepresentation);
		}
	};

	public static final Decoder< Double> DOUBLE_DECODER = new NumberDecoder< Double>( "double") {

		@ Override
		protected Double valueOf( final String stringRepresentation) {
			return Double.valueOf( stringRepresentation);
		}
	};

	public static final Decoder< Boolean> BOOLEAN_DECODER = new Decoder< Boolean>() {

		@ Override
		public Boolean decode( final String stringRepresentation) throws DecoderException {
			if( stringRepresentation == null)
				return null;
			switch( stringRepresentation.toUpperCase()) {
				case "TRUE":
				case "T":
				case "YES":
				case "Y":
					return Boolean.TRUE;
				case "FALSE":
				case "F":
				case "NO":
				case "N":
					return Boolean.FALSE;
				default:
					throw new DecoderException( "invalid boolean value: " + stringRepresentation);
			}
		}
	};

	public static final Decoder< Character> CHARACTER_DECODER = new Decoder< Character>() {

		@ Override
		public Character decode( final String stringRepresentation) throws DecoderException {
			if( stringRepresentation == null)
				return null;
			if( stringRepresentation.length() != 1)
				throw new DecoderException( "invalid char value: " + stringRepresentation);
			return Character.valueOf( stringRepresentation.charAt( 0));
		}
	};

	public static final Decoder< BigInteger> BIG_INTEGER_DECODER = new NumberDecoder< BigInteger>( "integer") {

		@ Override
		protected BigInteger valueOf( final String stringRepresentation) {
			return new BigInteger( stringRepresentation);
		}
	};

	public static final Decoder< BigDecimal> BIG_DECIMAL_DECODER = new NumberDecoder< BigDecimal>( "decimal") {

		@ Override
		protected BigDecimal valueOf( final String stringRepresentation) {
			return new BigDecimal( stringRepresentation);
		}
	};

	public static final Decoder< String> STRING_DECODER = new Decoder< String>() {

		@ Override
		public String decode( final String stringRepresentation) {
			return stringRepresentation;
		}
	};

	public static final Decoder< File> FILE_DECODER = new Decoder< File>() {

		@ Override
		public File decode( final String stringRepresentation) {
			return stringRepresentation == null? null: new File( stringRepresentation);
		}
	};
}

abstract class NumberDecoder< T> implements Decoder< T> {

	private final String typeName;

	protected NumberDecoder( final String typeName) {
		super();
		this.typeName = typeName;
	}

	@ Override
	public T decode( final String stringRepresentation) throws DecoderException {
		if( stringRepresentation == null)
			return null;
		try {
			return this.valueOf( stringRepresentation);
		} catch( final NumberFormatException formatException) {
			throw new DecoderException( "invalid " + this.typeName + " value: " + stringRepresentation);
		}
	}

	protected abstract T valueOf( String stringRepresentation);
}
