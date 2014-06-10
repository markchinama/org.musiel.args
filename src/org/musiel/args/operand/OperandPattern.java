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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.musiel.args.operand.OperandException.Reason;

public class OperandPattern {

	// IMPORTANT: GnuMonoTermPrinter is using a related pattern, any future change should be done in both classes
	private static final Pattern TOKENS = Pattern.compile( "^(?:(" //
			+ "[a-zA-Z\\-0-9]+)" + "|(" // operand name: input-file, a, b
			+ "\\[|\\]" + "|" // optional: [ input-file ]
			+ "\\.\\.\\." + "|" // repeatable: input-file...
			+ "\\(|\\)" + "|" // parentheses: ( input-data input-style )...
			+ "\\|" // selection: [ input-file | input-data input-style ]
			+ "))\\s*");

	private static List< String> tokenize( final String pattern, final Set< String> names) {
		final List< String> tokens = new ArrayList<>();
		Matcher matcher;
		for( CharSequence remaining = pattern.trim(); remaining.length() > 0; remaining =
				remaining.subSequence( matcher.group().length(), remaining.length()))
			if( !( matcher = OperandPattern.TOKENS.matcher( remaining)).find())
				throw new IllegalArgumentException( "invalid pattern: " + pattern);
			else if( matcher.group( 1) == null)
				tokens.add( matcher.group( 2));
			else {
				names.add( matcher.group( 1));
				tokens.add( matcher.group( 1));
			}
		return tokens;
	}

	private String pattern = null;
	private List< String> names;

	public String getPattern() {
		return this.pattern;
	}

	public List< String> getNames() {
		return this.names;
	}

	public static OperandPattern compile( final String pattern) {
		final Set< String> names = new LinkedHashSet<>();
		final List< String> tokens = OperandPattern.tokenize( pattern, names);
		final OperandPattern matcher =
				tokens.isEmpty()? new OperandPattern(): OperandPattern
						.compile( tokens.toArray( new String[ tokens.size()]), 0, tokens.size());
		matcher.pattern = pattern;
		matcher.names = Collections.unmodifiableList( new LinkedList<>( names));
		return matcher;
	}

	private static int findRightBound( final String left, final String right, final String[] tokens, final int from, final int limit) {
		for( int level = 1, pointer = from; pointer < limit; ++pointer)
			if( left.equals( tokens[ pointer]))
				++level;
			else if( right.equals( tokens[ pointer])) {
				--level;
				if( level == 0)
					return pointer;
			}
		throw new IllegalArgumentException( "\"" + right + "\" expected");
	}

	private static OperandPattern compile( final String[] tokens, final int offset, final int length) {
		final LinkedList< LinkedList< OperandPattern>> alternatives = new LinkedList<>(); // parts separated by '|'
		LinkedList< OperandPattern> alternative = new LinkedList<>(); // concatenated parts in one alternative
		for( int pointer = offset; pointer < offset + length; ++pointer)
			switch( tokens[ pointer]) {
				case "...":
					if( alternative.isEmpty())
						throw new IllegalArgumentException( "unexpected \"...\"");
					alternative.getLast().repeat();
					break;
				case "(": // just feeling lazy, did the two at once...
				case "[":
					final int rightParenthesis =
							OperandPattern.findRightBound( tokens[ pointer], "(".equals( tokens[ pointer])? ")": "]", tokens, pointer + 1,
									offset + length);
					alternative.add( OperandPattern.compile( tokens, pointer + 1, rightParenthesis - pointer - 1));
					if( "[".equals( tokens[ pointer]))
						alternative.getLast().optional();
					pointer = rightParenthesis;
					break;
				case "|":
					if( alternative.isEmpty())
						throw new IllegalArgumentException( "unexpected \"|\"");
					alternatives.add( alternative);
					alternative = new LinkedList<>();
					break;
				default:
					alternative.add( new OperandPattern( tokens[ pointer]));
			}

		if( alternative.isEmpty())
			// empty pattern won't be passed in, so there must be something beyond the boundary
			throw new IllegalArgumentException( "token expected before \"" + tokens[ offset + length] + "\"");
		alternatives.add( alternative);
		return OperandPattern.combineAlternatives( alternatives);
	}

