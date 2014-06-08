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
package org.musiel.args.printer;

import java.io.PrintStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MonoTermPrinter {

	protected int width( final String text) {
		return text.length();
	}

	protected String substringNoLongerThan( final String text, final int limit) {
		return text.substring( 0, limit);
	}

	protected String spacesNoLongerThan( final int limit) {
		return new String( new char[ limit]).replace( '\0', ' ');
	}

	private final PrintStream out;
	private final int margin;
	private int cursor = 0;

	public MonoTermPrinter( final PrintStream out, final int margin, final int cursor) {
		super();
		this.out = out;
		this.margin = margin;
		this.cursor = cursor;
	}

	private MonoTermPrinter breakLine() {
		this.out.println();
		this.cursor = 0;
		return this;
	}

	private MonoTermPrinter printInLine( final String text) {
		this.out.print( text);
		this.cursor += this.width( text);
		return this;
	}

	private MonoTermPrinter forwardInLine( final int cols) {
		return this.printInLine( this.spacesNoLongerThan( cols));
	}

	private MonoTermPrinter breakAndIndentIfAtEOL( final int indent) {
		if( this.cursor >= this.margin)
			this.breakLine();
		return this.forwardInLine( indent);
	}

	public MonoTermPrinter forwardTo( final int cursor) {
		if( cursor < 0 || cursor >= this.margin)
			throw new IllegalArgumentException();
		if( this.cursor > cursor)
			this.breakLine();
		this.forwardInLine( cursor - this.cursor);
		return this;
	}

	private static final Pattern BREAK_POINT_FINDER = Pattern.compile( "^(?:\\S+|\\s+)");

	public void print( String text, final int indent) {
		if( indent < 0 || indent >= this.margin)
			throw new IllegalArgumentException();
		for( Matcher matcher = MonoTermPrinter.BREAK_POINT_FINDER.matcher( text); matcher.find(); text =
				text.substring( matcher.group().length()), matcher = MonoTermPrinter.BREAK_POINT_FINDER.matcher( text)) {
			String breakable = matcher.group();
			int width = this.width( breakable);
			if( this.cursor + width <= this.margin)
				this.printInLine( breakable);
			else if( indent + width <= this.margin)
				this.breakLine().forwardInLine( indent).printInLine( breakable);
			else
				for( String part; width > 0; breakable = breakable.substring( part.length()), width = this.width( breakable)) {
					this.breakAndIndentIfAtEOL( indent);
					part = this.substringNoLongerThan( breakable, this.margin - this.cursor);
					this.printInLine( part);
				}
		}
	}

	public void println( final int indent) {
		if( indent < 0 || indent >= this.margin)
			throw new IllegalArgumentException();
		this.breakLine().forwardInLine( indent);
	}
}
