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
package compiler.classes;

import compiler.main.Settings;
import compiler.parser.Cmds;
import compiler.parser.ScopeParser;
import compiler.util.BugTrap;
import compiler.util.IOUtils;

public class ClassParser extends ScopeParser {
	public static final boolean Debug = Settings.Debug;

	private ClassScope scope;
	
	public ClassParser(Cmds cmds) throws BugTrap {
		// Do not use the parse() method from the super class. 
		// Instead, use the one customized in this class.
		super(cmds, false);
		this.parse();
		scope.getClassBlock().parse(lines, scope, null);
	}
	
	
	private ClassBlock parseClassBlock() throws BugTrap {
		int count = 0;
		for (int index=0; index<lines.size(); index++) {
			String line = lines.get(index);			
			if (ClassBlock.isAStart(line)) {
				count++;
			}
		}
		if (Debug) IOUtils.println("Parsing Class: Total " + count + " found.");
		
		if (count>1) {
			// For now, require every file to have up to 1 class.
		}

		int start = 0;  // first line
		int end = lines.size()-1;  // last line (inclusive)
		
		if (ClassBlock.isAStart(lines.get(start)) && 
				ClassBlock.isAnEnd(lines.get(end))) {
			// A class is explicitly defined
			ClassBlock classblock = new ClassBlock(start, end+1);
			return classblock;
		}
		else {
			// No class is explicitly defined
			return null;
		}
	}	
	
	private void parse() throws BugTrap {
		scope = new ClassScope(cmds);
		scope.commands=parseCommands();
		scope.declarations=parseDeclarations();
		scope.loops=parseLoops();
		scope.fors=parseFors();
		scope.modules=parseModules();
		scope.functions=parseFunctions();
		scope.selects=parseSelects();
		scope.ifs=parseIfs();
		
		// Check if the file is a class.
		// If not, then treat it as a program.
		scope.classBlock = parseClassBlock();
		
		if (Debug) scope.print(0);
		
		verify(scope, Debug);
	}
	


	private String[] verify(ClassScope scope, boolean verbose) {
		int indent = 0;
		int counts[] = new int[lines.size()];
		String status[] = new String[lines.size()];
		
		
		// First scan: Check that every line has at most one block that starts at this line.
		// Note that a line may be within the scope of many nested blocks, but only one
		// block can start at this line.
		for (int index=0; index<lines.size(); index++) {
			counts[index] = 0; // initialize
			status[index] = "OK";

			if (scope.commands[index]!=null) {
				counts[index]++;
				if (scope.commands[index].getStartLineNumber()!=index) {
					status[index] = "Mismatched startLineNumber for block at line " + index;
				}
			}
			if (scope.declarations[index]!=null) {
				counts[index]++;
				if (scope.declarations[index].getStartLineNumber()!=index) {
					status[index] = "Mismatched startLineNumber for block at line " + index;
				}
			}
			if (scope.selects[index]!=null) {
				counts[index]++;
				if (scope.selects[index].getStartLineNumber()!=index) {
					status[index] = "Mismatched startLineNumber for block at line " + index;
				}
			}
			if (scope.modules[index]!=null) {
				counts[index]++;
				if (scope.modules[index].getStartLineNumber()!=index) {
					status[index] = "Mismatched startLineNumber for block at line " + index;
				}
			}
			if (scope.functions[index]!=null) {
				counts[index]++;
				if (scope.functions[index].getStartLineNumber()!=index) {
					status[index] = "Mismatched startLineNumber for block at line " + index;
				}
			}
			if (scope.loops[index]!=null) {
				counts[index]++;
				if (scope.loops[index].getStartLineNumber()!=index) {
					status[index] = "Mismatched startLineNumber for block at line " + index;
				}
			}
			if (scope.fors[index]!=null) {
				counts[index]++;
				if (scope.fors[index].getStartLineNumber()!=index) {
					status[index] = "Mismatched startLineNumber for block at line " + index;
				}
			}
			if (scope.ifs[index]!=null) {
				counts[index]++;
				if (scope.ifs[index].getStartLineNumber()!=index) {
					status[index] = "Mismatched startLineNumber for block at line " + index;
				}
			}
			
			if (scope.classBlock!=null && (scope.classBlock.getStartLineNumber()==index || scope.classBlock.getEndLineNumber()==index)) {
				counts[index]++;
			}
			
			// After all counting, verify that the total at each line is at most 1.
			if (counts[index]==1) {
				status[index] = "line " + index + " parsed.";
			}
			else {
				status[index] = "*****Unmatched line*****" + index + ": " + lines.get(index);
			}
		}

		
		
		if (verbose) {
			IOUtils.printIndented(indent, "******** Scopes of lines: *********");
			for (int index=0; index<status.length; index++) 
				IOUtils.printIndented(indent, status[index]);
			IOUtils.printIndented(indent, "******** All scopes verified *********");
		}
		return status;
	}
	
	public ClassScope getScope() throws BugTrap {
		if (scope==null) parse();
		return scope;
	}
	
}
