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


/**
 * Thrown on user errors related to a specific syntax, such as illegal option names, or totally corrupted arguments that the syntax cannot
 * proceed with parsing. Errors like unknown-options or missing option-argument should not extend this class, use
 * {@link ArgumentException} instead for those purposes.
 * 
 * @author Bagana
 */
public class SyntaxException extends ParserException {

	private static final long serialVersionUID = 4159000998122389591L;

	public SyntaxException( final String message) {
		super( message);
	}

	public SyntaxException( final String messageBundleBase, final String messageKey, final Object... messageParameters) {
		super( messageBundleBase, messageKey, messageParameters);
	}
}
