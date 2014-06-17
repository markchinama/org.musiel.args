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

import org.musiel.args.generic.GenericParser;
import org.musiel.args.generic.GenericResult;
import org.musiel.args.reflect.ReflectParser;

/**
 * Parsing result can be represented in any model. This interface is just one example design. {@link GenericParser} uses it as the parsing
 * result accessor, via {@link GenericResult} who implements this interface. While {@link ReflectParser} gives it special meaning. See
 * their JavaDoc for detail.
 * 
 * <p>
 * Return values of this interface might be incorrect or even not meet the requirements of the options (required, argument policy, etc),
 * if any input error had occurred. Precisely under what conditions it is guaranteed that the results are correct should be clearly
 * documented by any class that provides instances of this interface.
 * </p>
 * 
 * @see GenericParser
 * @see GenericResult
 * @see ReflectParser
 * @author Bagana
 */
public interface DefaultAccessor {

	/**
	 * Returns whether an option occurred at least once.
	 * 
	 * <p>
	 * Using any alias of the option, if any, is equivalent. Thus, the specified option name itself might not have occurred.
	 * </p>
	 * 
	 * @param optionName
	 * @return
	 */
	public boolean isOccurred( String optionName);

	/**
	 * Returns how many times an option occurred.
	 * 
	 * <p>
	 * Using any alias of the option, if any, is equivalent. Thus, the specified option name itself might not have occurred.
	 * </p>
	 * 
	 * @param optionName
	 * @return
	 */
	public int getOccurrences( String optionName);

	/**
	 * Returns the option names used for an option, in the order they occurred.
	 * 
	 * <p>
	 * Using any alias of the option, if any, is equivalent. Thus, the specified option name itself might not have occurred.
	 * </p>
	 * 
	 * @param optionName
	 * @return
	 */
	public List< String> getNames( String optionName);

	/**
	 * An equivalent of {@link #getNames(String)}, with different return type.
	 * 
	 * @param optionName
	 * @return
	 */
	public String[] getNamesAsArray( String optionName);

	/**
	 * Returns the first element of {@link #getNames(String)}, or <code>null</code> if that method would return an empty list.
	 * 
	 * @param optionName
	 * @return
	 */
	public String getName( String optionName);

	/**
	 * Returns the option-arguments of an option, in the order they occurred. Occurrences without arguments produce <code>null</code>s.
	 * 
	 * <p>
	 * Using any alias of the option, if any, is equivalent. Thus, the specified option name itself might not have occurred.
	 * </p>
	 * 
	 * @param optionName
	 * @return
	 */
	public List< String> getArguments( String optionName);

	/**
	 * An equivalent of {@link #getArguments(String)}, with different return type.
	 * 
	 * @param optionName
	 * @return
	 */
	public String[] getArgumentsAsArray( String optionName);

	/**
	 * Returns the first element of {@link #getArguments(String)}, or <code>null</code> if the method would return an empty list.
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

	/**
	 * An equivalent of {@link #getOperands()}, with different return type.
	 * 
	 * @return
	 */
	public String[] getOperandsAsArray();

	/**
	 * Returns the first element of {@link #getOperands()}, or <code>null</code> if the method would return an empty list.
	 * 
	 * @return
	 */
	public String getOperand();

	/**
	 * Returns the operands for the specified name.
	 * 
	 * @param operandName
	 * @return
	 */
	public List< String> getOperands( String operandName);

	/**
	 * An equivalent of {@link #getOperands(String)}, with different return type.
	 * 
	 * @param operandName
	 * @return
	 */
	public String[] getOperandsAsArray( String operandName);

	/**
	 * Returns the first element of {@link #getOperands()}, or <code>null</code> if the method would return an empty list.
	 * 
	 * @param operandName
	 * @return
	 */
	public String getOperand( String operandName);
}
