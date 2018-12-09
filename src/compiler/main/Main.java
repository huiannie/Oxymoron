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
package compiler.main;

import java.io.File;

import compiler.parser.CommandlineEngine;
import compiler.util.BugTrap;
import compiler.util.IOUtils;
import gui.GuiMain;
import tests.BookExampleTests;
import tests.Tests;

public class Main {
	public static final boolean Debug = Settings.Debug;
	
	private static final int Code_Bad = -1;
	private static final int Code_gui = 0;
	private static final int Code_batchTest = 1;
	private static final int Code_commandLineRun = 2;

	
	
	
	private static boolean hasClasses(File filelist[]) {
		// Class files must be named after the form "classlistingX-X.txt", where X are integers.
		for (File f: filelist) {
			if (f.getName().contains("class")) return true;
		}
		return false;
	}
	
	
	private static int checkCommandLineArguments(String args[]) {
		// If no argument is provided, then set to GUI mode
		if (args.length==0) return Code_gui;
		
		// If three directories are provided as arguments, then run in batch mode.
		if (args.length==3) {
			File arg1 = new File(args[0]);
			File arg2 = new File(args[1]);
			File arg3 = new File(args[2]);
			
			if (arg1.exists() && arg2.exists() && arg3.exists()) {
				if (arg1.isDirectory() && arg2.isDirectory() && arg3.isDirectory()) return Code_batchTest;
				if (arg1.isFile() && arg2.isFile() && arg3.isFile()) return Code_commandLineRun;
				return Code_Bad;
			}
			else return Code_Bad;
		}
		
		// If arguments are provided and they are all files, then run in command-line mode.
		if (args.length>0) {
			for (int index=0; index<args.length; index++) {
				File arg = new File(args[index]);
				if (!arg.exists() || !arg.isFile()) return Code_Bad;
			}
			return Code_commandLineRun;
		}
		
		// Default: 
		return Code_Bad;
	}
	
	
	private static void usage(String name, int indent) {
		IOUtils.printIndented(indent, "Usage:");
		IOUtils.printIndented(indent+1, "For GUI run: " + name);
		IOUtils.printIndented(indent+1, "For batch test run: " + name + " testProgramsDirectory" + " dataFilesDirectory");
		IOUtils.printIndented(indent+1, "For commandline run :" + name + " testProgram.txt" + " [testProgram.txt]");
	}
	
	
	

	private static void runCommandlineInteractive(String args[]) {
		File[] programFiles = null;
		programFiles = new File[args.length];
		for (int index=0; index<args.length; index++) {
			programFiles[index] = new File(args[index]);
			if (Debug) IOUtils.println("Accepting file " + programFiles[index].getPath());
		}

		try {
			CommandlineEngine engine = new CommandlineEngine();
			
			// In command-line mode, accept two forms:
			// Form 1. All files are program files. In that case, run each as a single program.
			// Form 2. One program file and the rest are class files. In this case, attach the classes to the program file and run the program file only.
			if (hasClasses(programFiles)) {
				engine.runClassedProgram(programFiles);
			}
			else {
				engine.batchRunSinglePrograms(programFiles);
			}
		} catch (BugTrap e) {
			IOUtils.println("Program aborted anormally.");
			IOUtils.println(e.getDetails());
		}

	}
	



	public static void main(String args[]) {
		// The application may be run in 3 modes:
		// 1. Command-line mode
		// 2. Command-line batch mode (primarily for automated grading)
		// 3. GUI mode
		int code = checkCommandLineArguments(args);
		if (code==Code_gui) {
			GuiMain.main(args);
		}
		else if (code==Code_commandLineRun) {
			runCommandlineInteractive(args);
		}
		else if (code==Code_batchTest) {
			new Tests().runBatchTest(args[0], args[1], args[2]);
		}
		else {
			Main.usage(Settings.Title, 0);
		}
	}
}
