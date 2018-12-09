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
package compiler.parser;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import compiler.blocks.Block;
import compiler.main.Settings;
import compiler.tokenizers.Token;
import compiler.util.BugTrap;
import compiler.util.FileUtils;
import compiler.util.IOUtils;
import examples.Node;



public class Preprocessor {
	public static final boolean Debug = Settings.Debug;

	private static final String StringTokenMarker = "___STRINGMARKER";

	private int stringTokenCount = 0;
	private Cmds cmds;

	public Preprocessor(File sourceFile) throws BugTrap {
		cmds = new Cmds();
		
		cmds.sourceFile = sourceFile;
		
		// Resource file 
		if (sourceFile instanceof Node) 
			cmds.rawlines = ((Node) sourceFile).readlines();
		else // user-provided file
			cmds.rawlines = FileUtils.readline(sourceFile);
		if (Debug) FileUtils.printlines(cmds.rawlines);
		
		
		preprocess(cmds.rawlines);

		if (Debug) {
			IOUtils.println("After preprocessing:");
			cmds.printCmdLines(0);
		}
	}
	
	
	
	void preprocess(String rawlines[]) throws BugTrap {

		ArrayList<String> lines = new ArrayList<String>();
		StringsMap stringsMap = new StringsMap();

		
		int[] rawlineNumberMatch = new int[rawlines.length];
		HashMap<Integer, ArrayList<Integer>> lineNumberMatch = new HashMap<Integer, ArrayList<Integer>>();

		for (int index=0; index<rawlines.length; index++) {
			if (Debug) IOUtils.println("Preprocessing line " + index + ": " + rawlines[index]);
			
			rawlineNumberMatch[index] = -1;  // Assume current raw line may not be retained.
			
			// Trim off white spaces in front and behind.
			String line = rawlines[index].trim();
			
			// Skip all comments.
			if (line.startsWith("//") || line.length()==0) {
				continue;
			}
			
			// Count how many double quotes appear.
			int count = 0;
			for (int c=0; c<line.length(); c++) {
				if (line.charAt(c)=='"') {
					count++;
				}
			}
			
			// Tag each string by a label
			if (count>0) {
				int positions[] = new int[count];
				int i=0;
				for (int c=0; c<line.length(); c++) {
					if (line.charAt(c)=='"') {
						positions[i++] = c;
					}
				}
				if (positions.length%2!=0) throw new BugTrap("In file " + cmds.getFilename() + ", line " + index + ": String not closed.");

				String tagged = "";
				if (positions[0]>0) tagged += line.substring(0, positions[0]);
				for (int j=0; j<count; j+=2) {
					// j ranges from 0, 2, 4 and so on.
					// The substring between positions[j] and positions[j+1] is a string enclosed by double quotes.
					// The adding 1 after positions[j+1] simply include the ending double quote.
					String token = line.substring(positions[j], positions[j+1]+1);
					String tag = StringTokenMarker+stringTokenCount;
					stringTokenCount++;
					
					// Remove the double quote at the front and end before putting the string into the map.
					stringsMap.put(tag, token.substring(1, token.length()-1));

					tagged += tag;
					// Between positions[j+1]+1 and the next double quote at position[j+2], there is a substring not enclosed.
					if (j+2<positions.length) {
						tagged += line.substring(positions[j+1]+1, positions[j+2]);
					}
					// If positions[j+1] is the last double quote, then from positions[j+1]+1 to end-of-line, there is a subtring not enclosed.
					else if (positions[j+1]+1<line.length()) {
						tagged += line.substring(positions[j+1]+1);
					}
					else {
						
					}
				}

				
				// Overwrite string with tags
				line = tagged;
			}

			
			// Remove all white spaces before and after an open "(" bracket
			line = line.replaceAll("\\s*\\(\\s*", "(");
			// Remove all white spaces before and after a close ")" bracket
			line = line.replaceAll("\\s*\\)\\s*", ")");
			//line = line.replaceAll("\\s*\\)([A-Za-z_]+)\\s*", ") $1");
			
			// Remove all white spaces before and after an open "[" bracket
			line = line.replaceAll("\\s*\\[\\s*", "[");
			// Remove all white spaces before and after a close "]" bracket
			line = line.replaceAll("\\s*\\]\\s*", "]");


			// Remove all white spaces before and after an comma ,
			line = line.replaceAll("\\s*\\,\\s*", ",");
			// Remove all white spaces before and after a period .
			line = line.replaceAll("\\s*\\.\\s*", ".");
			// Remove all white spaces before and after a colon :
			line = line.replaceAll("\\s*:\\s*", ":");

			// Remove all white spaces before and after an equal sign
			line = line.replaceAll("\\s*=\\s*", "=");
			// Remove all white spaces before and after a plus sign
			line = line.replaceAll("\\s*\\+\\s*", "+");
			// Remove all white spaces before and after a minus sign
			line = line.replaceAll("\\s*\\-\\s*", "-");

			// Remove all white spaces before and after a * sign
			line = line.replaceAll("\\s*\\*\\s*", "*");
			// Remove all white spaces before and after a / sign
			line = line.replaceAll("\\s*/\\s*", "/");
			// Remove all white spaces before and after a ^ sign
			line = line.replaceAll("\\s*\\^\\s*", "^");

			
			// Replace multiple white spaces by a single whitespace.
			line = line.replaceAll("\\s+", " ");

			
			if (Debug) IOUtils.println("Preprocessing: " + line);
			
			// If the previous line ends with comma, then append this line to previous line.
			// Otherwise, treat this as a new line.
			if (lines.isEmpty()) {
				lines.add(line);
				rawlineNumberMatch[index] = lines.size()-1;				
			}
			else if (lines.get(lines.size()-1).endsWith(",")
					|| lines.get(lines.size()-1).endsWith("+") 
					|| lines.get(lines.size()-1).endsWith("-") 
					|| lines.get(lines.size()-1).endsWith("*") 
					|| lines.get(lines.size()-1).endsWith("/") 
					// Also add any line that does not begin with a keyword
					|| !Block.startsWithAKeyword(line)
					) {

				// Before merging the current line with the previous line,
				// check if the previous line ends with a normal letter.
				// If it does, then must add a space before merging.
				
				String previousLine = lines.get(lines.size()-1);
				char lastchar = previousLine.charAt(previousLine.length()-1);
				if (Token.isLetterOrDigit(lastchar) || Token.isUnderscore(lastchar)) 
					previousLine = previousLine + " ";

				// Now ready to merge the current line with the previous line.
				line = previousLine + line;
				lines.set(lines.size()-1, line);
				// Current raw line is merged to previous processed line.
				rawlineNumberMatch[index] = lines.size()-1;
			}
			else { 
				lines.add(line);
				// Current raw line is accepted as a processed line.
				rawlineNumberMatch[index] = lines.size()-1;				
			}
		}
		
		for (int index=0; index<rawlines.length; index++) {
			if (rawlineNumberMatch[index]!=-1) {
				int lineNumber = rawlineNumberMatch[index];
				
				if (Debug) IOUtils.println("Matching rawline " + index + " to line " + rawlineNumberMatch[index]);
				
				if (lineNumberMatch.containsKey(lineNumber)) { // a match from lines to rawlines already exists
					ArrayList<Integer> match = lineNumberMatch.get(lineNumber);
					match.add(index);
					lineNumberMatch.put(lineNumber, match);
				}
				else {
					ArrayList<Integer> match = new ArrayList<Integer>();
					match.add(index);
					lineNumberMatch.put(lineNumber, match);
				}
			}
		}


		
		cmds.rawlineNumberMatch = rawlineNumberMatch;
		cmds.lineNumberMatch = lineNumberMatch;
		cmds.lines = lines;
		cmds.stringsMap = stringsMap;
		
	}

	public Cmds getCmds() {
		return cmds;
	}
	
}
