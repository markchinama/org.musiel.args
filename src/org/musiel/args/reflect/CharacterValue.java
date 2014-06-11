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

@ Retention( RetentionPolicy.RUNTIME)
@ Target( ElementType.METHOD)
@ Inherited
@ DecoderAnnotation( CharacterValue.Decoder.class)
public @ interface CharacterValue {

	static class Decoder implements org.musiel.args.reflect.Decoder< Character> {

		public Decoder( final CharacterValue annotation) {
		}

		public Decoder() {
		}

		@ Override
		public Character decode( final String string) throws DecoderException {
			if( string.length() != 1)
				throw new DecoderException( "length != 1: " + string);
			return string.charAt( 0);
		}
	}
}
