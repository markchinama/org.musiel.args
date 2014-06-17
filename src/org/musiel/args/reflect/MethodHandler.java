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

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.musiel.args.ArgumentPolicy;
import org.musiel.args.DefaultAccessor;
import org.musiel.args.operand.OperandPattern;

abstract class MethodHandler {

	protected final ValueConstructor valueConstructor;
	protected final String defaultValue;
	protected final String environmentVariableName;

	public MethodHandler( final Method method) {
		final Decoder< ?> declaredDecoder = MethodHandler.getDeclaredDecoder( method);
		this.valueConstructor =
				declaredDecoder == null? MethodHandler.getDefaultConstructor( method): MethodHandler.checkAndReturnConstructor( method,
						declaredDecoder);

		this.defaultValue = method.isAnnotationPresent( Default.class)? method.getAnnotation( Default.class).value(): null;
		if( this.defaultValue != null && !"".equals( this.defaultValue))
			this.valueConstructor.decode( new ExceptionHandler< DecoderException>() {

				@ Override
				public void handle( final DecoderException exception) {
					throw new IllegalArgumentException( MethodHandler.this.defaultValue + " is invalid");
				}
			}, this.defaultValue, null);
		this.environmentVariableName =
				method.isAnnotationPresent( EnvironmentVariable.class)? method.getAnnotation( EnvironmentVariable.class).value(): null;
	}

	private static Decoder< ?> getDeclaredDecoder( final Method method) {
		Annotation decoderAnnotation = null;
		Class< ?> decoderClass = null;
		for( final Annotation annotation: method.getAnnotations())
			if( annotation.annotationType().isAnnotationPresent( DecoderAnnotation.class))
				if( decoderAnnotation != null)
					throw new IllegalArgumentException( "more than one decoder annotations found: " + decoderAnnotation + ", " + annotation);
				else
					decoderClass = ( decoderAnnotation = annotation).annotationType().getAnnotation( DecoderAnnotation.class).value();
		if( method.isAnnotationPresent( DecoderClass.class))
			if( decoderAnnotation != null)
				throw new IllegalArgumentException( "more than one decoder annotations found: " + decoderAnnotation + ", "
						+ method.getAnnotation( DecoderClass.class));
			else
				decoderClass = method.getAnnotation( DecoderClass.class).value();

		if( decoderClass == null)
			return null;
		if( decoderAnnotation != null)
			try {
				return Decoder.class.cast( decoderClass.getConstructor( decoderAnnotation.annotationType()).newInstance( decoderAnnotation));
			} catch( final NoSuchMethodException exception) {
				// deliberately ignored
			} catch( InstantiationException | IllegalAccessException | InvocationTargetException exception) {
				throw new IllegalArgumentException( exception);
			}
		try {
			return Decoder.class.cast( decoderClass.getConstructor().newInstance());
		} catch( final NoSuchMethodException exception) {
			throw new IllegalArgumentException( "constructor not found: " + decoderClass.getName());
		} catch( InstantiationException | IllegalAccessException | InvocationTargetException exception) {
			throw new IllegalArgumentException( exception);
		}
	}

	private static ValueConstructor getDefaultConstructor( final Method method) {
		final ValueConstructor defaultDecoder = MethodHandler.DEFAULTS.get( method.getReturnType());
		if( defaultDecoder == null)
			throw new IllegalArgumentException( "there is not a default decoder for return type " + method.getReturnType()
					+ ", please specify a decoder annotation");
		return defaultDecoder;
	}

