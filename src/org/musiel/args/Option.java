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

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A command line option. Could represent a POSIX short option, a GNU long option, a Windows slash-led option, or one of any other kind.
 * 
 * @author Bagana
 */
public interface Option {

	/**
	 * The primary name (since an option can have more than one names) of the option, typically used for help messages or exception
	 * messages. This method MUST return an element of what {@link #getNames()} would return, and is never <code>null</code>.
	 * 
	 * @return
	 */
	public String getName();

	/**
	 * All names for this option. MUST NOT be empty. It is preferable to use a set implementation that keeps certain order, like
	 * {@link LinkedHashSet}, since help message printers might construct a name list according to it.
	 * 
	 * @return
	 */
	public Set< String> getNames();

	/**
	 * Indicates whether this option must occur at least once (under any name of its) in an argument array.
	 * 
	 * @return
	 */
	public boolean isRequired();

	/**
	 * Indicates whether this option can occur more than once (possibly under different names) in an argument array.
	 * 
	 * @return
	 */
	public boolean isRepeatable();

	/**
	 * Returns the argument policy. Not <code>null</code>.
	 * 
	 * @return
	 */
	public ArgumentPolicy getArgumentPolicy();
}
