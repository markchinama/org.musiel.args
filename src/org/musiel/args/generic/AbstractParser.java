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
package org.musiel.args.generic;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.musiel.args.ArgumentException;
import org.musiel.args.ArgumentPolicy;
import org.musiel.args.Option;
import org.musiel.args.Parser;
import org.musiel.args.Result;
import org.musiel.args.operand.OperandException;
import org.musiel.args.operand.OperandPattern;
import org.musiel.args.syntax.Syntax;
import org.musiel.args.syntax.Syntax.SyntaxResult;

/**
 * An abstract implementation of {@link Parser}.
 * 
 * @author Bagana
 * 
 * @param <RESULT>
 */
public abstract class AbstractParser< RESULT extends Result< ?>> implements Parser< RESULT> {

	private final Syntax syntax;

	protected AbstractParser( final Syntax syntax) {
		this.syntax = syntax;
		if( this.syntax == null)
			throw new NullPointerException();
	}

	private final Map< String, Option> optionDictionary = new TreeMap<>();
	private final Set< Option> options = new LinkedHashSet<>();

	@ Override
	public List< ? extends Option> getOptions() {
		return Collections.unmodifiableList( new LinkedList<>( this.options));
	}

	@ Override
	public Option getOption( final String name) {
		return this.optionDictionary.get( name);
	}

	protected Option newOption( final String name, final String... aliases) {
		return this.newOption( false, false, ArgumentPolicy.NONE, name, aliases);
	}

	protected Option newOption( final boolean required, final boolean repeatable, final ArgumentPolicy argumentPolicy, final String name,
			final String... aliases) {
		final Option option = new GenericOption( required, repeatable, argumentPolicy, name, aliases);
		this.syntax.validate( option);
		for( final String optionName: option.getNames())
			if( this.optionDictionary.containsKey( optionName))
				throw new IllegalArgumentException( "duplicate option name: " + optionName);

		this.options.add( option);
		for( final String optionName: option.getNames())
			this.optionDictionary.put( optionName, option);

		return option;
	}

	private OperandPattern operandPattern = null;

	protected OperandPattern getOperandPatternMatcher() {
		return this.operandPattern;
	}

	@ Override
	public String getOperandPattern() {
		return this.operandPattern == null? null: this.operandPattern.getPattern();
	}

	@ Override
	public List< String> getOperandNames() {
		return this.operandPattern == null? null: this.operandPattern.getNames();
	}

	protected void setOperandPattern( final String operandPattern) {
		this.operandPattern = operandPattern == null? null: this.compileAndTestPattern( operandPattern);
	}

	private OperandPattern compileAndTestPattern( final String operandPattern) {
		final OperandPattern compiled = OperandPattern.compile( operandPattern);
		final String[][] ambiguityExample = compiled.findAmbiguityExample();
		if( ambiguityExample == null)
			return compiled;
		final StringBuilder message =
				new StringBuilder().append( "operand pattern \"").append( operandPattern)
						.append( "\" is ambiguous, for example, if the input length is ").append( ambiguityExample[ 0].length)
						.append( ", there are at least following two interpretation: \"");
		this.append( message, ambiguityExample[ 0]);
		message.append( "\" and \"");
		this.append( message, ambiguityExample[ 0]);
		message.append( "\"");
		throw new IllegalArgumentException( message.toString());
	}

	private void append( final StringBuilder message, final String[] strings) {
		for( int index = 0; index < strings.length; ++index) {
			if( index >= 0)
				message.append( ' ');
			message.append( strings[ index]);
		}
	}

	@ Override
	public RESULT parse( final String[] args, final int offset) {
		if( offset < 0 || offset >= args.length)
			throw new ArrayIndexOutOfBoundsException( offset);
		return this.parse( args, offset, args.length - offset);
	}

	@ Override
	public RESULT parse( final String[] args, final int offset, final int length) {
		if( offset < 0 || offset >= args.length)
			throw new ArrayIndexOutOfBoundsException( offset);
		if( length < 0)
			throw new IllegalArgumentException( String.valueOf( length));
		if( offset + length > args.length)
			throw new ArrayIndexOutOfBoundsException( offset + length);
		return this.parse( Arrays.copyOfRange( args, offset, offset + length));
	}

	@ Override
	public RESULT parse( final String... args) {
		final SyntaxResult syntaxResult = this.syntax.parse( Collections.unmodifiableSet( this.options), args);
		final Collection< ArgumentException> exceptions = new LinkedList< ArgumentException>( syntaxResult.getErrors());
		Map< String, List< String>> operandMap = null;
		try {
			operandMap = this.operandPattern == null? null: this.operandPattern.match( syntaxResult.getOperands());
		} catch( final OperandException exception) {
			exceptions.add( exception);
			operandMap = new TreeMap<>();
		}

		return this.adapt( syntaxResult, operandMap, Collections.unmodifiableCollection( exceptions));
	}

	protected abstract RESULT adapt( SyntaxResult syntaxResult, Map< String, List< String>> operands,
			Collection< ? extends ArgumentException> exceptions);
}
