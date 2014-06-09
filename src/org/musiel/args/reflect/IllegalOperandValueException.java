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
package org.musiel.args.reflect;

import org.musiel.args.ArgumentException;
import org.musiel.args.decoder.DecoderException;

public class IllegalOperandValueException extends ArgumentException {

	private static final long serialVersionUID = -568983887820756856L;

	private final String operandName;
	private final String operandValue;
	private final DecoderException decoderException;

	public String getOperandName() {
		return this.operandName;
	}

	public String getOperandValue() {
		return this.operandValue;
	}

	public DecoderException getDecoderException() {
		return this.decoderException;
	}

	public IllegalOperandValueException( final String operandValue, final DecoderException decoderException) {
		super( IllegalOperandValueException.class.getPackage().getName() + ".exception", IllegalOperandValueException.class.getSimpleName()
				+ ".unnamed", operandValue, decoderException.getMessage());
		this.operandName = null;
		this.operandValue = operandValue;
		this.decoderException = decoderException;
	}

	public IllegalOperandValueException( final String operandName, final String operandValue, final DecoderException decoderException) {
		super( IllegalOperandValueException.class.getPackage().getName() + ".exception", IllegalOperandValueException.class.getSimpleName()
				+ ".named", operandName, operandValue, decoderException.getMessage());
		this.operandName = operandName;
		this.operandValue = operandValue;
		this.decoderException = decoderException;
	}
}