	private static OperandPattern combineAlternatives( final LinkedList< LinkedList< OperandPattern>> machines) {
		final OperandPattern alternatives = OperandPattern.concatenate( machines.removeFirst());
		for( final LinkedList< OperandPattern> machine: machines)
			alternatives.alternative( OperandPattern.concatenate( machine));
		return alternatives;
	}

	private static OperandPattern concatenate( final LinkedList< OperandPattern> machines) {
		final OperandPattern concatenated = machines.removeFirst();
		for( final OperandPattern machine: machines)
			concatenated.concatenate( machine);
		return concatenated;
	}

	private final State initialState;
	private final Set< State> nonInitialStates = new HashSet<>();

	// ◎
	private OperandPattern() {
		this.initialState = new State( true);
	}

	// ● --- token ---> ◎
	private OperandPattern( final String token) {
		this.initialState = new State( false);
		final State destination = new State( true);
		this.nonInitialStates.add( destination);
		this.initialState.transitions.put( destination, token);
	}

	// @formatter:off
	// ● ---> ◎
	// becomes
	// ◎---> ◎
	// @formatter:on
	private void optional() {
		this.initialState.finalState = true;
	}

	// @formatter:off
	// ● --- 1 ---> ○ --- 2 ---> ◎
	// becomes
	// ● --- 1 ---> ○ --- 2 ---> ◎
	//              ^            |
	//              `---- 1 -----'
	// @formatter:on
	private void repeat() {
		for( final State nonInitialState: this.nonInitialStates)
			if( nonInitialState.finalState)
				nonInitialState.transitions.putAll( this.initialState.transitions);
	}

	// @formatter:off
	// ● --- 1 ---> ○ --- 2 ---> ◎
	//              ^            |
	//              `---- 1 -----'
	// +
	// ● --- 3 ---> ○ --- 4 ---> ◎
	// becomes
	// ● --- 1 ---> ○ --- 2 ---> ◎
	// |            ^            |
	// |            `---- 1 -----'
	// |
	// '---- 3 ---> ○ --- 4 ---> ◎
	// @formatter:on
	private void alternative( final OperandPattern alternative) {
		this.initialState.transitions.putAll( alternative.initialState.transitions);
		if( alternative.initialState.finalState)
			this.initialState.finalState = true;
		this.nonInitialStates.addAll( alternative.nonInitialStates);
	}

	// @formatter:off
	// ● --- 1 ---> ○ --- 2 ---> ◎
	//              ^            |
	//              `---- 1 -----'
	// +
	// ● --- 3 ---> ○ --- 4 ---> ◎
	// becomes
	// ● --- 1 ---> ○ --- 2 ---> ○ ---.
	//              ^            |     |
	//              `---- 1 -----'     |
	//                                 |
	//              ,-----3------------'
	//              |
	//              v
	//              ○ --- 4 ---> ◎
	// @formatter:on
	private void concatenate( final OperandPattern machine) {
		final List< State> finalStates = new LinkedList<>();
		if( this.initialState.finalState)
			finalStates.add( this.initialState);
		for( final State nonInitialState: this.nonInitialStates)
			if( nonInitialState.finalState)
				finalStates.add( nonInitialState);

		for( final State finalState: finalStates) {
			finalState.transitions.putAll( machine.initialState.transitions);
			finalState.finalState = machine.initialState.finalState;
		}
		this.nonInitialStates.addAll( machine.nonInitialStates);
	}

	public boolean isAmbiguous() {
		return this.findAmbiguityExample() != null;
	}

