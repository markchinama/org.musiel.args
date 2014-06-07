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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.musiel.args.Option;
import org.musiel.args.syntax.SyntaxException.Reason;

/**
 * A {@link Syntax} implementation compliant with the GNU <a
 * href="http://www.gnu.org/software/libc/manual/html_node/Argument-Syntax.html">Program Argument Syntax Conventions</a>.
 * 
 * <p>
 * The Conventions page mentioned above may contain some ambiguity, which is clarified here:
 * <ol>
 * <li>When multiple options are included in a single argument, the last one MAY take an argument.</li>
 * <li>An optional option-argument, if supported at all, MUST follow immediately after the option name in the same argument string.</li>
 * <li>The first argument "--" terminates all options, ONLY when it is not an option-argument.</li>
 * <li>If a long option name happens to be the starting part of another, the latter cannot be abbreviated to the former (an exact match
 * takes precedence).</li>
 * </ol>
 * </p>
 * 
 * <p>
 * The GNU implementations, by default, allow options to be specified after some of the operands, this implementation chooses the same
 * default behavior. This means that the default value for {@link #setLateOptionsAllowed(boolean)} is different from its super class
 * {@link PosixSyntax}.
 * </p>
 * 
 * <p>
 * Some programmers MIGHT think option name abbreviation a trouble making feature, it can be disabled by
 * {@link #setAbbreviationAllowed(boolean)}.
 * </p>
 * 
 * <p>
 * {@link #setOptionalArgumentsAllowed(boolean)} also controls whether optional option-arguments for long options are permitted.
 * </p>
 * 
 * @author Bagana
 */
public class GnuSyntax extends PosixSyntax {

	@ Override
	public GnuSyntax setOptionalArgumentsAllowed( final boolean optionalArgumentsAllowed) {
		super.setOptionalArgumentsAllowed( optionalArgumentsAllowed);
		return this;
	}

	@ Override
	public GnuSyntax setJointArgumentsAllowed( final boolean jointArgumentAllowed) {
		super.setJointArgumentsAllowed( jointArgumentAllowed);
		return this;
	}

	@ Override
	public GnuSyntax setLateOptionsAllowed( final boolean lateOptionsAllowed) {
		super.setLateOptionsAllowed( lateOptionsAllowed);
		return this;
	}

	private boolean abbreviationAllowed = true;

	public boolean isAbbreviationAllowed() {
		return this.abbreviationAllowed;
	}

	public GnuSyntax setAbbreviationAllowed( final boolean abbreviationAllowed) {
		this.abbreviationAllowed = abbreviationAllowed;
		return this;
	}

	public GnuSyntax() {
		this.setLateOptionsAllowed( true);
	}

	private static final Pattern OPTION_NAME_PATTERN = Pattern.compile( "^\\-(?:[A-Za-z0-9]|\\-[A-Za-z0-9\\-]+)$");

	@ Override
	protected void validateName( final String name) throws IllegalArgumentException {
		if( !GnuSyntax.OPTION_NAME_PATTERN.matcher( name).find())
			throw new IllegalArgumentException( "\"" + name + "\" is not a valid GNU option name");
	}

	@ Override
	protected PosixMachine newMachine( final Set< Option> options) {
		return new GnuMachine( options);
	}

	private static final Pattern LONG_OPTION_PATTERN = Pattern.compile( "^(\\-\\-[A-Za-z0-9\\-]+)(?:\\=(.*))?$");

	protected class GnuMachine extends PosixMachine {

		public GnuMachine( final Set< Option> options) {
			super( options);
		}

		@ Override
		protected void handleOptionArg( final String arg) throws SyntaxException {
			if( arg.startsWith( "--"))
				this.handleLongOptionArg( arg);
			else
				this.handleShortOptionArg( arg);
		}

		private void handleLongOptionArg( final String arg) throws SyntaxException {
			final Matcher matcher = GnuSyntax.LONG_OPTION_PATTERN.matcher( arg);
			if( !matcher.find())
				throw new SyntaxException( Reason.UNKNOWN_OPTION, arg);
			String name = matcher.group( 1);
			Option option = this.optionDictionary.get( name);
			if( option == null && GnuSyntax.this.isAbbreviationAllowed())
				option = this.optionDictionary.get( name = this.findAbbreviatedName( name));
			if( option == null)
				throw new SyntaxException( Reason.UNKNOWN_OPTION, name);

			final String argument = matcher.group( 2);
			if( argument != null)
				this.push( option, name, argument);
			else if( !option.isArgumentRequired())
				this.push( option, name);
			else {
				this.openOption = option;
				this.openOptionName = name;
			}
		}

		private String findAbbreviatedName( final String name) throws SyntaxException {
			String candidate = null;
			for( final String namePossible: this.optionDictionary.keySet())
				if( namePossible.startsWith( name))
					if( candidate == null)
						candidate = namePossible;
					else
						throw new SyntaxException( Reason.AMBIGUOUS, name);
			if( candidate == null)
				throw new SyntaxException( Reason.UNKNOWN_OPTION, name);
			return candidate;
		}
	}
}
