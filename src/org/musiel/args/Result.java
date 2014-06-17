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

import java.util.Collection;

/**
 * The result of a {@link Parser#parse(String...)} invocation.
 * 
 * @author Bagana
 * 
 * @param <ACCESSOR>
 */
public interface Result< ACCESSOR> {

	/**
	 * The {@link ArgumentException}s encountered during the parsing. Empty, if no error detected.
	 * 
	 * @return
	 */
	public Collection< ? extends ArgumentException> getErrors();

	/**
	 * Searches {@link #getErrors()} for {@link ArgumentException}s of the specified types and throw them if found.
	 * 
	 * @param exceptionTypes
	 * @return
	 * @throws ArgumentExceptions
	 */
	public Result< ACCESSOR> check( Collection< Class< ? extends ArgumentException>> exceptionTypes) throws ArgumentExceptions;

	/**
	 * Searches {@link #getErrors()} for {@link ArgumentException} of the specified type and throw them if found.
	 * 
	 * @param exceptionType
	 * @return
	 * @throws ArgumentExceptions
	 */
	public Result< ACCESSOR> check( Class< ? extends ArgumentException> exceptionType) throws ArgumentExceptions;

	/**
	 * Throw all user errors wrapped in a {@link ArgumentExceptions}, or return an accessor if no error is found. The accessor returned in
	 * this way is guarantees to comply every requirements, constraints, and patterns for the options and operands.
	 * 
	 * @return
	 * @throws ArgumentExceptions
	 */
	public ACCESSOR check() throws ArgumentExceptions;

	/**
	 * Get the accessor without checking the constraints. The returned accessor may carry incorrect and/or inconsistent data.
	 * 
	 * @return
	 */
	public ACCESSOR getAccessor();
}
