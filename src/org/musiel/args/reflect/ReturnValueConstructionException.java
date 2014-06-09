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

import org.musiel.args.decoder.DecoderException;

class ReturnValueConstructionException extends Exception {

	private static final long serialVersionUID = -876782689388935256L;

	private final int index;
	private final DecoderException cause;

	public int getIndex() {
		return this.index;
	}

	@ Override
	public DecoderException getCause() {
		return this.cause;
	}

	public ReturnValueConstructionException( final int index, final DecoderException cause) {
		super( cause.getMessage(), cause);
		this.index = index;
		this.cause = cause;
	}
}
