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
import java.util.List;
import java.util.Set;

import org.musiel.args.Option;
import org.musiel.args.ParserException;

/**
 * Many different kinds of syntax for command line arguments exist out there, a programmer may prefer one over another, or may want
 * his/her own unique syntax for the program being written. This is supported by abstracting the syntax by this interface and leaving the
 * variety for concrete implementations.
 * 
 * @author Bagana
 */
public interface Syntax {

	/**
	 * Checks whether an {@link Option} is supported by this syntax, and throws an exception if it is not.
	 * 
	 * @param option
	 * @throws IllegalArgumentException
	 */
	public void validate( Option option) throws IllegalArgumentException;

	/**
	 * Parses an argument array and returns the result.
	 * 
	 * <p>
	 * Options MUST BE verified by {@link #validate(Option)} before calling this method. An implementation, though, may test it again in
	 * the method, and throw a runtime exception ({@link IllegalArgumentException} preferred).
	 * </p>
	 * 
	 * @param options
	 * @param args
	 * @return
	 * @throws ParserException
	 */
	public SyntaxResult parse( Set< Option> options, String... args);

	/**
	 * The result of a parsing process.
	 * 
	 * <p>
	 * Note that when requesting an argument list for an option, arguments specified for all its aliases are returned. To get only those
	 * for a particular alias, one has to filter by his/herself according to {@link #getNames(Option)}.
	 * </p>
	 * 
	 * @author Bagana
	 */
	public static interface SyntaxResult {

		/**
		 * Returns the errors encountered during the parsing process.
		 * 
		 * @return
		 */
		public Collection< ? extends SyntaxException> getErrors();

		/**
		 * Returns the option names used for an option, in the order they occurred.
		 * 
		 * <p>
		 * Note that it may not comply the requirements of corresponding {@link Option}'s, for example, it may be an empty list for a
		 * required option, or a list with multiple elements for a non-repeatable option, in which cases there must be at least one error in
		 * {@link #getErrors()}. If {@link #getErrors()} returns an empty collection, this result is guaranteed to be compliant with the
		 * requirements.
		 * </p>
		 * 
		 * <p>
		 * Options NOT defined in the option set are also possible to be retrieved, in which case at least one error should have been added
		 * into {@link #getErrors()}.
		 * </p>
		 * 
		 * @param option
		 * @return
		 */
		public List< String> getNames( String option);

		/**
		 * Returns the option-arguments of an option, in the order they occurred. Occurrences without arguments produce <code>null</code>s.
		 * 
		 * <p>
		 * Note that it may not comply the requirements of corresponding {@link Option}'s, for example, it may be an empty list for a
		 * required option, or a list with multiple elements for a non-repeatable option, in which cases there must be at least one error in
		 * {@link #getErrors()}. If {@link #getErrors()} returns an empty collection, this result is guaranteed to be compliant with the
		 * requirements.
		 * </p>
		 * 
		 * <p>
		 * Options NOT defined in the option set are also possible to be retrieved, in which case at least one error should have been added
		 * into {@link #getErrors()}.
		 * </p>
		 * 
		 * @param option
		 * @return
		 */
		public List< String> getArguments( String option);

		/**
		 * Returns all operands.
		 * 
		 * @return
		 */
		public List< String> getOperands();
	}
}
