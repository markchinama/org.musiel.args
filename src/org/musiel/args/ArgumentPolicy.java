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
 * Indicates if an option can be given with or without an argument, or both.
 * 
 * @author Bagana
 */
public enum ArgumentPolicy {

	/**
	 * The option does not accept an argument
	 */
	NONE( false, false),

	/**
	 * The option accepts an argument, but not required
	 */
	OPTIONAL( true, false),

	/**
	 * The option requires an argument
	 */
	REQUIRED( true, true);

	private final boolean accepted;
	private final boolean required;

	/**
	 * Whether the option accepts an argument (optional or mandatory).
	 * 
	 * @return
	 */
	public boolean isAccepted() {
		return this.accepted;
	}

	/**
	 * Whether the option-argument is required.
	 * 
	 * @return
	 */
	public boolean isRequired() {
		return this.required;
	}

	private ArgumentPolicy( final boolean accepted, final boolean required) {
		this.accepted = accepted;
		this.required = required;
	}
}
