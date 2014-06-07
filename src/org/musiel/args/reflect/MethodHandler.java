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

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.musiel.args.Result;
import org.musiel.args.operand.OperandPattern;
import org.musiel.args.reflect.Argument.ArgumentStrategy;

abstract class MethodHandler {

	public static MethodHandler forMethod( final Method method, final ReflectParser< ?> parser, final OperandPattern operandPattern) {
		final Operands operands = method.getAnnotation( Operands.class);
		final Option option = method.getAnnotation( Option.class);
		if( operands != null) {
			if( option != null)
				throw new IllegalArgumentException( Option.class.getName() + " and " + Operands.class.getName() + " cannot coexist");
			if( method.isAnnotationPresent( Required.class))
				throw new IllegalArgumentException( "operand method cannot be annotated @" + Required.class.getSimpleName());
			if( method.isAnnotationPresent( Repeatable.class))
				throw new IllegalArgumentException( "operand method cannot be annotated @" + Repeatable.class.getSimpleName());
			if( method.isAnnotationPresent( Argument.class))
				throw new IllegalArgumentException( "operand method cannot be annotated @" + Argument.class.getSimpleName());

			if( "".equals( operands.value()))
				return new OperandsHandler( method, operandPattern);
			if( operandPattern == null || !operandPattern.getNames().contains( operands.value()))
				throw new IllegalArgumentException( "operand name \"" + operands.value() + "\" does not exist in the operand pattern");
			return new OperandHandler( method, operands.value(), operandPattern);
		} else
			return new OptionHandler( method, parser);
	}

	protected final Method method;
	protected final PostProcessor postProcessor;
	protected final Expectation expectation;

	public Method getMethod() {
		return this.method;
	}

	protected MethodHandler( final Method method) {
		this.method = method;
		this.postProcessor = new PostProcessor( this.method.getReturnType());
		this.expectation = this.postProcessor.expectation;
	}

	public Object decode( final Result result) throws DecoderException {
		return this.postProcessor.decode( this.findData( result));
	}

	protected abstract List< String> findData( Result result);
}

class OptionHandler extends MethodHandler {

	private final static Pattern SHORT_NAME_APPLICABLE = Pattern.compile( "^[a-zA-Z0-9]$");
	private final static Pattern LONG_NAME_APPLICABLE = Pattern.compile( "^[a-zA-Z0-9]{2,}$");

	private static String constructName( final String methodName) {
		if( OptionHandler.SHORT_NAME_APPLICABLE.matcher( methodName).find())
			return "-" + methodName;
		if( OptionHandler.LONG_NAME_APPLICABLE.matcher( methodName).find()) {
			final StringBuilder builder = new StringBuilder().append( "--");
			for( final char c: methodName.toCharArray())
				if( c >= 'a' && c <= 'z' || c >= '0' && c <= '9')
					builder.append( c);
				else if( c >= 'A' && c <= 'Z')
					builder.append( '-').append( ( char) ( c + ( 'a' - 'A')));
			return builder.toString();
		}
		throw new IllegalArgumentException( "cannot construct an option name from method name \"" + methodName
				+ "\", add an Option annotation");
	}

	private static void throwIllegalAnnotation( final Class< ?> annotationType, final Object value, final Method method) {
		throw new IllegalArgumentException( annotationType.getSimpleName() + "(" + value + ") is not allowed for methods with return type "
				+ method.getReturnType().getName());
	}

	private final String name;

	protected OptionHandler( final Method method, final ReflectParser< ?> parser) {
		super( method);

		// names
		String[] aliases;
		final Option option = method.getAnnotation( Option.class);
		if( option != null && option.value().length > 0) {
			this.name = option.value()[ 0];
			aliases = Arrays.copyOfRange( option.value(), 1, option.value().length);
		} else {
			this.name = OptionHandler.constructName( method.getName());
			aliases = new String[]{};
		}

		// required
		boolean required = this.expectation.required();
		final Required requiredAnnotation = method.getAnnotation( Required.class);
		if( requiredAnnotation != null && requiredAnnotation.value() != required)
			if( this.expectation.requiredChangeable())
				required = requiredAnnotation.value();
			else
				OptionHandler.throwIllegalAnnotation( Required.class, requiredAnnotation.value(), method);

		// repeatable
		boolean repeatable = this.expectation.repeatable();
		final Repeatable repeatableAnnotation = method.getAnnotation( Repeatable.class);
		if( repeatableAnnotation != null && repeatableAnnotation.value() != repeatable)
			if( this.expectation.repeatableChangeable())
				repeatable = repeatableAnnotation.value();
			else
				OptionHandler.throwIllegalAnnotation( Repeatable.class, repeatableAnnotation.value(), method);

		// argument
		ArgumentStrategy argument = this.expectation.argument();
		final Argument argumentAnnotation = method.getAnnotation( Argument.class);
		if( argumentAnnotation != null && !argumentAnnotation.value().equals( argument))
			if( this.expectation.argumentChangeable())
				argument = argumentAnnotation.value();
			else
				OptionHandler.throwIllegalAnnotation( Argument.class, argumentAnnotation.value(), method);

		// register
		parser.newOption( required, repeatable, argument.accepts, argument.requires, this.name, aliases);
	}

	@ Override
	protected List< String> findData( final Result result) {
		return result.getArguments( this.name);
	}
}

abstract class AbstractOperandHandler extends MethodHandler {

	protected AbstractOperandHandler( final Method method, final boolean absentPossible, final boolean multiplePossible, final String name) {
		super( method);
		if( !this.expectation.argument().accepts && !this.expectation.argumentChangeable())
			throw new IllegalArgumentException( "methods with return type " + method.getReturnType().getName()
					+ " cannot be used for operand list");
		if( absentPossible && this.expectation.required() && !this.expectation.requiredChangeable())
			throw new IllegalArgumentException( name + " may be absent, this is not allowed by methods with return type "
					+ method.getReturnType().getName());
		if( multiplePossible && !this.expectation.repeatable() && !this.expectation.repeatableChangeable())
			throw new IllegalArgumentException( name + " may occur more than once, this is not allowed by methods with return type "
					+ method.getReturnType().getName());
	}
}

class OperandHandler extends AbstractOperandHandler {

	private final String name;

	protected OperandHandler( final Method method, final String operandName, final OperandPattern operandPattern) {
		super( method, operandPattern.isAbsencePossible( operandName), operandPattern.isMultipleOccurrencePossible( operandName),
				"operand \"" + operandName + "\"");
		this.name = operandName;
	}

	@ Override
	protected List< String> findData( final Result result) {
		return result.getOperands( this.name);
	}
}

class OperandsHandler extends AbstractOperandHandler {

	public OperandsHandler( final Method method, final OperandPattern operandPattern) {
		super( method, operandPattern.isEmptyPossible(), operandPattern.isMoreThanOneOperandsPossible(), "operands");
	}

	@ Override
	protected List< String> findData( final Result result) {
		return result.getOperands();
	}
}
