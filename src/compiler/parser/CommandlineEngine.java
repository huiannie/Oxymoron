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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintStream;

import compiler.blocks.Program;
import compiler.classes.ClassedProgramParser;
import compiler.main.Settings;
import compiler.util.BugTrap;
import compiler.util.IOUtils;

public class CommandlineEngine {
	public static final boolean Debug = Settings.Debug;

	
	// For automated testing. Both stdin and stdout are redirected to directories.
	public void testSingleProgram(File programFile, File dataFile, File outputDir) throws BugTrap {
		FileInputStream instream = null;
		PrintStream outstream = null;
		
		try {
			// Prepare input stream
			instream = new FileInputStream(dataFile);
			
			if (outputDir==null) throw new BugTrap("Output directory missing.");
			
			// Prepare output stream
			String outputFilename =  programFile.getName() + "-" + dataFile.getName() + ".log";
			File outputFile = new File(outputDir, outputFilename);
			
			
			
			if (outputFile.exists()) outputFile.delete();
			outstream = new PrintStream(outputFile);
			
			StandaloneProgramParser programParser = new StandaloneProgramParser(programFile);
			programParser.preprocessAndCompile();
						
			// Set BugTrap to report rawline numbers if line tracking is on.
			BugTrap.setCmds(Settings.tracklines ? programParser.getProgramCmds() : null);

			Settings.SetOutstream(outstream);
			// May decide not to save the error messages.
			// Settings.SetErrstream(outstream);
			
			Program program = programParser.getProgram();
			program.run(instream);
			
		} catch (FileNotFoundException e) {
			throw new BugTrap("File " + dataFile.getName() + " not found.");
		}
		finally {
			try { if (instream!=null) instream.close(); } catch(Exception e) {}
			try { if (outstream!=null) outstream.close(); } catch(Exception e) {}
		}
	}
	
	
	// For automated testing. Both stdin and stdout are redirected to directories.
	public void testObjectOrientedProgram(File set[], File mainProgramFile, File dataFile, File outputDir) throws BugTrap {
		FileInputStream instream = null;
		PrintStream outstream = null;
		
		try {
			// Prepare input stream
			instream = new FileInputStream(dataFile);
			
			if (outputDir==null) throw new BugTrap("Output directory missing.");
			
			// Prepare output stream
			String outputFilename =  mainProgramFile.getName() + "-" + dataFile.getName() + ".log";
			File outputFile = new File(outputDir, outputFilename);
			
			
			if (outputFile.exists()) outputFile.delete();
			outstream = new PrintStream(outputFile);
			
			ClassedProgramParser classedProgramParser = new ClassedProgramParser(set);
			classedProgramParser.preprocessAndCompile();

			
			// Set BugTrap to report rawline numbers if line tracking is on.
			BugTrap.setCmds(Settings.tracklines ? classedProgramParser.getProgramCmds() : null);
			

			Settings.SetOutstream(outstream);
			// May decide not to save the error messages.
			// Settings.SetErrstream(outstream);

			
			Program program = classedProgramParser.getProgram();
			if (program!=null) program.run(instream);
			
		} catch (FileNotFoundException e) {
			throw new BugTrap("File " + dataFile.getName() + " not found.");
		}
		finally {
			try { if (instream!=null) instream.close(); } catch(Exception e) {}
			try { if (outstream!=null) outstream.close(); } catch(Exception e) {}
		}
	}

	
	
	
	
	
	// For interactive run. stdin and stdout are from console.
	public void batchRunSinglePrograms(File programFiles[]) throws BugTrap {
		
		for (File f : programFiles) {
			if (programFiles.length>1) IOUtils.println("\nCompiling file " + f.getName());

			StandaloneProgramParser programParser = new StandaloneProgramParser(f);
			programParser.preprocessAndCompile();
						
			// Set BugTrap to report rawline numbers if line tracking is on.
			BugTrap.setCmds(Settings.tracklines ? programParser.getProgramCmds() : null);

			Program program = programParser.getProgram();
			// Default input stream is defined in Settings.
			program.run(Settings.instream);

			if (programFiles.length>1) IOUtils.println();
		}
	}

	
	// For interactive run. stdin and stdout are from console.
	public void runClassedProgram(File programFiles[]) throws BugTrap {
		ClassedProgramParser classedProgramParser = new ClassedProgramParser(programFiles);
		classedProgramParser.preprocessAndCompile();

		
		// Set BugTrap to report rawline numbers if line tracking is on.
		BugTrap.setCmds(Settings.tracklines ? classedProgramParser.getProgramCmds() : null);
		
		Program program = classedProgramParser.getProgram();
		if (program!=null) program.run(Settings.instream);
	}

}