	public String[][] findAmbiguityExample() {
		final State[] states = new State[ this.nonInitialStates.size() + 1];
		states[ 0] = this.initialState; // initial state is assumed at index 0 below
		int index = 1;
		for( final State state: this.nonInitialStates)
			states[ index++] = state;
		final Map< State, Integer> stateIndices = new HashMap<>();
		for( int i = 0; i < states.length; ++i)
			stateIndices.put( states[ i], Integer.valueOf( i));

		// an additional vertex is added to represent a single final state, every actual final state has a transition to there
		final int graphSize = states.length + 1;
		final boolean[][] graph = new boolean[ graphSize][ graphSize];
		for( final State from: states)
			for( final State to: from.transitions.keySet())
				graph[ stateIndices.get( from).intValue()][ stateIndices.get( to).intValue()] = true;
		// this is adding additional transitions mentioned above
		for( final State state: states)
			if( state.finalState)
				graph[ stateIndices.get( state).intValue()][ states.length] = true;

		// now the task is to find a pair of different paths from state 0 to the final state with the same length. we construct a new graph
		// of V * V * { true, false}, where each V corresponds to one of the paths, and the boolean dimension indicates whether the two
		// paths have already been different
		final boolean[][] paired = new boolean[ graphSize * graphSize * 2][ graphSize * graphSize * 2];
		final boolean[] BOOL = new boolean[]{ false, true};
		for( int from1 = 0; from1 < graphSize; ++from1)
			for( int from2 = 0; from2 < graphSize; ++from2)
				for( int fromD = 0, from = graphSize * 2 * from1 + 2 * from2; fromD < 2; ++fromD, ++from)
					for( int to1 = 0; to1 < graphSize; ++to1)
						for( int to2 = 0; to2 < graphSize; ++to2)
							for( int toD = 0, to = graphSize * 2 * to1 + 2 * to2; toD < 2; ++toD, ++to)
								paired[ from][ to] =
										graph[ from1][ to1] && graph[ from2][ to2] && BOOL[ toD] == ( BOOL[ fromD] || from1 != from2 || to1 != to2);
		// 0 = initial state WITHOUT separation; .length - 1 = the added final state WITH separation.
		// If such a path exists, it means there are at least two different paths connects the initial state and the added final state in
		// the same number of steps
		final int[] pathPair = OperandPattern.findShortestPath( paired, 0, paired.length - 1);
		if( pathPair == null)
			return null;

		// -1 is to drop the initial step. the added final step never got into the result of shorted path searching
		final String[][] paths = new String[ 2][ pathPair.length - 1];
		State state1 = this.initialState;
		State state2 = this.initialState;
		for( int i = 1; i < pathPair.length; ++i) {
			final State nextState1 = states[ pathPair[ i] / 2 / graphSize];
			final State nextState2 = states[ pathPair[ i] / 2 % graphSize];
			paths[ 0][ i - 1] = state1.transitions.get( nextState1);
			paths[ 1][ i - 1] = state2.transitions.get( nextState2);
			state1 = nextState1;
			state2 = nextState2;
		}
		return paths;
	}

	private static int[] findShortestPath( final boolean[][] graph, final int from, final int to) {
		final LinkedHashMap< Integer, int[]> toBeVisited = new LinkedHashMap<>();
		toBeVisited.put( Integer.valueOf( from), new int[]{});
		final boolean[] visitedOrQueued = new boolean[ graph.length];
		visitedOrQueued[ from] = true;
		while( !toBeVisited.isEmpty()) {
			final Integer beingVisited = toBeVisited.keySet().iterator().next();
			final int[] path = toBeVisited.remove( beingVisited);
			final int[] updatedPath = Arrays.copyOf( path, path.length + 1);
			updatedPath[ updatedPath.length - 1] = beingVisited.intValue();
			final boolean[] adjacencyRow = graph[ beingVisited.intValue()];
			for( int connected = 0; connected < adjacencyRow.length; ++connected)
				if( adjacencyRow[ connected]) {
					if( connected == to)
						return updatedPath; // see? to is not added to the result
					if( visitedOrQueued[ connected])
						continue;
					visitedOrQueued[ connected] = true;
					toBeVisited.put( Integer.valueOf( connected), updatedPath);
				}
		}
		return null;
	}

	public boolean isMoreThanOneOperandsPossible() {
		for( final State state: this.initialState.transitions.keySet())
			if( !state.transitions.isEmpty())
				return true;
		return false;
	}

	public boolean isEmptyPossible() {
		return this.initialState.finalState;
	}

	public boolean isMultipleOccurrencePossible( final String operandName) {
		if( !this.names.contains( operandName))
			throw new IllegalArgumentException();

		final Set< State> allStates = new HashSet<>( this.nonInitialStates);
		allStates.add( this.initialState);

		final Set< State> metThatOperand = new HashSet<>();
		for( final State state: allStates)
			for( final Entry< State, String> transition: state.transitions.entrySet())
				if( operandName.equals( transition.getValue()))
					metThatOperand.add( transition.getKey());

		for( int sizeWas = -1, sizeIs = metThatOperand.size(); sizeWas != sizeIs; sizeWas = sizeIs, sizeIs = metThatOperand.size()) {
			final Set< State> extension = new HashSet<>();
			for( final State state: metThatOperand)
				for( final Entry< State, String> transition: state.transitions.entrySet())
					if( operandName.equals( transition.getValue()))
						return true;
					else
						extension.add( transition.getKey());
			metThatOperand.addAll( extension);
		}
		return false;
	}

