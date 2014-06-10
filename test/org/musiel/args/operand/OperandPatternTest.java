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
package org.musiel.args.operand;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.musiel.args.operand.OperandException.Reason;

public class OperandPatternTest {

	private static final String[] PATTERNS = new String[]{
			//
			/* 0 */"", //
			/* 1 */" ", //
			/* 2 */"A B | A C ", //
			/* 3 */"A [B] [C] D", //
			/* 4 */"[A [B]] (C D E)...", //
			/* 5 */"[A [B]] [C D E]...", //
			/* 6 */"[A [B [C [D [E]]]]]", //
			/* 7 */"[A [B [C [D E [F]]]]]", //
			/* 8 */"A B... C", //
			/* 9 */"A B... [C]", //
			/* 10 */"A B C [A]", //
	};

	@ Test
	public void testAmbiguity() {
		Assert.assertFalse( OperandPattern.compile( OperandPatternTest.PATTERNS[ 0]).isAmbiguous());
		Assert.assertFalse( OperandPattern.compile( OperandPatternTest.PATTERNS[ 1]).isAmbiguous());
		Assert.assertTrue( OperandPattern.compile( OperandPatternTest.PATTERNS[ 2]).isAmbiguous());
		Assert.assertTrue( OperandPattern.compile( OperandPatternTest.PATTERNS[ 3]).isAmbiguous());
		Assert.assertFalse( OperandPattern.compile( OperandPatternTest.PATTERNS[ 4]).isAmbiguous());
		Assert.assertFalse( OperandPattern.compile( OperandPatternTest.PATTERNS[ 5]).isAmbiguous());
		Assert.assertFalse( OperandPattern.compile( OperandPatternTest.PATTERNS[ 6]).isAmbiguous());
		Assert.assertFalse( OperandPattern.compile( OperandPatternTest.PATTERNS[ 7]).isAmbiguous());
		Assert.assertFalse( OperandPattern.compile( OperandPatternTest.PATTERNS[ 8]).isAmbiguous());
		Assert.assertTrue( OperandPattern.compile( OperandPatternTest.PATTERNS[ 9]).isAmbiguous());
	}

	private void seq( final int index, final int length, final String... paths) {
		final Set< List< String>> expected = new HashSet<>();
		for( String path: paths) {
			path = path.trim();
			final List< String> list = new LinkedList<>();
			Collections.addAll( list, "".equals( path)? new String[]{}: path.split( "\\s+"));
			expected.add( list);
		}
		final Set< List< String>> fact = new HashSet<>();
		for( final String[] sequence: OperandPattern.compile( OperandPatternTest.PATTERNS[ index]).getSequences( length)) {
			final List< String> list = new LinkedList<>();
			Collections.addAll( list, sequence);
			fact.add( list);
		}
		Assert.assertEquals( expected, fact);
	}

	@ Test
	public void testSequence() {
		for( int index = 0; index < 2; ++index) {
			this.seq( index, 0, "");
			this.seq( index, 1);
			this.seq( index, 2);
		}
		this.seq( 2, 0);
		this.seq( 2, 1);
		this.seq( 2, 2, "A B", "A C");
		this.seq( 2, 3);
		this.seq( 3, 0);
		this.seq( 3, 1);
		this.seq( 3, 2, "A D");
		this.seq( 3, 3, "A B D", "A C D");
		this.seq( 3, 4, "A B C D");
		this.seq( 3, 5);
		this.seq( 5, 0, "");
		this.seq( 5, 1, "A");
		this.seq( 5, 2, "A B");
		this.seq( 5, 3, "C D E");
		this.seq( 5, 4, "A C D E");
		this.seq( 5, 5, "A B C D E");
		this.seq( 5, 6, "C D E C D E");
		this.seq( 5, 7, "A C D E C D E");
	}

	@ Test
	public void testMatching() throws OperandException {
		try {
			OperandPattern.compile( OperandPatternTest.PATTERNS[ 3]).match( "a", "b", "d");
			Assert.fail();
		} catch( final IllegalStateException exception) {
		}

		try {
			OperandPattern.compile( OperandPatternTest.PATTERNS[ 2]).match( "a", "b", "c");
			Assert.fail();
		} catch( final OperandException exception) {
			Assert.assertEquals( Reason.TOO_MANY, exception.getReason());
		}

		try {
			OperandPattern.compile( OperandPatternTest.PATTERNS[ 2]).match( "a");
			Assert.fail();
		} catch( final OperandException exception) {
			Assert.assertEquals( Reason.TOO_FEW, exception.getReason());
		}

		Map< String, List< String>> result;

		result = OperandPattern.compile( OperandPatternTest.PATTERNS[ 3]).match( "a", "b", "c", "d");
		Assert.assertEquals( this.toList( "a"), result.get( "A"));
		Assert.assertEquals( this.toList( "b"), result.get( "B"));
		Assert.assertEquals( this.toList( "c"), result.get( "C"));
		Assert.assertEquals( this.toList( "d"), result.get( "D"));

		result = OperandPattern.compile( OperandPatternTest.PATTERNS[ 5]).match( "a", "c", "d", "e");
		Assert.assertEquals( this.toList( "a"), result.get( "A"));
		Assert.assertEquals( this.toList( "c"), result.get( "C"));
		Assert.assertEquals( this.toList( "d"), result.get( "D"));
		Assert.assertEquals( this.toList( "e"), result.get( "E"));

		result = OperandPattern.compile( OperandPatternTest.PATTERNS[ 5]).match( "a", "c1", "d1", "e1", "c2", "d2", "e2");
		Assert.assertEquals( this.toList( "a"), result.get( "A"));
		Assert.assertEquals( this.toList( "c1", "c2"), result.get( "C"));
		Assert.assertEquals( this.toList( "d1", "d2"), result.get( "D"));
		Assert.assertEquals( this.toList( "e1", "e2"), result.get( "E"));
	}