	private static final Map< Class< ?>, ValueConstructor> DEFAULTS = new HashMap<>();
	static {
		MethodHandler.DEFAULTS.put( void.class, new NullConstructor());
		MethodHandler.DEFAULTS.put( boolean.class, new ExistenceIndicator());
		MethodHandler.DEFAULTS.put( byte.class, new ObjectConstructor( new ByteValue.Decoder(), ( byte) 0));
		MethodHandler.DEFAULTS.put( short.class, new ObjectConstructor( new ShortValue.Decoder(), ( short) 0));
		MethodHandler.DEFAULTS.put( int.class, new ObjectConstructor( new IntegerValue.Decoder(), 0));
		MethodHandler.DEFAULTS.put( long.class, new ObjectConstructor( new LongValue.Decoder(), 0L));
		MethodHandler.DEFAULTS.put( float.class, new ObjectConstructor( new FloatValue.Decoder(), 0.0F));
		MethodHandler.DEFAULTS.put( double.class, new ObjectConstructor( new DoubleValue.Decoder(), 0.0D));
		MethodHandler.DEFAULTS.put( char.class, new ObjectConstructor( new CharacterValue.Decoder(), ( char) 0));

		MethodHandler.DEFAULTS.put( Void.class, new NullConstructor());
		MethodHandler.DEFAULTS.put( Boolean.class, new ObjectConstructor( new BooleanValue.Decoder(), null));
		MethodHandler.DEFAULTS.put( Byte.class, new ObjectConstructor( new ByteValue.Decoder(), null));
		MethodHandler.DEFAULTS.put( Short.class, new ObjectConstructor( new ShortValue.Decoder(), null));
		MethodHandler.DEFAULTS.put( Integer.class, new ObjectConstructor( new IntegerValue.Decoder(), null));
		MethodHandler.DEFAULTS.put( Long.class, new ObjectConstructor( new LongValue.Decoder(), null));
		MethodHandler.DEFAULTS.put( Float.class, new ObjectConstructor( new FloatValue.Decoder(), null));
		MethodHandler.DEFAULTS.put( Double.class, new ObjectConstructor( new DoubleValue.Decoder(), null));
		MethodHandler.DEFAULTS.put( Character.class, new ObjectConstructor( new CharacterValue.Decoder(), null));

		MethodHandler.DEFAULTS.put( BigInteger.class, new ObjectConstructor( new BigIntegerValue.Decoder(), null));
		MethodHandler.DEFAULTS.put( BigDecimal.class, new ObjectConstructor( new BigDecimalValue.Decoder(), null));
		MethodHandler.DEFAULTS.put( String.class, new ObjectConstructor( new StringValue.Decoder(), null));
		MethodHandler.DEFAULTS.put( File.class, new ObjectConstructor( new FileValue.Decoder(), null));

		// void[] is not possible
		MethodHandler.DEFAULTS.put( boolean[].class, new ArrayConstructor( new BooleanValue.Decoder(), boolean.class, false));
		MethodHandler.DEFAULTS.put( byte[].class, new ArrayConstructor( new ByteValue.Decoder(), byte.class, ( byte) 0));
		MethodHandler.DEFAULTS.put( short[].class, new ArrayConstructor( new ShortValue.Decoder(), short.class, ( short) 0));
		MethodHandler.DEFAULTS.put( int[].class, new ArrayConstructor( new IntegerValue.Decoder(), int.class, 0));
		MethodHandler.DEFAULTS.put( long[].class, new ArrayConstructor( new LongValue.Decoder(), long.class, 0L));
		MethodHandler.DEFAULTS.put( float[].class, new ArrayConstructor( new FloatValue.Decoder(), float.class, 0.0F));
		MethodHandler.DEFAULTS.put( double[].class, new ArrayConstructor( new DoubleValue.Decoder(), double.class, 0.0D));
		MethodHandler.DEFAULTS.put( char[].class, new ArrayConstructor( new CharacterValue.Decoder(), char.class, ( char) 0));

		MethodHandler.DEFAULTS.put( Void[].class, new ArrayConstructor( null, Void.class, null));
		MethodHandler.DEFAULTS.put( Boolean[].class, new ArrayConstructor( new BooleanValue.Decoder(), Boolean.class, null));
		MethodHandler.DEFAULTS.put( Byte[].class, new ArrayConstructor( new ByteValue.Decoder(), Byte.class, null));
		MethodHandler.DEFAULTS.put( Short[].class, new ArrayConstructor( new ShortValue.Decoder(), Short.class, null));
		MethodHandler.DEFAULTS.put( Integer[].class, new ArrayConstructor( new IntegerValue.Decoder(), Integer.class, null));
		MethodHandler.DEFAULTS.put( Long[].class, new ArrayConstructor( new LongValue.Decoder(), Long.class, null));
		MethodHandler.DEFAULTS.put( Float[].class, new ArrayConstructor( new FloatValue.Decoder(), Float.class, null));
		MethodHandler.DEFAULTS.put( Double[].class, new ArrayConstructor( new DoubleValue.Decoder(), Double.class, null));
		MethodHandler.DEFAULTS.put( Character[].class, new ArrayConstructor( new CharacterValue.Decoder(), Character.class, null));

		MethodHandler.DEFAULTS.put( BigInteger[].class, new ArrayConstructor( new BigIntegerValue.Decoder(), BigInteger.class, null));
		MethodHandler.DEFAULTS.put( BigDecimal[].class, new ArrayConstructor( new BigDecimalValue.Decoder(), BigDecimal.class, null));
		MethodHandler.DEFAULTS.put( String[].class, new ArrayConstructor( new StringValue.Decoder(), String.class, null));
		MethodHandler.DEFAULTS.put( File[].class, new ArrayConstructor( new FileValue.Decoder(), File.class, null));
	}

