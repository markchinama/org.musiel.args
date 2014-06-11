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
package org.musiel.args;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User errors like missing options or unknown arguments.
 * 
 * @author Bagana
 */
public class ArgumentException extends Exception {

	private static final long serialVersionUID = 1892846941097605037L;

	private final boolean useResourceBundle;

	private final String message;

	private final String messageBundleBase;
	private final String messageKey;
	private final String[] messageParameters;

	public ArgumentException( final String message) {
		this.useResourceBundle = false;

		this.message = message;

		this.messageBundleBase = null;
		this.messageKey = null;
		this.messageParameters = null;
	}

	public ArgumentException( final String messageBundleBase, final String messageKey, final Object... messageParameters) {
		this.useResourceBundle = true;

		this.message = null;

		this.messageBundleBase = messageBundleBase;
		this.messageKey = messageKey;
		this.messageParameters = new String[ messageParameters == null? 0: messageParameters.length];
		for( int index = this.messageParameters.length - 1; index >= 0; --index)
			this.messageParameters[ index] = messageParameters[ index] == null? "null": messageParameters[ index].toString();
	}

	@ Override
	public String getMessage() {
		return this.getMessage( Locale.getDefault());
	}

	public String getMessage( final Locale locale) {
		if( !this.useResourceBundle)
			return this.message;
		if( this.messageBundleBase == null || this.messageKey == null)
			return null;
		return getMessageFromResourceBundle( this.messageBundleBase, locale, this.messageKey, this.messageParameters);
	}

	private static String getMessageFromResourceBundle( final String base, final Locale locale, final String key, final String... params) {
		try {
			final String template = ResourceBundle.getBundle( base, locale).getString( key);
			return substitute( template, params);
		} catch( final MissingResourceException exception) {
			final StringBuilder fallback = new StringBuilder().append( '<').append( base).append( ">.").append( key).append( '(');
			for( int index = 0; index < params.length; ++index) {
				if( index > 0)
					fallback.append( ", ");
				fallback.append( params[ index]);
			}
			fallback.append( ')');
			return fallback.toString();
		}
	}

	private static final Pattern SUBSTITUTION_POINT_PATTERN = Pattern.compile( "\\{[1-9]\\d*\\}");

	private static String substitute( final String template, final String... params) {
		final StringBuilder result = new StringBuilder();
		final Matcher matcher = SUBSTITUTION_POINT_PATTERN.matcher( template);
		int matched;
		for( matched = 0; matcher.find( matched); matched = matcher.end()) {
			result.append( template, matched, matcher.start());
			try {
				final int index = Integer.parseInt( matcher.group().substring( 1, matcher.group().length() - 1)) - 1;
				result.append( index >= 0 && index < params.length? params[ index]: matcher.group());
			} catch( final NumberFormatException exception) {
				result.append( matcher.group());
			}
		}
		result.append( template, matched, template.length());
		return result.toString();
	}
}
