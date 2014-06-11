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

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

public class UncheckedDecoderExceptions extends RuntimeException implements Iterable< DecoderException> {

	private static final long serialVersionUID = 7496935121105401504L;

	private final Collection< DecoderException> decoderExceptions = new LinkedList<>();

	public Collection< DecoderException> getDecoderExceptions() {
		return this.decoderExceptions;
	}

	public UncheckedDecoderExceptions( final Collection< ? extends DecoderException> exceptions) {
		this.decoderExceptions.addAll( exceptions);
	}

	@ Override
	public Iterator< DecoderException> iterator() {
		return this.decoderExceptions.iterator();
	}
}