	private static ValueConstructor checkAndReturnConstructor( final Method method, final Decoder< ?> declaredDecoder) {
		final Class< ?> methodReturnType = method.getReturnType();
		Class< ?> decoderReturnType;
		try {
			decoderReturnType = declaredDecoder.getClass().getMethod( "decode", String.class).getReturnType();
		} catch( final NoSuchMethodException exception) {
			throw new AssertionError();
		}

		if( void.class.equals( methodReturnType) || Void.class.equals( methodReturnType))
			return MethodHandler.DEFAULTS.get( methodReturnType);
		if( methodReturnType.isAssignableFrom( decoderReturnType))
			return new ObjectConstructor( declaredDecoder, null); // primitive type not possible, arrays including primitive arrays are
		if( methodReturnType.isPrimitive())
			if( !PrimitiveType.forPrimitiveType( methodReturnType).getWrapperType().isAssignableFrom( decoderReturnType))
				throw new IllegalArgumentException( "decoder of type " + decoderReturnType + " cannot be applied to method with return type "
						+ methodReturnType);
			else
				return new ObjectConstructor( declaredDecoder, PrimitiveType.forPrimitiveType( methodReturnType).getDefaultValue());
		if( !methodReturnType.isArray())
			throw new IllegalArgumentException( "decoder of type " + decoderReturnType + " cannot be applied to method with return type "
					+ methodReturnType);
		final Class< ?> componentType = methodReturnType.getComponentType();
		if( componentType.isAssignableFrom( decoderReturnType))
			return new ArrayConstructor( declaredDecoder, componentType, null); // non-primitive
		if( !componentType.isPrimitive())
			throw new IllegalArgumentException( "decoder of type " + decoderReturnType + " cannot be applied to method with return type "
					+ methodReturnType);
		if( !PrimitiveType.forPrimitiveType( componentType).getWrapperType().isAssignableFrom( decoderReturnType))
			throw new IllegalArgumentException( "decoder of type " + decoderReturnType + " cannot be applied to method with return type "
					+ methodReturnType);
		return new ArrayConstructor( declaredDecoder, componentType, PrimitiveType.forPrimitiveType( componentType).getDefaultValue());
	}

	public abstract Object decode( final DefaultAccessor basicAccessor, ExceptionHandler< DecoderException> exceptionHandler);
}

class OptionHandler extends MethodHandler {

	private final String optionName;

