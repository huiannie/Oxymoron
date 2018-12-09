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
package tests;

import java.io.File;
import java.io.FilenameFilter;

import compiler.parser.CommandlineEngine;
import compiler.util.BugTrap;
import compiler.util.IOUtils;
import examples.ResSettings;

public class Tests {
	private static final String DefaultTestcasesDirectory = "testcases/";
	protected String testing_directory = DefaultTestcasesDirectory;
	
	protected static final String ProgramExtension = ResSettings.ResourceType;
	
	
	public Tests() {
	}
	
	public Tests(String testing_directory) {
		this.testing_directory = testing_directory;
	}

	protected File[] getVisibleFiles(File directory) {
		return directory.listFiles(new FilenameFilter(){
			// Avoid hidden files.
			public boolean accept(File dir, String name) {
				return !name.startsWith(".");
			}});
	}
	
	protected void runSingleProgramTest(String programPath, String datafilesDirString, String outputDirString) {
		File programFile = new File(programPath);
		File datafilesDir = new File(datafilesDirString);
		File outputDir = new File(outputDirString);
		File[] dataFiles = getVisibleFiles(datafilesDir);
		
		IOUtils.println("\nSource program: " + programFile.getPath());
		IOUtils.println("Stdin data directory: " + datafilesDir.getPath());
		IOUtils.println("Stdout data directory: " + outputDir.getPath());
		
		for (File d : dataFiles) {
			
			IOUtils.println("Testing program " + programFile.getName() + " with input " + d.getName());
			try {
				CommandlineEngine engine = new CommandlineEngine();
				engine.testSingleProgram(programFile, d, outputDir);
			} catch (BugTrap e) {
				IOUtils.println("Program " + programFile.getName() + " aborted anormally on input " + d.getName());					
			}
		}
	}
	
	
	private String getProgramInObjectOrientedTestSet(String[] set) {
		for (String filename : set) {
			if (filename.startsWith("program")) return filename;
		}
		return null;
	}


	protected void runObjectOrientedProgramTest(String programDirPath, String[] set, String datafilesDirString, String outputDirString) {
		File programDir = new File(programDirPath);
		File datafilesDir = new File(datafilesDirString);
		File outputDir = new File(outputDirString);
		File[] dataFiles = getVisibleFiles(datafilesDir);
		
		IOUtils.println("\nSource program directory: " + programDir.getPath());
		IOUtils.println("Stdin data directory: " + datafilesDir.getPath());
		IOUtils.println("Stdout data directory: " + outputDir.getPath());
		
		File[] programFiles = new File[set.length];
		File mainProgramFile = new File(programDir, getProgramInObjectOrientedTestSet(set));
		for (int index=0; index<set.length; index++) {
			programFiles[index] = new File(programDir, set[index]);
		}

		for (File d : dataFiles) {
			IOUtils.println("Testing program " + mainProgramFile.getName() + " with input " + d.getName());
			try {
				CommandlineEngine engine = new CommandlineEngine();
				engine.testObjectOrientedProgram(programFiles, mainProgramFile, d, outputDir);
			} catch (BugTrap e) {
				IOUtils.println("Program " + mainProgramFile.getName() + " aborted anormally on input " + d.getName());					
			}
			
		}
	}

	
	public void runBatchTest(String programDirString, String datafilesDirString, String outputDirString) {
		File programsDir = new File(programDirString);
		File datafilesDir = new File(datafilesDirString);
		File outputDir = new File(outputDirString);
		File[] programFiles = getVisibleFiles(programsDir);
		File[] dataFiles = getVisibleFiles(datafilesDir);
		
		IOUtils.println("\nSource program directory: " + programsDir.getPath());
		IOUtils.println("Stdin data directory: " + datafilesDir.getPath());
		IOUtils.println("Stdout data directory: " + outputDir.getPath());
		
		for (File p : programFiles) {
			for (File d : dataFiles) {
				
				IOUtils.println("Testing program " + p.getName() + " with input " + d.getName());
				try {
					CommandlineEngine engine = new CommandlineEngine();
					engine.testSingleProgram(p, d, outputDir);
				} catch (BugTrap e) {
					IOUtils.println("Program " + p.getName() + " aborted anormally on input " + d.getName());					
				}
			}
		}
	}
	
}
