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

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

@ Retention( RetentionPolicy.RUNTIME)
@ Target( ElementType.METHOD)
@ Inherited
@ DecoderAnnotation( BooleanValue.Decoder.class)
public @ interface BooleanValue {

	public boolean caseSensitive() default false;

	public String[] trueValues() default { "TRUE", "YES", "T", "Y"};

	public String[] falseValues() default { "FALSE", "NO", "F", "N"};

	static class Decoder implements org.musiel.args.reflect.Decoder< Boolean> {

		private final boolean caseSensitive;
		private final Set< String> trueValues = new TreeSet<>();
		private final Set< String> falseValues = new TreeSet<>();

		public Decoder( final BooleanValue annotation) {
			this.caseSensitive = annotation.caseSensitive();
			for( final String trueValue: annotation.trueValues())
				this.trueValues.add( this.caseSensitive? trueValue: trueValue.toUpperCase());
			for( final String falseValue: annotation.falseValues())
				this.falseValues.add( this.caseSensitive? falseValue: falseValue.toUpperCase());
			final HashSet< String> intersection = new HashSet<>( this.trueValues);
			intersection.retainAll( this.falseValues);
			if( !intersection.isEmpty())
				throw new IllegalArgumentException( "common elements exist in trueValues and falseValue: " + intersection);
		}

		public Decoder() {
			this.caseSensitive = false;
			this.trueValues.add( "TRUE");
			this.trueValues.add( "YES");
			this.falseValues.add( "FALSE");
			this.falseValues.add( "NO");
		}

		@ Override
		public Boolean decode( final String string) throws DecoderException {
			final String stringToSearch = this.caseSensitive? string: string.toUpperCase();
			if( this.trueValues.contains( stringToSearch))
				return Boolean.TRUE;
			if( this.falseValues.contains( stringToSearch))
				return Boolean.FALSE;
			throw new DecoderException( "not a boolean value: " + string);
		}
	}
}
