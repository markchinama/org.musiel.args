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
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.musiel.args.Option;
import org.musiel.args.ParserException;
import org.musiel.args.Result;
import org.musiel.args.generic.AbstractParser;
import org.musiel.args.syntax.GnuSyntax;
import org.musiel.args.syntax.Syntax;

public class ReflectParser< RESULT> extends AbstractParser< RESULT> {

	@ Override
	public Option newOption( final String name, final String... aliases) {
		return super.newOption( name, aliases);
	}

	@ Override
	public Option newOption( final boolean required, final boolean repeatable, final boolean acceptsArgument,
			final boolean requiresArgument, final String name, final String... aliases) {
		return super.newOption( required, repeatable, acceptsArgument, requiresArgument, name, aliases);
	}

	public ReflectParser( final Class< RESULT> resultType) {
		this( new GnuSyntax(), resultType);
	}

	private final Class< RESULT> resultType;
	private final Set< MethodHandler> methodHandlers = new HashSet<>();

	public ReflectParser( final Syntax syntax, final Class< RESULT> resultType) {
		super( syntax);

		if( !resultType.isInterface())
			throw new IllegalArgumentException( resultType.getName() + " is not an interface");
		this.resultType = resultType;

		if( resultType.isAnnotationPresent( OperandPattern.class))
			this.setOperandPattern( resultType.getAnnotation( OperandPattern.class).value());

		for( final Method method: resultType.getMethods())
			if( !Result.class.equals( method.getDeclaringClass()))
				this.methodHandlers.add( MethodHandler.forMethod( method, this, this.getOperandPatternMatcher()));
	}

	@ Override
	protected RESULT postProcess( final Result result) throws ParserException {
		final Map< Method, Object> returnValues = new HashMap<>();
		for( final MethodHandler handler: this.methodHandlers)
			returnValues.put( handler.getMethod(), handler.decode( result));

		return this.resultType.cast( Proxy.newProxyInstance( this.resultType.getClassLoader(), new Class< ?>[]{ this.resultType},
				new InvocationHandler() {

					@ Override
					public Object invoke( final Object proxy, final Method method, final Object[] args) throws Throwable {
						if( Result.class.equals( method.getDeclaringClass()))
							return method.invoke( result, args);
						if( returnValues.containsKey( method))
							return returnValues.get( method);
						throw new AssertionError( "you found a bug");
					}
				}));
	}

	public static < RESULT>RESULT parse( final Syntax syntax, final Class< RESULT> resultType, final String... args)
			throws ParserException {
		return new ReflectParser< RESULT>( syntax, resultType).parse( args);
	}

	public static < RESULT>RESULT parse( final Class< RESULT> resultType, final String... args) throws ParserException {
		return new ReflectParser< RESULT>( resultType).parse( args);
	}
}
