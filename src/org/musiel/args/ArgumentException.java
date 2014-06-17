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
 * Indicates an error from user input, such as an unknown option or an unexpected option-argument.
 * 
 * @author Bagana
 */
public abstract class ArgumentException extends Exception {

	private static final long serialVersionUID = -1088285368268583084L;

	private final boolean useResourceBundle;
	// this field is never used if useResourceBundle is true
	private final String message;
	// these fields are never used if useResourceBundle is false
	private final String messageBundleBase;
	private final String messageKey;
	private final String[] messageParameters;

	/**
	 * Construct an {@link ArgumentException} with specified cause and message. {@link #getMessage(Locale)} with any locale parameter
	 * returns the same message, that is given here.
	 * 
	 * @param cause
	 * @param message
	 */
	public ArgumentException( final Throwable cause, final String message) {
		super( cause);
		this.useResourceBundle = false;
		this.message = message;
		this.messageBundleBase = null;
		this.messageKey = null;
		this.messageParameters = null;
	}

	/**
	 * Construct an {@link ArgumentException} with specified message. {@link #getMessage(Locale)} with any locale parameter returns the
	 * same message, that is given here.
	 * 
	 * @param message
	 */
	public ArgumentException( final String message) {
		this( null, message);
	}

	/**
	 * Construct an {@link ArgumentException} with specified cause and message, which is given in resource bundle base, key, and
	 * parameters.
	 * 
	 * <p>
	 * Note that the parameters are converted to {@link String}s at construction time, if their {@link Object#toString()} values change in
	 * future, it does not affect the return value of {@link #getMessage()} or {@link #getMessage(Locale)}.
	 * </p>
	 * 
	 * <p>
	 * If one or all of the parameters need to vary according to the locale, {@link #getLocalizedParameters(Locale)} should be overridden to
	 * do the pre-processing.
	 * </p>
	 * 
	 * @param cause
	 * @param messageBundleBase
	 * @param messageKey
	 * @param messageParameters
	 */
	public ArgumentException( final Throwable cause, final String messageBundleBase, final String messageKey,
			final Object... messageParameters) {
		super( cause);
		this.useResourceBundle = true;
		this.message = null;
		this.messageBundleBase = messageBundleBase;
		this.messageKey = messageKey;
		this.messageParameters = new String[ messageParameters.length];
		for( int index = this.messageParameters.length - 1; index >= 0; --index)
			this.messageParameters[ index] = messageParameters[ index] != null? messageParameters[ index].toString(): "null";
	}

	/**
	 * Construct an {@link ArgumentException} with specified message given in resource bundle base, key, and parameters.
	 * 
	 * <p>
	 * Note that the parameters are converted to {@link String}s at construction time, if their {@link Object#toString()} values change in
	 * future, it does not affect the return value of {@link #getMessage()} or {@link #getMessage(Locale)}.
	 * </p>
	 * 
	 * <p>
	 * If one or all of the parameters need to vary according to the locale, {@link #getLocalizedParameters(Locale)} should be overridden to
	 * do the pre-processing.
	 * </p>
	 * 
	 * @param messageBundleBase
	 * @param messageKey
	 * @param messageParameters
	 */
	public ArgumentException( final String messageBundleBase, final String messageKey, final Object... messageParameters) {
		this( null, messageBundleBase, messageKey, messageParameters);
	}

	@ Override
	public String getMessage() {
		return this.getMessage( Locale.getDefault());
	}

	/**
	 * Get the error message in specified locale.
	 * 
	 * @param locale
	 * @return
	 */
	public String getMessage( final Locale locale) {
		if( !this.useResourceBundle)
			return this.message;
		if( this.messageBundleBase == null || this.messageKey == null)
			return this.getFailsafeMessage();
		try {
			return ArgumentException.substitute( ResourceBundle.getBundle( this.messageBundleBase, locale).getString( this.messageKey),
					this.getLocalizedParameters( locale));
		} catch( final MissingResourceException exception) {
			return this.getFailsafeMessage();
		}
	}

	protected String[] getLocalizedParameters( final Locale locale) {
		return this.messageParameters;
	}

	private static final Pattern SUBSTITUTION_POINT_PATTERN = Pattern.compile( "\\{[1-9]\\d*\\}");

	private static String substitute( final String template, final String... params) {
		final StringBuilder result = new StringBuilder();
		final Matcher matcher = ArgumentException.SUBSTITUTION_POINT_PATTERN.matcher( template);
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

	protected String getFailsafeMessage() {
		final StringBuilder message =
				new StringBuilder().append( '<').append( this.messageBundleBase).append( ">.").append( this.messageKey).append( '(');
		for( int index = 0; index < this.messageParameters.length; ++index) {
			if( index > 0)
				message.append( ", ");
			message.append( this.messageParameters[ index]);
		}
		message.append( ')');
		return message.toString();
	}
}
