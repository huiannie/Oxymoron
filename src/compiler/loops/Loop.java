/*******************************************************************************
 * Copyright (c) 2018 Annie Hui @ NVCC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package compiler.loops;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

import compiler.blocks.Block;
import compiler.main.Settings;
import compiler.tokenizers.LineMatcher;
import compiler.util.BugTrap;
import compiler.util.IOUtils;

public class Loop {
	public static final String DoKeyword = DoWhile.StartKeyword;
	public static final String UntilKeyword = DoUntil.EndKeyword;
	public static final String WhileKeyword = DoWhile.EndKeyword;
	public static final String EndWhileKeyword = While.EndKeyword;
	private static final boolean Debug = Settings.Debug;
	
	public static boolean startsWithAKeyword(String line) {
		if (LineMatcher.matchStart(line, DoKeyword)) return true;
		else if (LineMatcher.matchStart(line, UntilKeyword)) return true;
		else if (LineMatcher.matchStart(line, WhileKeyword)) return true;
		else if (LineMatcher.matchStart(line, EndWhileKeyword)) return true;
		return false;
	}
	

	
	private static boolean isADo(String line) {
		return (LineMatcher.matchExact(line, DoKeyword));
	}
	private static boolean isAnUntil(String line) {
		return (LineMatcher.matchStart(line, UntilKeyword));
	}
	private static boolean isAWhile(String line) {
		return (LineMatcher.matchStart(line, WhileKeyword));
	}
	public static boolean isAnEndWhile(String line) {
		return (LineMatcher.matchExact(line, EndWhileKeyword));
	}
	

	public static Block[] parse(ArrayList<String> code, int startAt, int endAt) throws BugTrap {
		Block[] loops = new Block[code.size()];
		for (int index=0; index<loops.length; index++) {
			loops[index] = null; // Initialize
		}

		ArrayList<Match> matches = new ArrayList<Match>();
		ArrayList<Integer> nodes = new ArrayList<Integer>();
		
		for (int index=0; index<endAt; index++) {
			String line = code.get(index);
			
			if (isADo(line)) {
				for (int m=index+1; m<endAt; m++) {
					String line2 = code.get(m);
					if (isAnUntil(line2)) {
						matches.add(new Match(DoKeyword, index, UntilKeyword, m));
					}
					else if (isAWhile(line2)) {
						matches.add(new Match(DoKeyword, index, WhileKeyword, m));						
					}
				}
				nodes.add(index);
			}
			else if (isAWhile(line)) {
				for (int m=index+1; m<endAt; m++) {
					String line2 = code.get(m);
					if (isAnEndWhile(line2)) {
						matches.add(new Match(WhileKeyword, index, EndWhileKeyword, m));
					}
				}				
				nodes.add(index);
			}
			else if (isAnUntil(line)) {
				nodes.add(index);
			}
			else if (isAnEndWhile(line)) {
				nodes.add(index);
			}
		}
		
		if (Debug) {
			IOUtils.printIndented(0, "Loop: potential matches:");
			for (Match m : matches) {
				m.print(2);
			}
			IOUtils.printIndented(0, "Loop: End potential matches.");
		}
		
		
		// Java 8 conversion from ArrayList to int[]
		int[] nodeArray = nodes.stream().mapToInt(i->i).toArray();
		ArrayList<Match> scopes = findCoverSet(nodeArray, matches);
		
		if (scopes==null) {
			if (nodes.size()>0) throw new BugTrap(startAt, endAt, "Loops mismatched");
			return loops;
		}

		// Print output for inspection
		if (Debug) {
			for (Match m : scopes) {
				m.print(2);
			}
		}
		
		for (Match m : scopes) {
			int startLine = m.lineNumber;
			int endLine = m.matchingLine+1;  // end boundary of a block is exclusive

			// Just return an incompletely parsed block. Later, call parse() within the block to parse content.
			if (m.token==WhileKeyword && m.matchingToken==EndWhileKeyword)
				loops[startLine] = new While(startLine, endLine);
			if (m.token==DoKeyword && m.matchingToken==WhileKeyword)
				loops[startLine] = new DoWhile(startLine, endLine);
			if (m.token==DoKeyword && m.matchingToken==UntilKeyword)
				loops[startLine] = new DoUntil(startLine, endLine);
			
		}
		return loops;
	}
	
	static ArrayList<Match> findCoverSet(int nodes[], ArrayList<Match> matches) throws BugTrap {
		Arrays.sort(nodes);
		
		Queue<CoverSet> queue = new LinkedList<CoverSet>();
		
		// Initialize Queue
		for (int index=0; index<matches.size(); index++) {
			Match match = matches.get(index);
			if (match.lineNumber==nodes[0]) {
				CoverSet coverset = new CoverSet(match);
				if (coverset.isValid())
					queue.add(coverset);
			}
		}
		
		while (!queue.isEmpty()) {
			// printQueue(queue);
			
			CoverSet coverset = queue.poll();
			int cover[] = coverset.getCover();
			boolean done = cover.length==nodes.length;
			
			if (!done) {
				// Find the first missing element
				int missing = -1;
				for (int i=0; i<nodes.length; i++) {
					if (i<cover.length && nodes[i]!=cover[i]) {
						missing = i;
						break;
					}
					else if (i==cover.length) {
						missing = i;
						break;
					}
				}
				// IOUtils.println("First missing element is " + nodes[missing]);
				
				for (Match m : matches) {
					if (m.lineNumber==nodes[missing]) {
						// m's first element covers one uncovered node.
						// Now need to make sure the second element does not cross over the boundary 
						// of any existing matches.
						boolean overlap = false;
						for (Match existingMatch : coverset.getMatches()) {
							if (m.lineNumber<existingMatch.lineNumber && existingMatch.lineNumber<m.matchingLine 
									&& m.matchingLine < existingMatch.matchingLine)
								overlap = true;
							else if (existingMatch.lineNumber<m.lineNumber && m.lineNumber<existingMatch.matchingLine 
									&& existingMatch.matchingLine < m.matchingLine)
								overlap = true;
						}
						if (overlap) continue;
						
						// New match does not crash with existing matches. 
						// It may now be added to create a new coverset.
						ArrayList<Match> newset = (ArrayList<Match>) coverset.getMatches().clone();
						newset.add(m);
						CoverSet newCover = new CoverSet(newset);
						if (newCover.isValid()) {
							boolean exists = false;
							for (CoverSet s : queue) {
								if (s.getCover().length==newCover.getCover().length) {
									boolean paired = true;
									for (int j=0; j<s.getCover().length; j++) 
										if (s.getCover()[j]!=newCover.getCover()[j]) paired=false;
									
									if (paired==true) {
										exists = true;
										break;
									}
								}
							}
							if (!exists)
								queue.add(newCover);
						}
					}
				}
			}
			else {
				// Found!
				return coverset.getMatches();
			}
		}
		return null;
	}
	static void printQueue(Queue<CoverSet> q){
		IOUtils.println("Start Queue");
		for (CoverSet cover : q) {
			cover.print(2);
		}
		IOUtils.println("End Queue");
	}
	
	
	static class Match {
		String token;
		int lineNumber;
		String matchingToken;
		int matchingLine;
		public Match(String token, int lineNumber, String matchingToken, int matchingLine) {
			this.token = token;
			this.lineNumber = lineNumber;
			this.matchingToken = matchingToken;
			this.matchingLine = matchingLine;
		}
		public void print(int indent) {
			IOUtils.printIndented(indent, token + "->" + matchingToken + " : (" + lineNumber + "," + matchingLine + ")");
		}
	}
	static class CoverSet {
		ArrayList<Match> matches;
		int[] element;
		public CoverSet(Match match) {
			this.matches = new ArrayList<Match>();
			this.matches.add(match);
			this.element = toArray();
		}
		public CoverSet(ArrayList<Match> matches) {
			this.matches = matches;
			this.element = toArray();
		}
		public ArrayList<Match> getMatches() {
			return matches;
		}
		private int[] toArray() {
			int[] element;
			element = new int[matches.size()*2];
			int p=0;
			for (Match m : matches) {
				element[p++] = m.lineNumber; 
				element[p++] = m.matchingLine;
			}
			Arrays.sort(element);
			return element;
		}
		int[] getCover() {
			return element;
		}
		boolean isValid() {
			int[] element = getCover();
			for (int j=0; j<element.length-1; j++) if (element[j]==element[j+1]) return false;
			return true;
		}
		void print(int indent) {
			String s = "";
			for (int e : element) s += e + " ";
			IOUtils.printIndented(indent, s);
		}
	}
	
}
