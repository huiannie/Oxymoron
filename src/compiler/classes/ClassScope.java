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

import compiler.blocks.Block;
import compiler.main.Settings;
import compiler.parser.Cmds;
import compiler.parser.Scope;
import compiler.util.BugTrap;
import compiler.util.IOUtils;

public class ClassScope extends Scope {
	public static final boolean Debug = Settings.Debug;

	public ClassBlock classBlock;
	
	public ClassScope(Cmds cmds) {
		super(cmds);
	}
	

	// Create a map of all modules and functions belonging to this class.
	
	public Block findBlockAtLine(int lineNumber) throws BugTrap {
		Block found = super.findBlockAtLine(lineNumber);

		if (found!=null) return found;
		else if (classBlock!=null && classBlock.getStartLineNumber()==lineNumber) return classBlock;
		
		return null;
	}
	
	
	
	public void print(int indent) {
		IOUtils.printIndented(indent, "Start listing of scopes:");
		
		if (classBlock!=null) {
			IOUtils.printIndented(indent+1, "Class:");
			classBlock.print(indent+2);
			IOUtils.printIndented(indent+1, "End of Class");
		}
		
		super.print(indent+1);		
	}
	
	
	public ClassBlock getClassBlock() {
		return classBlock;
	}
	
}
