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

import java.util.Collection;

import org.musiel.args.ParserException;

public class SyntaxException extends ParserException {

	private static final long serialVersionUID = -3819801566549584517L;

	public static enum Reason {

		TOO_MANY_OCCURRENCES {

			@ Override
			protected String composeMessage( final String optionName, final Collection< String> additionalOptionNames) {
				return "option " + ParserException.optionNamesToString( optionName, additionalOptionNames) + " is specified more than once";
			}
		},

		ARGUMENT_REQUIRED {

			@ Override
			protected String composeMessage( final String optionName, final Collection< String> additionalOptionNames) {
				return "option " + optionName + " requires an argument";
			}
		},

		UNEXPECTED_ARGUMENT {

			@ Override
			protected String composeMessage( final String optionName, final Collection< String> additionalOptionNames) {
				return "option " + optionName + " does not accept an argument";
			}
		},

		LATE_OPTION {

			@ Override
			protected String composeMessage( final String optionName, final Collection< String> additionalOptionNames) {
				return "options must precede operands: " + optionName;
			}
		},

		UNKNOWN_OPTION {

			@ Override
			protected String composeMessage( final String optionName, final Collection< String> additionalOptionNames) {
				return "unknown option: " + optionName;
			}
		},

		AMBIGUOUS {

			@ Override
			protected String composeMessage( final String optionName, final Collection< String> additionalOptionNames) {
				return "ambiguous option name: " + optionName;
			}
		},

		MISSING_OPTION {

			@ Override
			protected String composeMessage( final String optionName, final Collection< String> additionalOptionNames) {
				return "option " + optionName + " missing";
			}
		},

		;

		protected abstract String composeMessage( String optionName, Collection< String> additionalOptionNames);
	}

	private final Reason reason;

	public Reason getReason() {
		return this.reason;
	}

	public SyntaxException( final Reason reason, final String optionName) {
		super( reason.composeMessage( optionName, null), optionName, null);
		this.reason = reason;
	}

	public SyntaxException( final Reason reason, final String optionName, final Collection< String> additionalOptionNames) {
		super( reason.composeMessage( optionName, additionalOptionNames), optionName, additionalOptionNames);
		this.reason = reason;
	}

	public SyntaxException( final String message, final String optionName) {
		super( message, optionName, null);
		this.reason = null;
	}

	public SyntaxException( final String message, final String optionName, final Collection< String> additionalOptionNames) {
		super( message, optionName, additionalOptionNames);
		this.reason = null;
	}
}
