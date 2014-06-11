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
import java.util.regex.Pattern;

@ Retention( RetentionPolicy.RUNTIME)
@ Target( ElementType.METHOD)
@ Inherited
@ DecoderAnnotation( StringValue.Decoder.class)
public @ interface StringValue {

	public String pattern() default ".+";

	static class Decoder implements org.musiel.args.reflect.Decoder< String> {

		private final Pattern pattern;

		public Decoder( final StringValue annotation) {
			this.pattern = Pattern.compile( annotation.pattern());
		}

		public Decoder() {
			this.pattern = Pattern.compile( ".+");
		}

		@ Override
		public String decode( final String string) throws DecoderException {
			if( !this.pattern.matcher( string).matches())
				throw new DecoderException( "value does not match regular expression " + this.pattern + ": " + string);
			return string;
		}
	}
}
