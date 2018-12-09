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
import java.io.File;
import java.util.ArrayList;

import compiler.blocks.Program;
import compiler.main.Settings;
import compiler.parser.Cmds;
import compiler.parser.Compilation;
import compiler.parser.FlowParser;
import compiler.parser.Preprocessor;
import compiler.parser.ProgramCompilation;
import compiler.parser.ProgramParser;
import compiler.parser.Scope;
import compiler.parser.ScopeParser;
import compiler.util.BugTrap;
import compiler.util.IOUtils;

public class ClassedProgramParser extends ProgramParser {
	public static final boolean Debug = Settings.Debug;
	
	File programFiles[];
	

	ArrayList<File> classFiles = new ArrayList<File>();
	ArrayList<Cmds> classCmds = new ArrayList<Cmds>();
	Cmds mainProgramCmds = null;
	File mainProgramFile = null;

	ArrayList<Compilation> results;

	public ClassedProgramParser(File programFiles[]) throws BugTrap {
		this.programFiles = programFiles;
	}
	
	public void preprocess() throws BugTrap {
		// Preprocess
		
		for (File f : programFiles) {
			if (programFiles.length>1) IOUtils.println("\nPreprocessing file " + f.getName());
			Preprocessor preprocessor = new Preprocessor(f);
			Cmds cmds = preprocessor.getCmds();
			
			if (cmds.isAClassFile()) {
				classCmds.add(cmds);
				classFiles.add(f);
			}
			else {
				if (mainProgramFile==null) {
					// Delay compilation until all classes are done.
					mainProgramCmds = cmds;
					mainProgramFile = f;
				}
				else throw new BugTrap("Object-oriented program should include only 1 program file. Multiple programs found.");
			}
		}		
	}
	
	public void compile() throws BugTrap {
		// In two steps:
		// Step 1: compile all the classes individually.
		// Step 2: Bind all the classes to an empty program. Then compile the program.

		
		results = new ArrayList<Compilation>();
		
		ClassBlocksMap map = new ClassBlocksMap();
		
		for (int index=0; index<classCmds.size(); index++) {
			Cmds cmds = classCmds.get(index);
			File f = classFiles.get(index);


			// Set BugTrap to report rawline numbers if line tracking is on.
			BugTrap.setCmds(Settings.tracklines ? cmds : null);
			
			
			ClassParser classParser = new ClassParser(cmds);
			ClassScope scope = classParser.getScope();
			ClassBlock classBlock = classParser.getScope().getClassBlock();
			if (map.containsKey(classBlock.getName()))
				throw new BugTrap(classBlock, "Class " + classBlock.getName() + " already exists.");
			map.put(classBlock.getName(), classBlock);
			
			ClassCompilation classResult = new ClassCompilation(f, classBlock, scope);
			results.add(classResult);
			
			if (Debug) IOUtils.println("File " + f.getName() + " completely parsed.\n\n");			
		}
		
		// After processing each class, build the inheritance relationship
		for (String name : map.keyset()) {
			ClassBlock b = map.get(name);
			ClassBlock superclassDummy = b.getSuperClass();
			// Replace the dummy placeholder by the real one.
			if (superclassDummy!=null) {
				ClassBlock superclass = map.get(superclassDummy.getName());
				b.setSuperClass(superclass);
				superclass.putSubClass(b);
			}
		}

		if (mainProgramCmds!=null) {
			// Now handle the program.
			

			// Set BugTrap to report rawline numbers if line tracking is on.
			BugTrap.setCmds(Settings.tracklines ? mainProgramCmds : null);
			
			
			// First, find the scope of blocks within the program.
			ScopeParser scopeParser = new ScopeParser(mainProgramCmds);
			
			Scope programScope = scopeParser.getScope();
			FlowParser flowParser = new FlowParser(programScope, map);

			ProgramCompilation programResult = new ProgramCompilation(mainProgramFile, flowParser.getProgram(), programScope);
			results.add(programResult);
			
			if (Debug) IOUtils.println("File " + mainProgramFile.getName() + " completed!\n\n");
		}
		else {
			if (Debug) IOUtils.println("Main program file not found.\n\n");
		}

	}
	
	public void preprocessAndCompile() throws BugTrap {
		preprocess();
		compile();
	}
	
	

	public Program getProgram() throws BugTrap {
		for (Compilation c : results) {
			if (c.isProgram()) return c.getProgram();
		}
		throw new BugTrap("Program not found");
	}


	public ArrayList<Compilation> getCompilations() {
		return results;
	}

	public ArrayList<Cmds> getClassCmds() {
		return classCmds;
	}
	public Cmds getProgramCmds() {
		return mainProgramCmds;
	}
	
	public String getProgramName() {
		return mainProgramCmds.getFilename();
	}

}
