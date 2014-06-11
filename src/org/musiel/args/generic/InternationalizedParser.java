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

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeMap;

import org.musiel.args.Option;
import org.musiel.args.Result;
import org.musiel.args.i18n.Resource;
import org.musiel.args.i18n.ResourceSet;
import org.musiel.args.syntax.Syntax;

public abstract class InternationalizedParser< RESULT extends Result< ?>> extends AbstractParser< RESULT> implements ResourceSet {

	protected InternationalizedParser( final Syntax syntax) {
		super( syntax);
	}

	private String bundleBase = null;

	public String getBundleBase() {
		return this.bundleBase;
	}

	public void setBundleBase( final String bundleBase) {
		this.bundleBase = bundleBase;
	}

	private String description = null;
	private final Map< Option, String> optionDescriptions = new HashMap<>();
	private final Map< Option, String> argumentNames = new HashMap<>();
	private final Map< String, String> operandDescriptions = new TreeMap<>();

	public String getDescription() {
		return this.description;
	}

	public void setDescription( final String description) {
		this.description = description;
	}

	public String getOptionDescription( final String optionName) {
		final Option option = this.getOption( optionName);
		if( option == null)
			throw new IllegalArgumentException( "unknown option: " + optionName);
		return this.optionDescriptions.get( option);
	}

	public void setOptionDescription( final String optionName, final String description) {
		final Option option = this.getOption( optionName);
		if( option == null)
			throw new IllegalArgumentException( "unknown option: " + optionName);
		this.optionDescriptions.put( option, description);
	}

	public String getArgumentName( final String optionName) {
		final Option option = this.getOption( optionName);
		if( option == null)
			throw new IllegalArgumentException( "unknown option: " + optionName);
		if( !option.getArgumentPolicy().isAccepted())
			throw new IllegalArgumentException( "option " + optionName + " does not accept arguments");
		return this.argumentNames.get( option);
	}

	public void setArgumentName( final String optionName, final String argumentName) {
		final Option option = this.getOption( optionName);
		if( option == null)
			throw new IllegalArgumentException( "unknown option: " + optionName);
		if( !option.getArgumentPolicy().isAccepted())
			throw new IllegalArgumentException( "option " + optionName + " does not accept arguments");
		this.argumentNames.put( option, argumentName);
	}

	public String getOperandDescription( final String operandName) {
		final Collection< String> operandNames = this.getOperandNames();
		if( operandNames == null || !operandNames.contains( operandName))
			throw new IllegalArgumentException( "unknown operand: " + operandName);
		return this.operandDescriptions.get( operandName);
	}

	public void setOperandDescription( final String operandName, final String operandDescription) {
		final Collection< String> operandNames = this.getOperandNames();
		if( operandNames == null || !operandNames.contains( operandName))
			throw new IllegalArgumentException( "unknown operand: " + operandName);
		this.operandDescriptions.put( operandName, operandDescription);
	}

	@ Override
	public Resource getDefaultResource() {
		return this.getResource( Locale.getDefault());
	}

	@ Override
	public Resource getResource( final Locale locale) {
		return new ResourceImpl( locale);
	}

	private class ResourceImpl implements Resource {

		private final Locale locale;

		public ResourceImpl( final Locale locale) {
			super();
			this.locale = locale;
		}

		@ Override
		public String getDescription() {
			return this.get( InternationalizedParser.this.getDescription(), "description");
		}

		@ Override
		public String getArgumentName( final String optionName) {
			final Option option = InternationalizedParser.this.getOption( optionName);
			if( option == null)
				throw new IllegalArgumentException( "unknown option: " + optionName);
			if( !option.getArgumentPolicy().isAccepted())
				throw new IllegalArgumentException( "option " + optionName + " does not accept arguments");
			final Set< String> names = option.getNames();
			final String[] keys = new String[ names.size()];
			int index = 0;
			for( final String name: names)
				keys[ index++] = "option." + name + ".argument";
			return this.get( InternationalizedParser.this.argumentNames.get( option), keys);
		}

		@ Override
		public String getOptionDescription( final String optionName) {
			final Option option = InternationalizedParser.this.getOption( optionName);
			if( option == null)
				throw new IllegalArgumentException( "unknown option: " + optionName);
			final Set< String> names = option.getNames();
			final String[] keys = new String[ names.size()];
			int index = 0;
			for( final String name: names)
				keys[ index++] = "option." + name + ".description";
			return this.get( InternationalizedParser.this.optionDescriptions.get( option), keys);
		}

		@ Override
		public String getOperandDescription( final String operandName) {
			return this.get( InternationalizedParser.this.getOperandDescription( operandName), "operand." + operandName);
		}

		private String get( final String direct, final String... keys) {
			if( direct != null)
				return direct;
			if( InternationalizedParser.this.bundleBase == null)
				return null;
			try {
				final ResourceBundle bundle = ResourceBundle.getBundle( InternationalizedParser.this.bundleBase, this.locale);
				for( final String key: keys)
					try {
						return bundle.getString( key);
					} catch( final MissingResourceException exception) {
						// omitted deliberately
					}
			} catch( final MissingResourceException exception) {
				// omitted deliberately
			}
			return null;
		}
	}
}
