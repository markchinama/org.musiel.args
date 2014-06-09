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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.musiel.args.ArgumentException;
import org.musiel.args.ArgumentPolicy;
import org.musiel.args.DefaultAccessor;
import org.musiel.args.operand.OperandPattern;
import org.musiel.args.operand.OperandPatternException;
import org.musiel.args.reflect.annotation.Argument;
import org.musiel.args.reflect.annotation.ArgumentName;
import org.musiel.args.reflect.annotation.Description;
import org.musiel.args.reflect.annotation.Operands;
import org.musiel.args.reflect.annotation.Option;
import org.musiel.args.reflect.annotation.Repeatable;
import org.musiel.args.reflect.annotation.Required;
import org.musiel.args.syntax.OptionException;

abstract class MethodHandler {

	public static MethodHandler forMethod( final Method method, final ReflectParser< ?> parser, final OperandPattern operandPattern) {
		return method.isAnnotationPresent( Operands.class)? AbstractOperandHandler.forMethod( method, operandPattern): new OptionHandler(
				method, parser);
	}

	protected final Method method;
	protected final ReturnValueConstructor valueConstructor;
	protected final Expectation expectation;

	public Method getMethod() {
		return this.method;
	}

	protected MethodHandler( final Method method) {
		this.method = method;
		this.valueConstructor = new ReturnValueConstructor( this.method.getReturnType());
		this.expectation = this.valueConstructor.expectation;
	}

	public abstract Object decode( final DefaultAccessor result, Collection< ? extends ArgumentException> parseTimeExceptions)
			throws FutureException;
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
	private final Set< String> names = new HashSet<>();

	protected OptionHandler( final Method method, final ReflectParser< ?> parser) {
		super( method);

		// names
		String[] additionalNames;
		final Option option = method.getAnnotation( Option.class);
		if( option != null && option.value().length > 0) {
			this.name = option.value()[ 0];
			Collections.addAll( this.names, option.value());
			additionalNames = option.value(); // duplicates are okay
		} else {
			this.name = OptionHandler.constructName( method.getName());
			this.names.add( this.name);
			additionalNames = new String[]{};
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
		ArgumentPolicy argument = this.expectation.argument();
		final Argument argumentAnnotation = method.getAnnotation( Argument.class);
		if( argumentAnnotation != null && !argumentAnnotation.value().equals( argument))
			if( this.expectation.argumentChangeable())
				argument = argumentAnnotation.value();
			else
				OptionHandler.throwIllegalAnnotation( Argument.class, argumentAnnotation.value(), method);

		// i18n
		final String description = method.isAnnotationPresent( Description.class)? method.getAnnotation( Description.class).value(): null;
		final String argumentName =
				method.isAnnotationPresent( ArgumentName.class)? method.getAnnotation( ArgumentName.class).value(): null;

		// register
		parser.newOption( required, repeatable, argument, this.name, additionalNames);
		if( description != null)
			parser.setOptionDescription( this.name, description);
		if( argumentName != null)
			parser.setArgumentName( this.name, argumentName);
	}

	@ Override
	public Object decode( final DefaultAccessor result, final Collection< ? extends ArgumentException> parseTimeExceptions)
			throws FutureException {
		final List< String> arguments = result.getArguments( this.name);
		try {
			return this.valueConstructor.construct( arguments);
		} catch( final PreconditionException exception) {
			final Collection< ArgumentException> possibleCauses = new LinkedList<>();
			for( final ArgumentException parseTimeException: parseTimeExceptions)
				if( parseTimeException instanceof OptionException
						&& this.names.contains( ( ( OptionException) parseTimeException).getOptionName()))
					possibleCauses.add( parseTimeException);
			throw new FutureException( possibleCauses);
		} catch( final ReturnValueConstructionException exception) {
			final List< String> names = result.getNames( this.name);
			throw new FutureException( new IllegalOptionArgumentException( names.get( exception.getIndex()), arguments.get( exception
					.getIndex()), exception.getCause()));
		}
	}
}

abstract class AbstractOperandHandler extends MethodHandler {

	private static final Set< Class< ? extends Annotation>> CONFLICT_WITH_OPERANDS = new HashSet<>();
	static {
		AbstractOperandHandler.CONFLICT_WITH_OPERANDS.add( Argument.class);
		AbstractOperandHandler.CONFLICT_WITH_OPERANDS.add( ArgumentName.class);
		AbstractOperandHandler.CONFLICT_WITH_OPERANDS.add( Description.class);
		AbstractOperandHandler.CONFLICT_WITH_OPERANDS.add( Option.class);
		AbstractOperandHandler.CONFLICT_WITH_OPERANDS.add( Repeatable.class);
		AbstractOperandHandler.CONFLICT_WITH_OPERANDS.add( Required.class);
	}

	public static MethodHandler forMethod( final Method method, final OperandPattern operandPattern) {
		for( final Class< ? extends Annotation> conflictingAnnotations: AbstractOperandHandler.CONFLICT_WITH_OPERANDS)
			if( method.isAnnotationPresent( conflictingAnnotations))
				throw new IllegalArgumentException( "operand method cannot be annotated @" + conflictingAnnotations.getSimpleName());

		final String operandName = method.getAnnotation( Operands.class).value();
		if( "".equals( operandName))
			return new OperandsHandler( method, operandPattern);
		if( operandPattern == null || !operandPattern.getNames().contains( operandName))
			throw new IllegalArgumentException( "operand name \"" + operandName + "\" does not exist in the operand pattern");
		return new OperandHandler( method, operandName, operandPattern);
	}

	protected AbstractOperandHandler( final Method method, final boolean absentPossible, final boolean multiplePossible, final String name) {
		super( method);
		if( !this.expectation.argument().isAccepted() && !this.expectation.argumentChangeable())
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
	public Object decode( final DefaultAccessor result, final Collection< ? extends ArgumentException> parseTimeExceptions)
			throws FutureException {
		final List< String> operands = result.getOperands( this.name);
		try {
			return this.valueConstructor.construct( operands);
		} catch( final PreconditionException exception) {
			final Collection< ArgumentException> possibleCauses = new LinkedList<>();
			for( final ArgumentException parseTimeException: parseTimeExceptions)
				if( parseTimeException instanceof OperandPatternException)
					possibleCauses.add( parseTimeException);
			throw new FutureException( possibleCauses);
		} catch( final ReturnValueConstructionException exception) {
			throw new FutureException( new IllegalOperandValueException( this.name, operands.get( exception.getIndex()),
					exception.getCause()));
		}
	}
}

class OperandsHandler extends AbstractOperandHandler {

	public OperandsHandler( final Method method, final OperandPattern operandPattern) {
		super( method, operandPattern.isEmptyPossible(), operandPattern.isMoreThanOneOperandsPossible(), "operands");
	}

	@ Override
	public Object decode( final DefaultAccessor result, final Collection< ? extends ArgumentException> parseTimeExceptions)
			throws FutureException {
		final List< String> operands = result.getOperands();
		try {
			return this.valueConstructor.construct( operands);
		} catch( final PreconditionException exception) {
			final Collection< ArgumentException> possibleCauses = new LinkedList<>();
			for( final ArgumentException parseTimeException: parseTimeExceptions)
				if( parseTimeException instanceof OperandPatternException)
					possibleCauses.add( parseTimeException);
			throw new FutureException( possibleCauses);
		} catch( final ReturnValueConstructionException exception) {
			throw new FutureException( new IllegalOperandValueException( operands.get( exception.getIndex()), exception.getCause()));
		}
	}
}
