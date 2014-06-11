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

import java.util.List;

public interface DefaultAccessor {

	/**
	 * Returns whether an option occurred at least once.
	 * 
	 * @param optionName
	 * @return
	 */
	public boolean isOccurred( String optionName);

	/**
	 * Returns how many times an option occurred.
	 * 
	 * @param optionName
	 * @return
	 */
	public int getOccurrences( String optionName);

	/**
	 * Returns the option names used for an option, in the order they occurred.
	 * 
	 * @param optionName
	 * @return
	 */
	public List< String> getNames( String optionName);

	public String[] getNamesAsArray( String optionName);

	public String getName( String optionName);

	/**
	 * Returns the option-arguments of an option, in the order they occurred. Occurrences without arguments produce <code>null</code>s.
	 * 
	 * @param optionName
	 * @return
	 */
	public List< String> getArguments( String optionName);

	public String[] getArgumentsAsArray( String optionName);

	/**
	 * Returns the option-argument in the only occurrence of an option, or <code>null</code> if the option never occurred, or not with an
	 * argument.
	 * 
	 * @param optionName
	 * @return
	 */
	public String getArgument( String optionName);

	/**
	 * Returns all operands.
	 * 
	 * @return
	 */
	public List< String> getOperands();

	public String[] getOperandsAsArray();

	public List< String> getOperands( String operandName);

	public String[] getOperandsAsArray( String operandName);

	public String getOperand( String operandName);
}
