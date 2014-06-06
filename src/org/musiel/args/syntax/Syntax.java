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
	public ParseResult parse( Set< Option> options, String... args) throws ParserException;

	public static interface ParseResult {

		public List< String> getOptionNames( Option option);

		public List< String> getOptionArguments( Option option);

		public List< String> getOperands();
	}
}
