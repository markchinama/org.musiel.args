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
import java.math.BigDecimal;
import java.math.BigInteger;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.musiel.args.decoder.Decoder;
import org.musiel.args.decoder.DecoderException;

public class DecoderTest {

	@ Rule
	public final ExpectedException exceptions = ExpectedException.none();

	@ Test
	public void testNulls() throws DecoderException {
		for( final Decoder< ?> decoder: new Decoder< ?>[]{ Decoder.BYTE_DECODER, Decoder.SHORT_DECODER, Decoder.INTEGER_DECODER,
				Decoder.LONG_DECODER, Decoder.FLOAT_DECODER, Decoder.DOUBLE_DECODER, Decoder.BOOLEAN_DECODER, Decoder.CHARACTER_DECODER,
				Decoder.BIG_INTEGER_DECODER, Decoder.BIG_DECIMAL_DECODER, Decoder.STRING_DECODER, Decoder.FILE_DECODER})
			Assert.assertNull( decoder.decode( null));
	}

	@ Test
	public void testNonNumber() {
		for( final Decoder< ?> decoder: new Decoder< ?>[]{ Decoder.BYTE_DECODER, Decoder.SHORT_DECODER, Decoder.INTEGER_DECODER,
				Decoder.LONG_DECODER, Decoder.FLOAT_DECODER, Decoder.DOUBLE_DECODER, Decoder.BIG_INTEGER_DECODER,
				Decoder.BIG_DECIMAL_DECODER,})
			try {
				decoder.decode( "1x");
				Assert.fail();
			} catch( final DecoderException e) {
			}
	}

	@ Test
	public void testNonInteger() {
		for( final Decoder< ?> decoder: new Decoder< ?>[]{ Decoder.BYTE_DECODER, Decoder.SHORT_DECODER, Decoder.INTEGER_DECODER,
				Decoder.LONG_DECODER, Decoder.BIG_INTEGER_DECODER})
			try {
				decoder.decode( "1.0");
				Assert.fail();
			} catch( final DecoderException e) {
			}
	}

	@ Test
	public void testByte() throws DecoderException {
		Assert.assertEquals( 0, Decoder.BYTE_DECODER.decode( "0").byteValue());
		Assert.assertEquals( 0, Decoder.BYTE_DECODER.decode( "-0").byteValue());
		Assert.assertEquals( 127, Decoder.BYTE_DECODER.decode( "7f").byteValue());
		Assert.assertEquals( -128, Decoder.BYTE_DECODER.decode( "80").byteValue());
		this.exceptions.expect( DecoderException.class);
		this.exceptions.expectMessage( "invalid ");
		this.exceptions.expectMessage( " value");
		Decoder.BYTE_DECODER.decode( "1g");
	}

	@ Test
	public void testShort() throws DecoderException {
		Assert.assertEquals( 128, Decoder.SHORT_DECODER.decode( "128").shortValue());
		Assert.assertEquals( -129, Decoder.SHORT_DECODER.decode( "-129").shortValue());
		Assert.assertEquals( 32767, Decoder.SHORT_DECODER.decode( "32767").shortValue());
		Assert.assertEquals( -32768, Decoder.SHORT_DECODER.decode( "-32768").shortValue());
		this.exceptions.expect( DecoderException.class);
		this.exceptions.expectMessage( "invalid ");
		this.exceptions.expectMessage( " value");
		Decoder.SHORT_DECODER.decode( "32768");
	}

	@ Test
	public void testInt() throws DecoderException {
		Assert.assertEquals( 32768, Decoder.INTEGER_DECODER.decode( "32768").intValue());
		Assert.assertEquals( -32769, Decoder.INTEGER_DECODER.decode( "-32769").intValue());
		Assert.assertEquals( 2147483647, Decoder.INTEGER_DECODER.decode( "2147483647").intValue());
		Assert.assertEquals( -2147483648, Decoder.INTEGER_DECODER.decode( "-2147483648").intValue());
		this.exceptions.expect( DecoderException.class);
		this.exceptions.expectMessage( "invalid ");
		this.exceptions.expectMessage( " value");
		Decoder.INTEGER_DECODER.decode( "2147483648");
	}

