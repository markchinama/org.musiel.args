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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.musiel.args.ArgumentPolicy;
import org.musiel.args.DefaultAccessor;
import org.musiel.args.Option;
import org.musiel.args.ParserException;
import org.musiel.args.Result;
import org.musiel.args.generic.GenericAccessor;
import org.musiel.args.generic.InternationalizedParser;
import org.musiel.args.reflect.annotation.Description;
import org.musiel.args.reflect.annotation.OperandName;
import org.musiel.args.reflect.annotation.OperandPattern;
import org.musiel.args.reflect.annotation.Resource;
import org.musiel.args.syntax.GnuSyntax;
import org.musiel.args.syntax.Syntax;
import org.musiel.args.syntax.Syntax.SyntaxResult;

public class ReflectParser< MODEL> extends InternationalizedParser< Result< MODEL>> {

	@ Override
	public Option newOption( final String name, final String... aliases) {
		return super.newOption( name, aliases);
	}

	@ Override
	public Option newOption( final boolean required, final boolean repeatable, final ArgumentPolicy argumentPolicy, final String name,
			final String... aliases) {
		return super.newOption( required, repeatable, argumentPolicy, name, aliases);
	}

	public ReflectParser( final Class< MODEL> resultType) {
		this( new GnuSyntax(), resultType);
	}

	private final Class< MODEL> model;
	private final Map< Method, MethodHandler> methodHandlers = new HashMap<>();

	public ReflectParser( final Syntax syntax, final Class< MODEL> model) {
		super( syntax);

		if( !model.isInterface())
			throw new IllegalArgumentException( model.getName() + " is not an interface");
		this.model = model;

		if( model.isAnnotationPresent( OperandPattern.class)) {
			final OperandPattern annotation = model.getAnnotation( OperandPattern.class);
			this.setOperandPattern( annotation.value());
			for( final OperandName name: annotation.displayNames())
				this.setOperandName( name.name(), name.displayName());
		}
		if( model.isAnnotationPresent( Resource.class))
			this.setBundleBase( model.getAnnotation( Resource.class).value());
		if( model.isAnnotationPresent( Description.class))
			this.setDescription( model.getAnnotation( Description.class).value());

		for( final Method method: model.getMethods())
			if( !DefaultAccessor.class.equals( method.getDeclaringClass()))
				this.methodHandlers.put( method, MethodHandler.forMethod( method, this, this.getOperandPatternMatcher()));
	}

	@ Override
	protected Result< MODEL> adapt( final SyntaxResult syntaxResult, final Map< String, ? extends List< String>> operandMap,
			final Collection< ? extends ParserException> exceptions) {
		final GenericAccessor basicAccessor = new GenericAccessor( syntaxResult, operandMap);

		final Set< Method> decoded = new HashSet<>();
		final Map< Method, Object> decodedValues = new HashMap<>();
		final MODEL accessor =
				this.model.cast( Proxy.newProxyInstance( this.model.getClassLoader(), new Class< ?>[]{ this.model}, new InvocationHandler() {

					@ Override
					public Object invoke( final Object proxy, final Method method, final Object[] args) throws IllegalAccessException,
							InvocationTargetException {
						if( DefaultAccessor.class.equals( method.getDeclaringClass()))
							return method.invoke( basicAccessor, args);
						if( decoded.contains( method))
							return decodedValues.get( method);
						try {
							final Object decodedValue = ReflectParser.this.methodHandlers.get( method).decode( basicAccessor);
							decoded.add( method);
							decodedValues.put( method, decodedValue);
							return decodedValue;
						} catch( final DecoderException exception) {
							throw new UncheckedParserException( exception);
						}
					}
				}));

		return new Result< MODEL>() {

			@ Override
			public Collection< ? extends ParserException> getErrors() {
				return exceptions;
			}

			@ Override
			public Result< MODEL> check() throws ParserException {
				if( !exceptions.isEmpty())
					throw exceptions.iterator().next();
				for( final Entry< Method, MethodHandler> handler: ReflectParser.this.methodHandlers.entrySet())
					if( !decoded.contains( handler.getKey())) {
						decoded.add( handler.getKey());
						decodedValues.put( handler.getKey(), handler.getValue().decode( basicAccessor));
					}
				return this;
			}

			@ Override
			public MODEL getAccessor() {
				return accessor;
			}
		};
	}

	public static < MODEL>Result< MODEL> parse( final Syntax syntax, final Class< MODEL> resultType, final String... args) {
		return new ReflectParser< MODEL>( syntax, resultType).parse( args);
	}

	public static < MODEL>Result< MODEL> parse( final Class< MODEL> resultType, final String... args) {
		return new ReflectParser< MODEL>( resultType).parse( args);
	}
}
