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

public class ValueException extends ArgumentException {

	private static final long serialVersionUID = 7128732326624057254L;

	public static enum OptionOrOperand {
		OPTION, OPERAND;
	}

	private final OptionOrOperand optionOrOperand;
	private final String name;
	private final String value;
	private final DecoderException decoderException;

	public boolean isOption() {
		return OptionOrOperand.OPTION.equals( this.optionOrOperand);
	}

	public boolean isOperand() {
		return OptionOrOperand.OPERAND.equals( this.optionOrOperand);
	}

	public String getName() {
		return this.name;
	}

	public String getValue() {
		return this.value;
	}

	public DecoderException getDecoderException() {
		return this.decoderException;
	}

	public ValueException( final String operandValue, final DecoderException decoderException) {
		super( ValueException.class.getPackage().getName() + ".exception", ValueException.class.getSimpleName()
				+ ".operand.unnamed", operandValue, decoderException.getMessage());
		this.optionOrOperand = OptionOrOperand.OPERAND;
		this.name = null;
		this.value = operandValue;
		this.decoderException = decoderException;
	}

	public ValueException( final OptionOrOperand optionOrOperand, final String name, final String value,
			final DecoderException decoderException) {
		super( ValueException.class.getPackage().getName() + ".exception", ValueException.class.getSimpleName()
				+ ( OptionOrOperand.OPTION.equals( optionOrOperand)? ".option": ".operand.named"), name, value, decoderException.getMessage());
		if( optionOrOperand == null)
			throw new NullPointerException();
		this.optionOrOperand = optionOrOperand;
		this.name = name;
		this.value = value;
		this.decoderException = decoderException;
	}
}