	@ Test
	public void testLong() throws DecoderException {
		Assert.assertEquals( 2147483648L, Decoder.LONG_DECODER.decode( "2147483648").longValue());
		Assert.assertEquals( -2147483649L, Decoder.LONG_DECODER.decode( "-2147483649").longValue());
		Assert.assertEquals( 9223372036854775807L, Decoder.LONG_DECODER.decode( "9223372036854775807").longValue());
		Assert.assertEquals( -9223372036854775808L, Decoder.LONG_DECODER.decode( "-9223372036854775808").longValue());
		this.exceptions.expect( DecoderException.class);
		this.exceptions.expectMessage( "invalid ");
		this.exceptions.expectMessage( " value");
		Decoder.LONG_DECODER.decode( "9223372036854775808");
	}

	@ Test
	public void testBig() throws DecoderException {
		Assert.assertEquals( new BigInteger( "-1234567891234567891234567891234568912345678914454545454545454"),
				Decoder.BIG_INTEGER_DECODER.decode( "-1234567891234567891234567891234568912345678914454545454545454"));
		Assert.assertEquals( new BigDecimal( "-123456789123456789123456789123456891.2345678914454545454545454"),
				Decoder.BIG_DECIMAL_DECODER.decode( "-123456789123456789123456789123456891.2345678914454545454545454"));
	}

	@ Test
	public void testReal() throws DecoderException {
		Assert.assertEquals( 2.0f, Decoder.FLOAT_DECODER.decode( "2.0").floatValue(), 0.00001);
		Assert.assertEquals( 2.0d, Decoder.DOUBLE_DECODER.decode( "2.0").doubleValue(), 0.00001);
	}

	@ Test
	public void testCharAndStringAndFile() throws DecoderException {
		Assert.assertEquals( "abcdefg", Decoder.STRING_DECODER.decode( "abcdefg"));
		Assert.assertEquals( new File( "/tmp"), Decoder.FILE_DECODER.decode( "/tmp"));
		Assert.assertEquals( 'x', Decoder.CHARACTER_DECODER.decode( "x").charValue());
		this.exceptions.expect( DecoderException.class);
		Decoder.CHARACTER_DECODER.decode( "xx");
	}

	@ Test
	public void testBoolean() throws DecoderException {
		for( final String string: new String[]{ "true", "TRUE", "T", "t", "tRuE", "yes", "Yes", "yEs", "y", "Y"})
			Assert.assertTrue( Decoder.BOOLEAN_DECODER.decode( string));
		for( final String string: new String[]{ "false", "FALSE", "F", "f", "fAlSe", "no", "No", "nO", "n", "N"})
			Assert.assertFalse( Decoder.BOOLEAN_DECODER.decode( string));
		this.exceptions.expect( DecoderException.class);
		Decoder.BOOLEAN_DECODER.decode( "enabled");
	}

	@ Test
	public void test() throws DecoderException {
		Assert.assertNull( Decoder.VOID_DECODER.decode( "abc"));

		Assert.assertEquals( 400, Decoder.SHORT_DECODER.decode( "400").shortValue());
		Assert.assertEquals( -400, Decoder.SHORT_DECODER.decode( "-400").shortValue());

		Assert.assertEquals( 2147483647, Decoder.INTEGER_DECODER.decode( "2147483647").intValue());
		Assert.assertEquals( -2147483648, Decoder.INTEGER_DECODER.decode( "-2147483648").intValue());

		Assert.assertEquals( 40000000, Decoder.INTEGER_DECODER.decode( "40000000").intValue());
		Assert.assertEquals( -40000, Decoder.INTEGER_DECODER.decode( "-40000").intValue());
	}
}