	public OptionHandler( final Method method, final ReflectParser< ?> parser) {
		super( method);

		// names
		String[] additionalNames;
		final Option option = method.getAnnotation( Option.class);
		if( option != null && option.value().length > 0) {
			this.optionName = option.value()[ 0];
			additionalNames = option.value(); // duplicates are okay
		} else {
			this.optionName = OptionHandler.constructName( method.getName());
			additionalNames = new String[]{};
		}

		// properties
		final boolean required = method.isAnnotationPresent( Required.class)? method.getAnnotation( Required.class).value(): false;
		final boolean repeatable =
				method.isAnnotationPresent( Repeatable.class)? method.getAnnotation( Repeatable.class).value(): this.valueConstructor
						.expectsMany();
		final ArgumentPolicy argument =
				method.isAnnotationPresent( Argument.class)? method.getAnnotation( Argument.class).value(): this.valueConstructor
						.dependsOnContent()? ArgumentPolicy.REQUIRED: ArgumentPolicy.NONE;

		// i18n
		final String description = method.isAnnotationPresent( Description.class)? method.getAnnotation( Description.class).value(): null;
		final String argumentName =
				method.isAnnotationPresent( ArgumentName.class)? method.getAnnotation( ArgumentName.class).value(): null;

		// register
		parser.newOption( required, repeatable, argument, this.optionName, additionalNames);
		if( description != null)
			parser.setOptionDescription( this.optionName, description);
		if( argumentName != null)
			parser.setArgumentName( this.optionName, argumentName);
	}

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

	@ Override
	public Object decode( final DefaultAccessor basicAccessor, final ExceptionHandler< DecoderException> exceptionHandler) {
		return this.valueConstructor.decode( new ExceptionHandler< DecoderException>() {

			@ Override
			public void handle( final DecoderException exception) {
				exceptionHandler.handle( new DecoderException( exception, MethodHandler.class.getPackage().getName() + ".exceptions",
						"illegal-value.option", OptionHandler.this.optionName));
			}
		}, this.defaultValue, this.environmentVariableName, basicAccessor.getArgumentsAsArray( this.optionName));
	}
}

class OperandHandler extends MethodHandler {

	private final String operandName;

	public OperandHandler( final Method method, final OperandPattern operandPattern) {
		super( method);

		// check illegal annotations
		for( final Class< ? extends Annotation> conflictingAnnotations: OperandHandler.CONFLICT_WITH_OPERANDS)
			if( method.isAnnotationPresent( conflictingAnnotations))
				throw new IllegalArgumentException( "operand method cannot be annotated @" + conflictingAnnotations.getSimpleName());
		final String operandName = method.getAnnotation( Operands.class).value();

		// check and set operand name
		if( "".equals( operandName))
			this.operandName = null;
		else if( operandPattern == null || !operandPattern.getNames().contains( operandName))
			throw new IllegalArgumentException( "operand name \"" + operandName + "\" does not exist in the operand pattern");
		else
			this.operandName = operandName;
	}

	private static final Set< Class< ? extends Annotation>> CONFLICT_WITH_OPERANDS = new HashSet<>();
	static {
		OperandHandler.CONFLICT_WITH_OPERANDS.add( Argument.class);
		OperandHandler.CONFLICT_WITH_OPERANDS.add( ArgumentName.class);
		OperandHandler.CONFLICT_WITH_OPERANDS.add( Description.class);
		OperandHandler.CONFLICT_WITH_OPERANDS.add( Option.class);
		OperandHandler.CONFLICT_WITH_OPERANDS.add( Repeatable.class);
		OperandHandler.CONFLICT_WITH_OPERANDS.add( Required.class);
	}

	@ Override
	public Object decode( final DefaultAccessor basicAccessor, final ExceptionHandler< DecoderException> exceptionHandler) {
		return this.valueConstructor.decode( new ExceptionHandler< DecoderException>() {

			@ Override
			public void handle( final DecoderException exception) {
				exceptionHandler.handle( OperandHandler.this.operandName == null? new DecoderException( exception, MethodHandler.class
						.getPackage().getName() + ".exceptions", "illegal-value.operand.unnamed"): new DecoderException( exception,
						MethodHandler.class.getPackage().getName() + ".exceptions", "illegal-value.operand.named",
						OperandHandler.this.operandName));
			}
		}, this.defaultValue, this.environmentVariableName,
				this.operandName == null? basicAccessor.getOperandsAsArray(): basicAccessor.getOperandsAsArray( this.operandName));
	}
}
