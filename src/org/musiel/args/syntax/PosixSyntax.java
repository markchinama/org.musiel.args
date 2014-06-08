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
package org.musiel.args.syntax;

import java.util.Set;
import java.util.regex.Pattern;

import org.musiel.args.Option;
import org.musiel.args.syntax.SyntaxException.Reason;

/**
 * A {@link Syntax} implementation compliant with the <a
 * href="http://pubs.opengroup.org/onlinepubs/9699919799/basedefs/V1_chap12.html">Utility Conventions</a> chapter of <a href="">The Open
 * Group Base Specifications Issue 7 IEEE Std 1003.1™, 2013 Edition</a> (the POSIX.1-2008 standard).
 * 
 * <p>
 * <a href="http://pubs.opengroup.org/onlinepubs/9699919799/basedefs/V1_chap12.html#tag_12_02">Utility Syntax Guidelines</a> disallows
 * optional option-arguments or option-arguments joint with the option name, while <a
 * href="http://pubs.opengroup.org/onlinepubs/9699919799/basedefs/V1_chap12.html#tag_12_01">Utility Argument Syntax</a> requires an
 * implementation of some historic utilities to support them to be a conforming implementation. This class supports both strict and
 * conforming behaviors, configurable by {@link #setOptionalArgumentsAllowed(boolean)} and {@link #setJointArgumentsAllowed(boolean)},
 * both defaulting to the strict mode, encouraging new applications to follow the guidelines.
 * </p>
 * 
 * <p>
 * Configuring {@link #setOptionalArgumentsAllowed(boolean)} to <code>true</code> automatically configures
 * {@link #setJointArgumentsAllowed(boolean)} to <code>true</code>, and configuring {@link #setJointArgumentsAllowed(boolean)} to
 * <code>false</code> automatically configures {@link #setOptionalArgumentsAllowed(boolean)} to <code>false</code>.
 * </p>
 * 
 * <p>
 * The standard requires all options to precede the operands, which might sound too strict to some programmers. This restriction can be
 * disabled by {@link #setLateOptionsAllowed(boolean)}.
 * </p>
 * 
 * @author Bagana
 */
public class PosixSyntax implements Syntax {

	private boolean optionalArgumentsAllowed = false;
	private boolean jointArgumentsAllowed = false;

	public boolean isOptionalArgumentsAllowed() {
		return this.optionalArgumentsAllowed;
	}

	public boolean isJointArgumentsAllowed() {
		return this.jointArgumentsAllowed;
	}

	public PosixSyntax setOptionalArgumentsAllowed( final boolean optionalArgumentsAllowed) {
		this.optionalArgumentsAllowed = optionalArgumentsAllowed;
		if( optionalArgumentsAllowed)
			this.setJointArgumentsAllowed( true);
		return this;
	}

	public PosixSyntax setJointArgumentsAllowed( final boolean jointArgumentAllowed) {
		this.jointArgumentsAllowed = jointArgumentAllowed;
		if( !jointArgumentAllowed)
			this.setOptionalArgumentsAllowed( false);
		return this;
	}

	private boolean lateOptionsAllowed = false;

	public boolean isLateOptionsAllowed() {
		return this.lateOptionsAllowed;
	}

	public PosixSyntax setLateOptionsAllowed( final boolean lateOptionsAllowed) {
		this.lateOptionsAllowed = lateOptionsAllowed;
		return this;
	}

	@ Override
	public void validate( final Option option) throws IllegalArgumentException {
		if( !this.optionalArgumentsAllowed && option.isArgumentAccepted() && !option.isArgumentRequired())
			throw new IllegalArgumentException( "optional option-argument is not allowed (by configuration)");
		for( final String name: option.getNames())
			this.validateName( name);
	}

	private static final Pattern OPTION_NAME_PATTERN = Pattern.compile( "^\\-[A-Za-z0-9]$");

	protected void validateName( final String name) throws IllegalArgumentException {
		if( !PosixSyntax.OPTION_NAME_PATTERN.matcher( name).find())
			throw new IllegalArgumentException( "\"" + name + "\" is not a valid POSIX option name");
	}

	@ Override
	public SyntaxResult parse( final Set< Option> options, final String... args) {
		final PosixMachine machine = this.newMachine( options);
		for( final String arg: args)
			machine.feed( arg);
		machine.build();
		return machine;
	}

	protected PosixMachine newMachine( final Set< Option> options) {
		return new PosixMachine( options);
	}

	protected class PosixMachine extends AbstractParseResult {

		protected PosixMachine( final Set< Option> options) {
			super( options);
			for( final Option option: options)
				PosixSyntax.this.validate( option);
		}

		private boolean optionTerminatedByDoubleHyphen = false;
		// the name of a found-but-not-pushed option. openOption is null and non-null when the name is unknown and known, respectively.
		// if it is a known option, it must require an argument, or it should have been pushed in the first place.
		// if it is an unknown option, a hyphen-led arg pushes it without argument, other args are considered its argument.
		protected String openOptionName = null;
		protected Option openOption = null;

		private void feed( final String arg) {
			if( this.optionTerminatedByDoubleHyphen) {
				this.operands.add( arg);
				return;
			}

			if( this.openOptionName != null && ( this.openOption != null || !arg.startsWith( "-") || arg.equals( "-"))) {
				this.push( this.openOptionName, arg);
				this.openOptionName = null;
				this.openOption = null;
				return;
			}

			if( "--".equals( arg)) {
				this.optionTerminatedByDoubleHyphen = true;
				return;
			}

			if( !arg.startsWith( "-") || arg.equals( "-")) {
				this.operands.add( arg);
				return;
			}

			if( !this.operands.isEmpty() && !PosixSyntax.this.isLateOptionsAllowed())
				this.errors.add( new SyntaxException( Reason.LATE_OPTION, arg));
			this.handleOption( arg);
		}

		// specially prepared for GNU and those support different types of options...
		protected void handleOption( final String arg) {
			this.handleShortOption( arg);
		}

		protected void handleShortOption( final String arg) {
			final String firstOptionName = arg.substring( 0, 2); // long enough always
			final Option option = this.optionDictionary.get( firstOptionName);
			if( option == null)
				this.errors.add( new SyntaxException( Reason.UNKNOWN_OPTION, firstOptionName));

			if( arg.length() == 2) {
				if( option == null || option.isArgumentRequired()) {
					this.openOptionName = firstOptionName;
					this.openOption = option;
				} else
					this.push( firstOptionName, null);
				return;
			}

			if( option == null || !option.isArgumentAccepted() || !PosixSyntax.this.isJointArgumentsAllowed()) {
				this.push( firstOptionName, null);
				this.handleShortOption( "-" + arg.substring( 2));
				return;
			}

			this.push( firstOptionName, arg.substring( 2));
		}

		@ Override
		public void build() {
			if( this.openOptionName != null)
				this.push( this.openOptionName, null);
			super.build();
		}
	}
}
