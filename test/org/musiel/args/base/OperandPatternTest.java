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
package org.musiel.args.base;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

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

	private void match( final int index, final int length, final String... paths) {
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
	public void testMatching() {
		for( int index = 0; index < 2; ++index) {
			this.match( index, 0, "");
			this.match( index, 1);
			this.match( index, 2);
		}
		this.match( 2, 0);
		this.match( 2, 1);
		this.match( 2, 2, "A B", "A C");
		this.match( 2, 3);
		this.match( 3, 0);
		this.match( 3, 1);
		this.match( 3, 2, "A D");
		this.match( 3, 3, "A B D", "A C D");
		this.match( 3, 4, "A B C D");
		this.match( 3, 5);
		this.match( 5, 0, "");
		this.match( 5, 1, "A");
		this.match( 5, 2, "A B");
		this.match( 5, 3, "C D E");
		this.match( 5, 4, "A C D E");
		this.match( 5, 5, "A B C D E");
		this.match( 5, 6, "C D E C D E");
		this.match( 5, 7, "A C D E C D E");
	}
}
