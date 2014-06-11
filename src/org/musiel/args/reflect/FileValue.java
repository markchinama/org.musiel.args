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
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@ Retention( RetentionPolicy.RUNTIME)
@ Target( ElementType.METHOD)
@ Inherited
@ DecoderAnnotation( FileValue.Decoder.class)
public @ interface FileValue {

	public boolean exists() default false;

	public boolean notExists() default false;

	public boolean file() default false;

	public boolean directory() default false;

	public boolean readable() default false;

	public boolean writable() default false;

	public boolean executable() default false;

	static class Decoder implements org.musiel.args.reflect.Decoder< File> {

		private final boolean mustExist;
		private final boolean mustNotExist;
		private final boolean mustBeFile;
		private final boolean mustBeDirectory;
		private final boolean mustBeReadable;
		private final boolean mustBeWritable;
		private final boolean mustBeExecutable;

		public Decoder( final FileValue annotation) {
			this.mustExist = annotation.exists();
			this.mustNotExist = annotation.notExists();
			this.mustBeFile = annotation.file();
			this.mustBeDirectory = annotation.directory();
			this.mustBeReadable = annotation.readable();
			this.mustBeWritable = annotation.writable();
			this.mustBeExecutable = annotation.executable();
		}

		public Decoder() {
			this.mustExist = false;
			this.mustNotExist = false;
			this.mustBeFile = false;
			this.mustBeDirectory = false;
			this.mustBeReadable = false;
			this.mustBeWritable = false;
			this.mustBeExecutable = false;
		}

		@ Override
		public File decode( final String string) throws DecoderException {
			final File file = new File( string);
			if( this.mustExist && !file.exists())
				throw new DecoderException( "file " + string + " does not exist");
			if( this.mustNotExist && file.exists())
				throw new DecoderException( "file " + string + " exists");
			if( this.mustBeFile && !file.isFile())
				throw new DecoderException( "file " + string + " is not a file");
			if( this.mustBeDirectory && !file.isDirectory())
				throw new DecoderException( "file " + string + " is not a directory");
			if( this.mustBeReadable && !file.canRead())
				throw new DecoderException( "file " + string + " is not readable");
			if( this.mustBeWritable && !file.canWrite())
				throw new DecoderException( "file " + string + " is not writable");
			if( this.mustBeExecutable && !file.canExecute())
				throw new DecoderException( "file " + string + " is not executable");
			return file;
		}
	}
}
