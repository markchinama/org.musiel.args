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

enum PrimitiveType {

	VOID( void.class, null, Void.class, Void[].class, null),

	BOOLEAN( boolean.class, boolean[].class, Boolean.class, Boolean[].class, false),

	BYTE( byte.class, byte[].class, Byte.class, Byte[].class, ( byte) 0),

	SHORT( short.class, short[].class, Short.class, Short[].class, ( short) 0),

	INT( int.class, int[].class, Integer.class, Integer[].class, 0),

	LONG( long.class, long[].class, Long.class, Long[].class, 0L),

	FLOAT( float.class, float[].class, Float.class, Float[].class, 0.0F),

	DOUBLE( double.class, double[].class, Double.class, Double[].class, 0.0D),

	CHAR( char.class, char[].class, Character.class, Character[].class, ( char) 0),

	;

	private final Class< ?> type;
	private final Class< ?> arrayType;
	private final Class< ?> wrapperType;
	private final Class< ?> wrapperArrayType;
	private final Object defaultValue;

	public Class< ?> getType() {
		return this.type;
	}

	public Class< ?> getArrayType() {
		return this.arrayType;
	}

	public Class< ?> getWrapperType() {
		return this.wrapperType;
	}

	public Class< ?> getWrapperArrayType() {
		return this.wrapperArrayType;
	}

	public Object getDefaultValue() {
		return this.defaultValue;
	}

	private PrimitiveType( final Class< ?> type, final Class< ?> arrayType, final Class< ?> wrapperType, final Class< ?> wrapperArrayType,
			final Object defaultValue) {
		this.type = type;
		this.arrayType = arrayType;
		this.wrapperType = wrapperType;
		this.wrapperArrayType = wrapperArrayType;
		this.defaultValue = defaultValue;
	}

	public static PrimitiveType forPrimitiveType( final Class< ?> primitiveType) {
		for( final PrimitiveType type: PrimitiveType.values())
			if( type.type.equals( primitiveType))
				return type;
		return null;
	}
}
