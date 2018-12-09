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

import compiler.blocks.Program;
import compiler.main.Settings;
import compiler.util.BugTrap;
import compiler.util.IOUtils;

public class StandaloneProgramParser extends ProgramParser {
	public static final boolean Debug = Settings.Debug;
	
	File programFile;
	ProgramCompilation result;
	Cmds cmds;
	
	public StandaloneProgramParser(File programFile) throws BugTrap {
		this.programFile = programFile;
	}

	public void preprocess() throws BugTrap {
		Preprocessor preprocessor = new Preprocessor(programFile);
		cmds = preprocessor.getCmds();
		if (cmds==null || cmds.isEmpty()) throw new BugTrap("No commands found.");
	}
	
	
	public void compile() throws BugTrap {
		
		Program program = null;
		Scope scope = null;

		// Set BugTrap to report rawline numbers if line tracking is on.
		BugTrap.setCmds(Settings.tracklines ? cmds : null);
		
		if (!cmds.isAClassFile()) {
			ScopeParser scopeParser = new ScopeParser(cmds);
			scope = scopeParser.getScope();

			FlowParser flowParser = new FlowParser(scope, null);
			program = flowParser.getProgram();
			
			if (Debug) IOUtils.println("File " + programFile.getName() + " completed!\n\n");
		}
		else {
			if (Debug) IOUtils.println("File " + programFile.getName() + " is not a program file.\n\n");
			throw new BugTrap("File " + programFile.getName() + " is not a program file.\n\n");
		}
		
		result = new ProgramCompilation(programFile, program, scope);
		
	}
	
	public void preprocessAndCompile() throws BugTrap {
		preprocess();
		compile();
	}

	public ProgramCompilation getCompilation() {
		return result;
	}

	public Cmds getProgramCmds() {
		return cmds;
	}
	
	public Program getProgram() {
		return result.getProgram();
	}
	public String getProgramName() {
		return cmds.getFilename();
	}


	@Override
	public ArrayList<Cmds> getClassCmds() {
		// Return an empty list
		return new ArrayList<Cmds>();
	}

}
