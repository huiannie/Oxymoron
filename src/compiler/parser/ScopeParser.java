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

import java.util.ArrayList;
import java.util.Stack;

import compiler.blocks.Block;
import compiler.blocks.Function;
import compiler.blocks.Module;
import compiler.commands.Command;
import compiler.conditionals.If;
import compiler.conditionals.Select;
import compiler.declarations.Declaration;
import compiler.loops.For;
import compiler.loops.Loop;
import compiler.main.Settings;
import compiler.util.BugTrap;
import compiler.util.IOUtils;

public class ScopeParser {
	public static final boolean Debug = Settings.Debug;

	protected ArrayList<String> lines;
	protected Cmds cmds;
	private Scope scope = null;
	
	public ScopeParser(Cmds cmds) throws BugTrap {
		this(cmds, true);
	}
	
	// Allow sub-classes to decide whether to use the parse() method or 
	// to use their own overridden ones.
	protected ScopeParser(Cmds cmds, boolean parse) throws BugTrap {
		this.cmds = cmds;
		this.lines = cmds.lines;
		if (parse) this.parse();
	}
	
	protected Block[] parseCommands() throws BugTrap {
		Block[] blocks;
		blocks = new Block[lines.size()];
		for (int index=0; index<blocks.length; index++) {
			String line = lines.get(index);
			blocks[index] = null;
			if (Command.isACommand(line))
				blocks[index] = Command.create(line, index);
		}
		return blocks;
	}
	protected Block[] parseDeclarations() throws BugTrap {
		Block[] blocks;
		blocks = new Block[lines.size()];
		for (int index=0; index<blocks.length; index++) {
			String line = lines.get(index);
			blocks[index] = null;
			if (Declaration.isADeclaration(line)) 
				blocks[index] = new Declaration(index);
		}
		return blocks;
	}
	
	
	// Assume that modules cannot be nested.
	protected Block[] parseModules() throws BugTrap {
		Block[] blocks;
		
		int count = 0;
		for (int index=0; index<lines.size(); index++) {
			String line = lines.get(index);
			if (Module.isAStart(line)) {
				// Found the start of a module
				count++;
			}
		}
		if (Debug) IOUtils.println("Parsing modules: Total " + count + " found.");
		
		int start[] = new int[count];
		int end[] = new int[count];
		
		
		int countStart = 0;
		int countEnd = 0;
		for (int index=0; index<lines.size(); index++) {
			String line = lines.get(index);			
			if (Module.isAStart(line)) {
				start[countStart++] = index;
			}
			else if (Module.isAnEnd(line)) {
				end[countEnd++] = index;
			}
		}
		if (countStart!=countEnd) throw new BugTrap("Module definitions mismatch.");

		
		blocks = new Block[lines.size()];
		for (int index=0; index<blocks.length; index++) {
			blocks[index] = null;
		}		
		for (int p=0; p<start.length; p++) {
			int startline = start[p];
			int endline = end[p]+1;  // end boundary is exclusive
			blocks[startline] = new Module(startline, endline);
			
		}
		return blocks;
	}

	
	// Assume that functions cannot be nested.
	protected Block[] parseFunctions() throws BugTrap {
		Block[] blocks;
		
		int count = 0;
		for (int index=0; index<lines.size(); index++) {
			String line = lines.get(index);			
			if (Function.isAStart(line)) {
				// Found the start of a function
				count++;
			}
		}
		if (Debug) IOUtils.println("Parsing functions: Total " + count + " found.");

		int start[] = new int[count];
		int end[] = new int[count];
				
		int countStart = 0;
		int countEnd = 0;
		for (int index=0; index<lines.size(); index++) {
			String line = lines.get(index);			
			if (Function.isAStart(line)) {
				start[countStart++] = index;
			}
			else if (Function.isAnEnd(line)) {
				end[countEnd++] = index;
			}
		}
		if (countStart!=countEnd) throw new BugTrap("Function definitions mismatch.");
		
		blocks = new Block[lines.size()];
		for (int index=0; index<blocks.length; index++) {
			blocks[index] = null;
		}		
		for (int p=0; p<start.length; p++) {
			int startline = start[p];
			int endline = end[p]+1;  // end boundary is exclusive
			blocks[startline] = new Function(startline, endline);
			
		}
		return blocks;
	}


