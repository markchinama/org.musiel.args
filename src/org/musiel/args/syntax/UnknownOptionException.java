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

import org.musiel.args.ParserException;

public class UnknownOptionException extends ParserException {

	private static final long serialVersionUID = 5490220155957195567L;

	private final String optionName;

	public UnknownOptionException( final String optionName) {
		super( "unknown option: " + optionName);
		this.optionName = optionName;
	}

	public String getOptionName() {
		return this.optionName;
	}
}
