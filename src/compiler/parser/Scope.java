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

import compiler.blocks.Block;
import compiler.classes.ClassBlocksMap;
import compiler.util.BugTrap;
import compiler.util.IOUtils;

public class Scope {
	private Cmds cmds;
	
	public Block[] commands;
	public Block[] declarations;
	public Block[] modules;
	public Block[] loops;
	public Block[] fors;
	public Block[] selects;
	public Block[] ifs;
	public Block[] functions;
	
	// Only useful for a main program that calls classes.
	private ClassBlocksMap classesMap = new ClassBlocksMap();
	
	public Scope(Cmds cmds) {
		this.cmds = cmds;
	}

	// Create a map of all modules and functions belonging to this class.
	
	public Block findBlockAtLine(int lineNumber) throws BugTrap {
		if (commands[lineNumber]!=null) {
			return commands[lineNumber];
		}
		else if (declarations[lineNumber]!=null) {
			return declarations[lineNumber];
		}
		else if (modules[lineNumber]!=null) {
			return modules[lineNumber];
		}
		else if (loops[lineNumber]!=null) {
			return loops[lineNumber];
		}
		else if (fors[lineNumber]!=null) {
			return fors[lineNumber];
		}
		else if (selects[lineNumber]!=null) {
			return selects[lineNumber];
		}
		else if (ifs[lineNumber]!=null) {
			return ifs[lineNumber];
		}
		else if (functions[lineNumber]!=null) {
			return functions[lineNumber];
		}
		
		return null;
	}
	
	
	
	public void print(int indent) {
		IOUtils.printIndented(indent, "Start listing of scopes:");
				
		IOUtils.printIndented(indent+2, "Commands:");
		for (int index=0; index<commands.length; index++)
			if (commands[index]!=null) commands[index].print(indent+3);
		IOUtils.printIndented(indent+2, "End of Commands");
		
		IOUtils.printIndented(indent+2, "Declarations:");
		for (int index=0; index<declarations.length; index++)
			if (declarations[index]!=null) declarations[index].print(indent+3);
		IOUtils.printIndented(indent+2, "End of Declarations");

		IOUtils.printIndented(indent+2, "Loops:");
		for (int index=0; index<loops.length; index++)
			if (loops[index]!=null) loops[index].print(indent+3);
		IOUtils.printIndented(indent+2, "End of Loops");

		IOUtils.printIndented(indent+2, "Fors:");
		for (int index=0; index<fors.length; index++)
			if (fors[index]!=null) fors[index].print(indent+3);
		IOUtils.printIndented(indent+2, "End of fors");

		IOUtils.printIndented(indent+2, "Selects:");
		for (int index=0; index<selects.length; index++)
			if (selects[index]!=null) selects[index].print(indent+3);
		IOUtils.printIndented(indent+2, "End of Selects");
		
		IOUtils.printIndented(indent+2, "Ifs:");
		for (int index=0; index<ifs.length; index++)
			if (ifs[index]!=null) ifs[index].print(indent+3);
		IOUtils.printIndented(indent+2, "End of Ifs");

		IOUtils.printIndented(indent+2, "Modules:");
		for (int index=0; index<modules.length; index++)
			if (modules[index]!=null) modules[index].print(indent+3);
		IOUtils.printIndented(indent+2, "End of Modules");
		
		IOUtils.printIndented(indent+2, "Functions:");
		for (int index=0; index<functions.length; index++)
			if (functions[index]!=null) functions[index].print(indent+3);
		IOUtils.printIndented(indent+2, "End of Functions");
		
		IOUtils.printIndented(indent, "End listing of scopes.");
		
		
	}
	
	public String getFilename() {
		return cmds.getFilename();
	}
	
	public ArrayList<String> getLines() {
		return cmds.lines;
	}
		
	public StringsMap getStringsMap() {
		return cmds.getStringsMap();
	}
	
	public void setClassesMap(ClassBlocksMap map) {
		this.classesMap = map;
	}
	
	public ClassBlocksMap getClassesMap() {
		return classesMap;
	}
	
	public Cmds getCmds() {
		return cmds;
	}
}