	protected Block[] parseLoops() throws BugTrap {
		return Loop.parse(lines, 0, lines.size());
	}
	
	protected Block[] parseFors() throws BugTrap {
		Block[] blocks;

		// initialize blocks
		blocks = new Block[lines.size()];
		for (int index=0; index<blocks.length; index++) {
			blocks[index] = null;
		}		
		
		Stack<Integer> stack = new Stack<Integer>();
		for (int index=0; index<lines.size(); index++) {
			String line = lines.get(index);			
			if (For.isAStart(line)) {
				// Found the start of a For
				stack.push(index);
			}
			else if (For.isAnEnd(line)) {
				int start = (int) stack.pop();
				blocks[start] = new For(start, index+1);
			}
		}
		
		if (!stack.isEmpty()) throw new BugTrap("For loop definitions mismatch.");
		return blocks;
	}
	
	
	protected Block[] parseSelects() throws BugTrap {
		Block[] blocks;
		int count = 0;
		for (int index=0; index<lines.size(); index++) {
			String line = lines.get(index);
			if (Select.isAStart(line)) {
				// Found the start of a Select
				count++;
			}
		}
		
		int start[] = new int[count];
		int end[] = new int[count];
		
		int countStart = 0;
		int countEnd = 0;
		for (int index=0; index<lines.size(); index++) {
			String line = lines.get(index);
			if (Select.isAStart(line)) {
				start[countStart++] = index;
			}
			if (Select.isAnEnd(line)) {
				end[countEnd++] = index;
			}
		}
		if (countStart!=countEnd) throw new BugTrap("Select definitions mismatch.");
		
		blocks = new Block[lines.size()];
		for (int index=0; index<blocks.length; index++) {
			blocks[index] = null;
		}		
		for (int p=0; p<start.length; p++) {
			int startline = start[p];
			int endline = end[p]+1;  // end boundary is exclusive
			blocks[startline] = new Select(startline, endline);
		}
		return blocks;
	}
	
	protected Block[] parseIfs() throws BugTrap {
		Block[] blocks;

		// initialize blocks
		blocks = new Block[lines.size()];
		for (int index=0; index<blocks.length; index++) {
			blocks[index] = null;
		}		
		
		Stack<Integer> stack = new Stack<Integer>();
		for (int index=0; index<lines.size(); index++) {
			String line = lines.get(index);
			if (If.isAStart(line)) {
				// Found the start of an If
				stack.push(index);
			}
			else if (If.isAnEnd(line)) {
				int start = (int) stack.pop();
				blocks[start] = new If(start, index+1);
			}
		}
		
		if (!stack.isEmpty()) throw new BugTrap("If statement definitions mismatch.");
		return blocks;
	}
	
	
	private void parse() throws BugTrap {
		try {
			scope = new Scope(cmds);
			scope.commands=parseCommands();
			scope.declarations=parseDeclarations();
			scope.loops=parseLoops();
			scope.fors=parseFors();
			scope.modules=parseModules();
			scope.functions=parseFunctions();
			scope.selects=parseSelects();
			scope.ifs=parseIfs();
					
			if (Debug) scope.print(0);
			verify(scope, Debug);
		}
		catch (Exception e) {
			if (e instanceof BugTrap) throw e;
			else throw new BugTrap(e);
		}
	}
	


	private String[] verify(Scope scope, boolean verbose) {
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
	
	public Scope getScope() throws BugTrap {
		if (scope==null) parse();
		return scope;
	}
	

}
