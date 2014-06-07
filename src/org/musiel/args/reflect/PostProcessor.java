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

import java.io.File;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.musiel.args.reflect.Argument.ArgumentStrategy;

class PostProcessor {

	public final Category category;
	private final Class< ?> type;
	private final Decoder< ?> decoder;

	public final Expectation expectation;

	public PostProcessor( final Class< ?> type) {
		this.category = //
				void.class.equals( type)? Category.MANDATORY_FLAG: //
						boolean.class.equals( type)? Category.FLAG: //
								type.isPrimitive()? Category.PRIMITIVE: //
										!type.isArray()? Category.OBJECT: //
												type.getComponentType().isPrimitive()? Category.PRIMITIVE_ARRAY: //
														Category.OBJECT_ARRAY;
		this.type = type.isArray()? type.getComponentType(): type;
		this.decoder = PostProcessor.DECODERS.get( this.type);
		if( this.decoder == null)
			throw new IllegalArgumentException( "type " + this.type.getName() + " is not supported");

		try {
			this.expectation = Category.class.getField( this.category.name()).getAnnotation( Expectation.class);
		} catch( final NoSuchFieldException e) {
			throw new AssertionError();
		}
	}

	public Object decode( final List< String> data) throws DecoderException {
		switch( this.category) {
			case FLAG:
				return Boolean.valueOf( !data.isEmpty());
			case MANDATORY_FLAG:
			case PRIMITIVE:
			case OBJECT:
				return data.isEmpty()? null: this.decoder.decode( data.get( 0));
			case PRIMITIVE_ARRAY:
			case OBJECT_ARRAY:
				final Object decoded = Array.newInstance( this.type, data.size());
				int decodeIndex = 0;
				for( final String item: data)
					Array.set( decoded, decodeIndex++, this.decoder.decode( item));
				return decoded;
			default:
				throw new AssertionError();
		}
	}

	protected static final Map< Class< ?>, Decoder< ?>> DECODERS;
	static {
		final Map< Class< ?>, Decoder< ?>> decoders = new HashMap<>();

		decoders.put( void.class, Decoder.VOID_DECODER);

		decoders.put( byte.class, Decoder.BYTE_DECODER);
		decoders.put( short.class, Decoder.SHORT_DECODER);
		decoders.put( int.class, Decoder.INTEGER_DECODER);
		decoders.put( long.class, Decoder.LONG_DECODER);
		decoders.put( float.class, Decoder.FLOAT_DECODER);
		decoders.put( double.class, Decoder.DOUBLE_DECODER);
		decoders.put( boolean.class, Decoder.BOOLEAN_DECODER);
		decoders.put( char.class, Decoder.CHARACTER_DECODER);

		decoders.put( Void.class, Decoder.VOID_DECODER);

		decoders.put( Byte.class, Decoder.BYTE_DECODER);
		decoders.put( Short.class, Decoder.SHORT_DECODER);
		decoders.put( Integer.class, Decoder.INTEGER_DECODER);
		decoders.put( Long.class, Decoder.LONG_DECODER);
		decoders.put( Float.class, Decoder.FLOAT_DECODER);
		decoders.put( Double.class, Decoder.DOUBLE_DECODER);
		decoders.put( Boolean.class, Decoder.BOOLEAN_DECODER);
		decoders.put( Character.class, Decoder.CHARACTER_DECODER);

		decoders.put( BigInteger.class, Decoder.BIG_INTEGER_DECODER);
		decoders.put( BigDecimal.class, Decoder.BIG_DECIMAL_DECODER);

		decoders.put( String.class, Decoder.STRING_DECODER);
		decoders.put( File.class, Decoder.FILE_DECODER);

		DECODERS = Collections.unmodifiableMap( decoders);
	}
}

enum Category {

	@ Expectation( required = true, argument = ArgumentStrategy.NONE, argumentChangeable = false)
	MANDATORY_FLAG,

	@ Expectation( argument = ArgumentStrategy.NONE, argumentChangeable = false)
	FLAG,

	@ Expectation( required = true, requiredChangeable = false, repeatable = false, repeatableChangeable = false, argumentChangeable = false)
	PRIMITIVE,

	@ Expectation( repeatable = false, repeatableChangeable = false)
	OBJECT,

	@ Expectation( argumentChangeable = false)
	PRIMITIVE_ARRAY,

	@ Expectation
	OBJECT_ARRAY;
}

@ Target( ElementType.FIELD)
@ Retention( RetentionPolicy.RUNTIME)
@ interface Expectation {

	public boolean required() default false;

	public boolean requiredChangeable() default true;

	public boolean repeatable() default true;

	public boolean repeatableChangeable() default true;

	public ArgumentStrategy argument() default ArgumentStrategy.REQUIRED;

	public boolean argumentChangeable() default true;
}
