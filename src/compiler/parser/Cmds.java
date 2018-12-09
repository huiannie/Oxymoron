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

import compiler.classes.ClassBlock;
import compiler.util.IOUtils;

public class Cmds {
	public static final int UnknownLineNumber = -1;
	
	File sourceFile;
	ArrayList<String> lines;
	int[] rawlineNumberMatch; // A match from rawlines to processed lines.
	HashMap<Integer, ArrayList<Integer>> lineNumberMatch;
	StringsMap stringsMap;
	String[] rawlines;

	// Assume that when not specified, the rawline numbers start at 1.
	public String matchRawlineNumbersAsString(int lineNumber) {
		return matchRawlineNumbersAsString(lineNumber, true);
	}
	
	public String matchRawlineNumbersAsString(int lineNumber, boolean startAt1) {
		
		// Remember that line numbers stored start at zero.
		// But when they are displayed, they are offset by 1.
		ArrayList<Integer> range = getRawlineNumber(lineNumber);

		String s = "";
		if (range==null) {}
		else if (range.size()==1) {
			if (startAt1)
				s = "" + (range.get(0)+1);
			else
				s = "" + range.get(0);
		}
		else {
			int start = range.get(0);  // inclusive
			int end = range.get(range.size()-1);  // inclusive
			if (startAt1)
				s = "" + (start+1) + "-" + (end+1);
			else
				s = "" + start + "-" + end;
		}
		return s;
	}
	
	
	public ArrayList<Integer> getRawlineNumber(int lineNumber) {
		if (lineNumberMatch.containsKey(lineNumber)) return lineNumberMatch.get(lineNumber);
		else {
			IOUtils.println("Cannot match line " + lineNumber);
			return null;
		}
	}
	public String getRawlines(int lineNumber) {
		String raw = "";
		for (int i : getRawlineNumber(lineNumber)) {
			raw += rawlines[i] + "; ";
		}
		return raw;
	}
	public String getRawlines(int startLineNumber, int endLineNumber) {
		String raw = "";
		for (int j=startLineNumber; j<endLineNumber; j++) {
			for (int i : getRawlineNumber(j)) {
				raw += rawlines[i] + "; ";
			}
		}
		return raw;
	}
	
	public String[] getRawLines() {
		return rawlines;
	}
	public int getNumberOfLines() {
		return lines.size();
	}
	
	public String getFilename() {
		return sourceFile.getName();
	}

	public String getCmdLine(int index) {
		return lines.get(index);
	}
	
	void printCmdLines(int indent) {
		IOUtils.printIndented(indent, "Listing of all commands:");
		for (int index=0; index<lines.size(); index++)
			IOUtils.printIndented(indent+1, "line " + index + ": " + lines.get(index));
		IOUtils.printIndented(indent, "End listing of all commands.");
	}

	
	public StringsMap getStringsMap() {
		return stringsMap;
	}

	public boolean isAClassFile() {
		String firstLine = getCmdLine(0);
		if (ClassBlock.startsWithAKeyword(firstLine)) return true;
		else return false;
	}

	public boolean isEmpty() {
		return lines==null || lines.size()==0;
	}
}
