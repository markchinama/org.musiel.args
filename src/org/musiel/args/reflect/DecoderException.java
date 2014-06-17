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

import java.util.Arrays;
import java.util.Locale;

import org.musiel.args.ArgumentException;

public class DecoderException extends ArgumentException {

	private static final long serialVersionUID = 7996264514095528394L;

	private final DecoderException cause;

	public DecoderException( final String message) {
		super( message);
		this.cause = null;
	}

	public DecoderException( final String messageBundleBase, final String messageKey, final Object... messageParameters) {
		super( messageBundleBase, messageKey, messageParameters);
		this.cause = null;
	}

	public DecoderException( final DecoderException cause, final String message) {
		super( message + ": " + cause.getMessage());
		this.cause = cause;
	}

	public DecoderException( final DecoderException cause, final String messageBundleBase, final String messageKey,
			final Object... messageParameters) {
		super( messageBundleBase, messageKey, messageParameters);
		this.cause = cause;
	}

	@ Override
	protected String[] getLocalizedParameters( final Locale locale) {
		if( this.cause == null)
			return super.getLocalizedParameters( locale);
		final String[] params = super.getLocalizedParameters( locale);
		final String[] causeAdded = Arrays.copyOf( params, params.length + 1);
		causeAdded[ causeAdded.length - 1] = this.cause.getMessage( locale);
		return causeAdded;
	}
}
