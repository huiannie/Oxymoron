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
package compiler.blocks;

import java.util.ArrayList;

import compiler.binder.Context;
import compiler.classes.ClassBlock;
import compiler.commands.Command;
import compiler.conditionals.If;
import compiler.conditionals.Select;
import compiler.declarations.Declaration;
import compiler.loops.For;
import compiler.loops.Loop;
import compiler.main.Settings;
import compiler.parser.Scope;
import compiler.util.BugTrap;
import compiler.util.IOUtils;

public class Block {
	public static final boolean Debug = Settings.Debug;

	protected static final int ExitCode_OK = 0;
	protected static final int ExitCode_Return = 1;
	
	protected int startLineNumber; // inclusive
	protected int endLineNumber;   // exclusive
	protected boolean hasHeaderAndFooter;  // some blocks are identified by a header line. Eg: Module, Function, If, While.
	protected boolean selfContained;  // Variables and constants (except globals) cannot be search for outside of the bound of this block.
	
	public Block parent = null;
	public ArrayList<Block> subblocks = new ArrayList<Block>();


	public Block(int startLineNumber, int endLineNumber) {
		this.startLineNumber = startLineNumber;
		this.endLineNumber = endLineNumber;
		this.hasHeaderAndFooter = false;
		this.selfContained = false;  // most blocks are not self-contained, except for Program, Class and Methods.
	}
	
	
	public static boolean startsWithAKeyword(String line) {
		if (ClassBlock.startsWithAKeyword(line)) return true;
		else if (Method.startsWithAKeyword(line)) return true;
		else if (Command.startsWithAKeyword(line)) return true;
		else if (Declaration.startsWithAKeyword(line)) return true;
		else if (If.startsWithAKeyword(line)) return true;
		else if (Select.startsWithAKeyword(line)) return true;
		else if (Loop.startsWithAKeyword(line)) return true;
		else if (For.startsWithAKeyword(line)) return true;
		return false;
	}

	
	
	public int getStartLineNumber() {
		return startLineNumber;
	}
	public int getEndLineNumber() {
		return endLineNumber;
	}
	
	public boolean isSelfContained() {
		return selfContained;
	}
	
	public void add(Block subblock) {
		subblocks.add(subblock);
	}


	public void print(int indent) {
		IOUtils.printIndented(indent, "Block is a " + this.getClass().getSimpleName() 
								+ " range: " + startLineNumber + "-" + endLineNumber);
		if (!subblocks.isEmpty()) {
			IOUtils.printIndented(indent, "Start of subblocks:");
			for (Block block : subblocks) {
				block.print(indent+2);
			}
			IOUtils.printIndented(indent, "End of subblocks");
		}		
	}

	
	
	public void parse(ArrayList<String> code, Scope scope, Block parent) throws BugTrap {
		if (this.parent==null) this.parent = parent;

		int startAt;
		int endAt;
		
		if (!hasHeaderAndFooter) {
			// Generic block without any keywords as markers.
			startAt = getStartLineNumber();
			endAt = getEndLineNumber();
		}
		else {
			// Specific block with keyword as start and end markers. Skip those markers.
			// Those markers are supposed to be processed by the specific blocks.
			startAt = getStartLineNumber()+1;
			endAt = getEndLineNumber()-1;
		}
		
		if (Debug) {
			IOUtils.println("Block: parsing block at lines " + startAt + " to " + endAt);
		}
		
		
		int index = startAt;
		while (index<endAt) {
			Block subblock = scope.findBlockAtLine(index);
			if (subblock==null) {  
				// No block starts at this line. 
				// This line could contain a keyword like Else, ElseIf, Case, etc which belong to a block.
				if (Debug) IOUtils.printIndented(0, "Skip parsing line " + index + ": " + code.get(index));
				String unrecongizedLine = code.get(index);
				index++;
				throw new BugTrap(this, "Block parse unrecognized line: " + unrecongizedLine);
			}
			else {
				if (Debug) IOUtils.printIndented(0, "Checking subblock " + subblock.getClass().getSimpleName() 
						+ " of line(s) " + subblock.getStartLineNumber() + "-" + subblock.getEndLineNumber());
				add(subblock);
				subblock.parse(code, scope, this);
				index = subblock.getEndLineNumber();
			}
		}
	}


	
	
	
	
	public int execute(Context context) throws BugTrap {
		if (Debug) IOUtils.println("In block " + this.getClass().getSimpleName());
		for (Block subblock : subblocks) {
			if ((subblock instanceof ClassBlock) || (subblock instanceof Method)) {
				if (Debug) IOUtils.println("Skipping subblock " + subblock.getClass().getName() + " in " + this.getClass().getSimpleName());
			}
			else {
				Context subContext = new Context(subblock, context);
				int exitCode = subblock.execute(subContext);
				if (exitCode==ExitCode_Return) return ExitCode_Return;
			}
		}
		return ExitCode_OK;
	}

}
