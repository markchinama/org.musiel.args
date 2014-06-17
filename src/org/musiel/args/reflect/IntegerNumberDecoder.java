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

import java.math.BigInteger;

abstract class IntegerNumberDecoder< T> implements Decoder< T> {

	private final int radix;
	private final BigInteger min;
	private final BigInteger max;
	private final String rangeInString;

	protected IntegerNumberDecoder( final int radix, final String min, final String max) {
		super();
		if( radix < 2 || radix > 36)
			throw new IllegalArgumentException( "radix out of range");
		this.radix = radix;
		this.min = min == null || min.equals( "")? null: new BigInteger( min);
		this.max = max == null || max.equals( "")? null: new BigInteger( max);
		this.rangeInString = ( this.min != null? "[" + this.min: "(-∞") + ", " + ( this.max != null? this.max + "]": "+∞)");
	}

	@ Override
	public T decode( final String string) throws DecoderException {
		try {
			final BigInteger decoded = new BigInteger( string, this.radix);
			if( this.min != null && decoded.compareTo( this.min) < 0 || this.max != null && decoded.compareTo( this.max) > 0)
				throw new DecoderException( IntegerNumberDecoder.class.getPackage().getName() + ".exceptions", "constraint",
						"value out of range " + this.rangeInString, string);
			return this.cast( decoded);
		} catch( final NumberFormatException formatException) {
			throw new DecoderException( IntegerNumberDecoder.class.getPackage().getName() + ".exceptions", "invalid-value",
					"an integer number", string);
		}
	}

	protected abstract T cast( BigInteger decoded);
}