	private List< String> toList( final String... strings) {
		final List< String> list = new LinkedList<>();
		Collections.addAll( list, strings);
		return list;
	}

	@ Test
	public void testMultipleOccurrencesPossible() {
		Assert.assertFalse( OperandPattern.compile( OperandPatternTest.PATTERNS[ 2]).isMultipleOccurrencePossible( "A"));
		Assert.assertFalse( OperandPattern.compile( OperandPatternTest.PATTERNS[ 2]).isMultipleOccurrencePossible( "B"));
		Assert.assertFalse( OperandPattern.compile( OperandPatternTest.PATTERNS[ 2]).isMultipleOccurrencePossible( "C"));

		Assert.assertFalse( OperandPattern.compile( OperandPatternTest.PATTERNS[ 3]).isMultipleOccurrencePossible( "A"));
		Assert.assertFalse( OperandPattern.compile( OperandPatternTest.PATTERNS[ 3]).isMultipleOccurrencePossible( "B"));
		Assert.assertFalse( OperandPattern.compile( OperandPatternTest.PATTERNS[ 3]).isMultipleOccurrencePossible( "C"));
		Assert.assertFalse( OperandPattern.compile( OperandPatternTest.PATTERNS[ 3]).isMultipleOccurrencePossible( "D"));

		for( int i = 4; i <= 5; ++i) {
			Assert.assertFalse( OperandPattern.compile( OperandPatternTest.PATTERNS[ 4]).isMultipleOccurrencePossible( "A"));
			Assert.assertFalse( OperandPattern.compile( OperandPatternTest.PATTERNS[ 4]).isMultipleOccurrencePossible( "B"));
			Assert.assertTrue( OperandPattern.compile( OperandPatternTest.PATTERNS[ 4]).isMultipleOccurrencePossible( "C"));
			Assert.assertTrue( OperandPattern.compile( OperandPatternTest.PATTERNS[ 4]).isMultipleOccurrencePossible( "D"));
			Assert.assertTrue( OperandPattern.compile( OperandPatternTest.PATTERNS[ 4]).isMultipleOccurrencePossible( "E"));
		}

		Assert.assertTrue( OperandPattern.compile( OperandPatternTest.PATTERNS[ 10]).isMultipleOccurrencePossible( "A"));
		Assert.assertFalse( OperandPattern.compile( OperandPatternTest.PATTERNS[ 10]).isMultipleOccurrencePossible( "B"));
		Assert.assertFalse( OperandPattern.compile( OperandPatternTest.PATTERNS[ 10]).isMultipleOccurrencePossible( "C"));
	}

	@ Test
	public void testAbsencePossible() {
		Assert.assertFalse( OperandPattern.compile( OperandPatternTest.PATTERNS[ 2]).isAbsencePossible( "A"));
		Assert.assertTrue( OperandPattern.compile( OperandPatternTest.PATTERNS[ 2]).isAbsencePossible( "B"));
		Assert.assertTrue( OperandPattern.compile( OperandPatternTest.PATTERNS[ 2]).isAbsencePossible( "C"));

		Assert.assertFalse( OperandPattern.compile( OperandPatternTest.PATTERNS[ 3]).isAbsencePossible( "A"));
		Assert.assertTrue( OperandPattern.compile( OperandPatternTest.PATTERNS[ 3]).isAbsencePossible( "B"));
		Assert.assertTrue( OperandPattern.compile( OperandPatternTest.PATTERNS[ 3]).isAbsencePossible( "C"));
		Assert.assertFalse( OperandPattern.compile( OperandPatternTest.PATTERNS[ 3]).isAbsencePossible( "D"));

		Assert.assertTrue( OperandPattern.compile( OperandPatternTest.PATTERNS[ 4]).isAbsencePossible( "A"));
		Assert.assertTrue( OperandPattern.compile( OperandPatternTest.PATTERNS[ 4]).isAbsencePossible( "B"));
		Assert.assertFalse( OperandPattern.compile( OperandPatternTest.PATTERNS[ 4]).isAbsencePossible( "C"));
		Assert.assertFalse( OperandPattern.compile( OperandPatternTest.PATTERNS[ 4]).isAbsencePossible( "D"));
		Assert.assertFalse( OperandPattern.compile( OperandPatternTest.PATTERNS[ 4]).isAbsencePossible( "E"));

		Assert.assertTrue( OperandPattern.compile( OperandPatternTest.PATTERNS[ 5]).isAbsencePossible( "A"));
		Assert.assertTrue( OperandPattern.compile( OperandPatternTest.PATTERNS[ 5]).isAbsencePossible( "B"));
		Assert.assertTrue( OperandPattern.compile( OperandPatternTest.PATTERNS[ 5]).isAbsencePossible( "C"));
		Assert.assertTrue( OperandPattern.compile( OperandPatternTest.PATTERNS[ 5]).isAbsencePossible( "D"));
		Assert.assertTrue( OperandPattern.compile( OperandPatternTest.PATTERNS[ 5]).isAbsencePossible( "E"));

		Assert.assertFalse( OperandPattern.compile( OperandPatternTest.PATTERNS[ 10]).isAbsencePossible( "A"));
		Assert.assertFalse( OperandPattern.compile( OperandPatternTest.PATTERNS[ 10]).isAbsencePossible( "B"));
		Assert.assertFalse( OperandPattern.compile( OperandPatternTest.PATTERNS[ 10]).isAbsencePossible( "C"));
	}

