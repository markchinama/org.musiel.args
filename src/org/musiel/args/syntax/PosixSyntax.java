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

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.musiel.args.Option;
import org.musiel.args.ParserException;
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
	public ParseResult parse( final Set< Option> options, final String... args) throws ParserException {
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
			for( final Option option: options) {
				PosixSyntax.this.validate( option);
				for( final String name: option.getNames())
					if( this.optionDictionary.containsKey( name))
						throw new IllegalArgumentException( "duplicate option name: " + name);
					else
						this.optionDictionary.put( name, option);
				this.optionNames.put( option, new LinkedList< String>());
				this.optionArguments.put( option, new LinkedList< String>());
			}
		}

		private void pushInCommon( final Option option, final String optionName, final String optionArgument) throws SyntaxException {
			if( !option.getNames().contains( optionName))
				throw new IllegalArgumentException( optionName + " is not a name for this option");
			final List< String> names = this.optionNames.get( option);
			final List< String> arguments = this.optionArguments.get( option);
			if( !option.isRepeatable() && !names.isEmpty())
				throw new SyntaxException( Reason.TOO_MANY_OCCURRENCES, optionName, names);
			names.add( optionName);
			arguments.add( optionArgument);
		}

		/**
		 * Records an occurrence WITHOUT an argument. If the option requires an argument, or it is not repeatable and this is not the first
		 * push, a {@link ParserException} is thrown.
		 * 
		 * <p>
		 * If the defining option does not have the specified name, an {@link IllegalArgumentException} is thrown.
		 * </p>
		 * 
		 * @param name
		 * @return
		 * @throws SyntaxException
		 * @throws ParserException
		 */
		protected void push( final Option option, final String optionName) throws SyntaxException {
			if( option.isArgumentRequired())
				throw new SyntaxException( Reason.ARGUMENT_REQUIRED, optionName);
			this.pushInCommon( option, optionName, null);
		}

		/**
		 * Records an occurrence WITH an argument. If the option does not accept an argument, or it is not repeatable and this is not the
		 * first push, a {@link ParserException} is thrown.
		 * 
		 * <p>
		 * If the defining option does not have the specified name, an {@link IllegalArgumentException} is thrown. If the given argument is
		 * <code>null</code>, a {@link NullPointerException} is thrown.
		 * </p>
		 * 
		 * @param name
		 * @param argument
		 * @return
		 * @throws SyntaxException
		 * @throws ParserException
		 */
		protected void push( final Option option, final String optionName, final String optionArgument) throws SyntaxException {
			if( optionArgument == null)
				throw new NullPointerException( "value must not be null");
			if( !option.isArgumentAccepted())
				throw new SyntaxException( Reason.UNEXPECTED_ARGUMENT, optionName);
			this.pushInCommon( option, optionName, optionArgument);
		}

		private boolean optionTerminatedByDoubleHyphen = false;
		protected Option openOption = null; // if not null, it must require arguments (or it should be pushed in the first place)
		protected String openOptionName = null;

		private void feed( final String arg) throws ParserException {
			if( this.optionTerminatedByDoubleHyphen)
				this.operands.add( arg);
			else if( this.openOptionName != null) {
				this.push( this.openOption, this.openOptionName, arg);
				this.openOption = null;
				this.openOptionName = null;
			} else if( "--".equals( arg))
				this.optionTerminatedByDoubleHyphen = true;
			else if( !arg.startsWith( "-") || arg.equals( "-"))
				this.operands.add( arg);
			else if( !PosixSyntax.this.isLateOptionsAllowed() && !this.operands.isEmpty())
				throw new SyntaxException( Reason.LATE_OPTION, arg);
			else
				this.handleOptionArg( arg);
		}

		// specially prepared for GNU and those support different types of options...
		protected void handleOptionArg( final String arg) throws ParserException {
			this.handleShortOptionArg( arg);
		}

		protected void handleShortOptionArg( final String arg) throws SyntaxException {
			final String firstOptionName = arg.substring( 0, 2); // long enough always
			final Option option = this.optionDictionary.get( firstOptionName);
			if( option == null)
				throw new SyntaxException( Reason.UNKNOWN_OPTION, firstOptionName);

			if( !option.isArgumentAccepted()) {
				this.push( option, firstOptionName);
				if( arg.length() > 2)
					this.handleShortOptionArg( "-" + arg.substring( 2));
			} else if( arg.length() == 2)
				if( option.isArgumentRequired()) {
					this.openOption = option;
					this.openOptionName = firstOptionName;
				} else
					this.push( option, firstOptionName);
			else if( PosixSyntax.this.isJointArgumentsAllowed())
				this.push( option, firstOptionName, arg.substring( 2));
			else
				// joint not allowed => optional not allowed; it accepts => it requires => always throws exception here
				throw new SyntaxException( Reason.ARGUMENT_REQUIRED, firstOptionName);
		}

		public void build() throws SyntaxException {
			if( this.openOptionName != null)
				throw new SyntaxException( Reason.ARGUMENT_REQUIRED, this.openOptionName);
		}
	}
}
