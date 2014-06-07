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

public interface Result {

	/**
	 * Returns whether an option occurred at least once.
	 * 
	 * @param option
	 * @return
	 */
	public boolean isOccurred( String option);

	/**
	 * Returns how many times an option occurred.
	 * 
	 * @param option
	 * @return
	 */
	public int getOccurrences( String option);

	/**
	 * Returns the option names used for an option, in the order they occurred.
	 * 
	 * @param option
	 * @return
	 */
	public List< String> getNames( String option);

	/**
	 * Returns the option-arguments of an option, in the order they occurred. Occurrences without arguments produce <code>null</code>s.
	 * 
	 * @param option
	 * @return
	 */
	public List< String> getArguments( String option);

	/**
	 * Returns the option-argument in the first occurrence of an option, or <code>null</code> if the option never occurred, or not with an
	 * argument in the first occurrence.
	 * 
	 * @param option
	 * @return
	 */
	public String getFirstArgument( String option);

	/**
	 * Returns the option-argument in the last occurrence of an option, or <code>null</code> if the option never occurred, or not with an
	 * argument in the last occurrence.
	 * 
	 * @param option
	 * @return
	 */
	public String getLastArgument( String option);

	/**
	 * Returns all operands.
	 * 
	 * @return
	 */
	public List< String> getOperands();

	public List< String> getOperands( String operandName);

	public String getFirstOperand( String operandName);

	public String getLastOperand( String operandName);
}
