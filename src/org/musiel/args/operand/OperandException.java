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
package org.musiel.args.operand;

import org.musiel.args.ArgumentException;

public class OperandException extends ArgumentException {

	private static final long serialVersionUID = 8158165952327548121L;

	public static enum Reason {
		TOO_MANY, TOO_FEW
	}

	private final Reason reason;

	public Reason getReason() {
		return this.reason;
	}

	public OperandException( final Reason reason) {
		super( OperandException.class.getPackage().getName() + ".exceptions", OperandException.class.getSimpleName() + "." + reason.name());
		this.reason = reason;
	}
}