	public boolean isAbsencePossible( final String operandName) {
		if( this.initialState.finalState)
			return true;
		// try to find a path from initial state to any final state, with all transitions on the specified operand ignored
		final LinkedList< State> toBeVisited = new LinkedList<>();
		toBeVisited.add( this.initialState);
		final HashSet< State> visitedOrQueued = new HashSet<>();
		visitedOrQueued.add( this.initialState);
		while( !toBeVisited.isEmpty()) {
			final State beingVisited = toBeVisited.removeFirst();
			for( final Entry< State, String> transition: beingVisited.transitions.entrySet())
				if( operandName.equals( transition.getValue()))
					continue;
				else if( transition.getKey().finalState)
					return true;
				else if( visitedOrQueued.contains( transition.getKey()))
					continue;
				else {
					visitedOrQueued.add( transition.getKey());
					toBeVisited.addLast( transition.getKey());
				}
		}
		return false;
	}

	private static class Explorer {

		private final String[] path;
		private final State state;

		public Explorer( final String[] path, final State state) {
			super();
			this.path = path;
			this.state = state;
		}
	}

	public Set< String[]> getSequences( final int length) {
		List< Explorer> explorers = new LinkedList<>();
		explorers.add( new Explorer( new String[ 0], this.initialState));
		for( int i = 0; i < length; ++i) {
			final List< Explorer> updatedExplorers = new LinkedList<>();
			for( final Explorer explorer: explorers)
				for( final Entry< State, String> transition: explorer.state.transitions.entrySet()) {
					final String[] updatedPath = Arrays.copyOf( explorer.path, explorer.path.length + 1);
					updatedPath[ updatedPath.length - 1] = transition.getValue();
					updatedExplorers.add( new Explorer( updatedPath, transition.getKey()));
				}
			explorers = updatedExplorers;
		}
		final Set< String[]> result = new HashSet<>();
		for( final Explorer explorer: explorers)
			if( explorer.state.finalState)
				result.add( explorer.path);
		return result;
	}

	public Map< String, List< String>> match( final String... operands) throws OperandException {
		final List< String> list = new LinkedList<>();
		Collections.addAll( list, operands);
		return this.match( list);
	}

	public Map< String, List< String>> match( final List< String> operands) throws OperandException {
		List< Explorer> explorers = new LinkedList<>();
		explorers.add( new Explorer( new String[ 0], this.initialState));
		for( int i = 0; i < operands.size(); ++i) {
			final List< Explorer> updatedExplorers = new LinkedList<>();
			for( final Explorer explorer: explorers)
				for( final Entry< State, String> transition: explorer.state.transitions.entrySet()) {
					final String[] updatedPath = Arrays.copyOf( explorer.path, explorer.path.length + 1);
					updatedPath[ updatedPath.length - 1] = transition.getValue();
					updatedExplorers.add( new Explorer( updatedPath, transition.getKey()));
				}
			explorers = updatedExplorers;

			if( explorers.isEmpty())
				throw new OperandException( Reason.TOO_MANY);
		}

		final Set< String[]> haltingPaths = new HashSet<>();
		for( final Explorer explorer: explorers)
			if( explorer.state.finalState)
				haltingPaths.add( explorer.path);
		if( haltingPaths.isEmpty())
			throw new OperandException( Reason.TOO_FEW);
		if( haltingPaths.size() > 1)
			throw new IllegalStateException( "the pattern is ambiguous, should not be used for matching");
		final String[] path = haltingPaths.iterator().next();

		final Map< String, List< String>> result = new TreeMap<>();
		final Iterator< String> operandIterator = operands.iterator();
		for( final String operandName: path) {
			List< String> list = result.get( operandName);
			if( list == null) {
				list = new LinkedList<>();
				result.put( operandName, list);
			}
			list.add( operandIterator.next());
		}
		for( final String operandName: this.names)
			if( !result.containsKey( operandName))
				result.put( operandName, new LinkedList< String>());
		return result;
	}

	private static class State {

		boolean finalState = true;
		// none of the four operations adds different tokens for the same destination, so just use a map
		Map< State, String> transitions = new HashMap<>();

		State( final boolean finalState) {
			this.finalState = finalState;
		}
	}
}
