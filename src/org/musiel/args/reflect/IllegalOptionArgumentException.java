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

import org.musiel.args.ArgumentException;
import org.musiel.args.decoder.DecoderException;

public class IllegalOptionArgumentException extends ArgumentException {

	private static final long serialVersionUID = 5695533507433718476L;

	private final String optionName;
	private final String argument;
	private final DecoderException decoderException;

	public String getOptionName() {
		return this.optionName;
	}

	public String getArgument() {
		return this.argument;
	}

	public DecoderException getDecoderException() {
		return this.decoderException;
	}

	public IllegalOptionArgumentException( final String optionName, final String argument, final DecoderException decoderException) {
		super( IllegalOptionArgumentException.class.getPackage().getName() + ".exception", IllegalOptionArgumentException.class
				.getSimpleName(), optionName, argument, decoderException.getMessage());
		this.optionName = optionName;
		this.argument = argument;
		this.decoderException = decoderException;
	}
}