	@ Test
	public void testMoreThanOneOperandsPossible() {
		Assert.assertFalse( OperandPattern.compile( OperandPatternTest.PATTERNS[ 0]).isMoreThanOneOperandsPossible());
		Assert.assertFalse( OperandPattern.compile( OperandPatternTest.PATTERNS[ 1]).isMoreThanOneOperandsPossible());
		Assert.assertTrue( OperandPattern.compile( OperandPatternTest.PATTERNS[ 2]).isMoreThanOneOperandsPossible());
		Assert.assertTrue( OperandPattern.compile( OperandPatternTest.PATTERNS[ 3]).isMoreThanOneOperandsPossible());
		Assert.assertTrue( OperandPattern.compile( OperandPatternTest.PATTERNS[ 4]).isMoreThanOneOperandsPossible());
		Assert.assertTrue( OperandPattern.compile( OperandPatternTest.PATTERNS[ 5]).isMoreThanOneOperandsPossible());
		Assert.assertTrue( OperandPattern.compile( OperandPatternTest.PATTERNS[ 6]).isMoreThanOneOperandsPossible());
		Assert.assertTrue( OperandPattern.compile( OperandPatternTest.PATTERNS[ 7]).isMoreThanOneOperandsPossible());
		Assert.assertTrue( OperandPattern.compile( OperandPatternTest.PATTERNS[ 8]).isMoreThanOneOperandsPossible());
		Assert.assertTrue( OperandPattern.compile( OperandPatternTest.PATTERNS[ 9]).isMoreThanOneOperandsPossible());
		Assert.assertTrue( OperandPattern.compile( OperandPatternTest.PATTERNS[ 10]).isMoreThanOneOperandsPossible());
	}

	@ Test
	public void testEmptyPossible() {
		Assert.assertTrue( OperandPattern.compile( OperandPatternTest.PATTERNS[ 0]).isEmptyPossible());
		Assert.assertTrue( OperandPattern.compile( OperandPatternTest.PATTERNS[ 1]).isEmptyPossible());
		Assert.assertFalse( OperandPattern.compile( OperandPatternTest.PATTERNS[ 2]).isEmptyPossible());
		Assert.assertFalse( OperandPattern.compile( OperandPatternTest.PATTERNS[ 3]).isEmptyPossible());
		Assert.assertFalse( OperandPattern.compile( OperandPatternTest.PATTERNS[ 4]).isEmptyPossible());
		Assert.assertTrue( OperandPattern.compile( OperandPatternTest.PATTERNS[ 5]).isEmptyPossible());
		Assert.assertTrue( OperandPattern.compile( OperandPatternTest.PATTERNS[ 6]).isEmptyPossible());
		Assert.assertTrue( OperandPattern.compile( OperandPatternTest.PATTERNS[ 7]).isEmptyPossible());
		Assert.assertFalse( OperandPattern.compile( OperandPatternTest.PATTERNS[ 8]).isEmptyPossible());
		Assert.assertFalse( OperandPattern.compile( OperandPatternTest.PATTERNS[ 9]).isEmptyPossible());
		Assert.assertFalse( OperandPattern.compile( OperandPatternTest.PATTERNS[ 10]).isEmptyPossible());
	}

	// * 0 */"", //
	// * 1 */" ", //
	// * 2 */"A B | A C ", //
	// * 3 */"A [B] [C] D", //
	// * 4 */"[A [B]] (C D E)...", //
	// * 5 */"[A [B]] [C D E]...", //
	// * 6 */"[A [B [C [D [E]]]]]", //
	// * 7 */"[A [B [C [D E [F]]]]]", //
	// * 8 */"A B... C", //
	// * 9 */"A B... [C]", //
	// * 10*/"A B C [A]", //
}
