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
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import compiler.main.Settings;
import compiler.util.IOUtils;
import examples.PathUtils;
import examples.ResSettings;

public class BookExampleTests extends Tests {
	
	// For testing purpose, turn on the flag and provide a suitable directory where files are tested.
	private static final boolean TESTING = true;

	
	
	private int getChapter10grouping(String programName) {
		String extension = ProgramExtension;
		// Trim off extension from the program name.
		if (programName.endsWith(extension))
			programName = programName.substring(0, programName.indexOf(extension));
		
		int group = 0;
		if (programName.equals("program10-1") || programName.equals("program10-2")) group = 1;
		else if (programName.equals("program10-3") || programName.equals("program10-4")) group = 3;
		else if (programName.equals("program10-5") || programName.equals("program10-6")) group = 5;
		else if (programName.equals("program10-7") || programName.equals("program10-8")) group = 7;
		else if (programName.equals("program10-9") || programName.equals("program10-10")) group = 9;
		else if (programName.equals("program10-11")) group = 11;
		else if (programName.equals("program10-12")) group = 12;
		else if (programName.equals("program10-13")) group = 13;
		else if (programName.equals("program10-14") || programName.equals("program10bad-14")) group = 14;
		
		return group;
	}
	private void setupChapter10FileDependency(String programName) {
		int chapterNumber = 10;
		String extension = ProgramExtension;
		// Trim off extension from the program name.
		if (programName.endsWith(extension))
			programName = programName.substring(0, programName.indexOf(extension));
		
		
		if (programName.equals("program10-9")) {
			// Special case: This program appends to data file. So remove any existing data file to prevent data accumulation.
			File IOFileDirectory = new File(getIOFileDirectory(chapterNumber, programName));
			File datafile = new File(IOFileDirectory, "coffee.dat");
			System.out.println("Removing old data file " + datafile.getName() + " before testing " + programName);
			if (datafile.exists()) datafile.delete();
		}

		else if (programName.equals("program10-11") || programName.equals("program10-12") || programName.equals("program10-13")) {
			// Special case: This program assumes a data file already exists with some specific data.
			File IOFileDirectory = new File(getIOFileDirectory(chapterNumber, programName));
			File datafile = new File(IOFileDirectory, "coffee.dat");
			if (datafile.exists()) datafile.delete();
			File defaultDatafile = new File(IOFileDirectory, "coffee-original.dat");
			if (defaultDatafile.exists()) {
				try {
					Files.copy(defaultDatafile.toPath(), datafile.toPath());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	private void setupChapter11FileDependency(String programName) {
		int chapterNumber = 11;
		String extension = ProgramExtension;
		// Trim off extension from the program name.
		if (programName.endsWith(extension))
			programName = programName.substring(0, programName.indexOf(extension));
		
		
		if (programName.equals("program11-6")) {
			// Special case: This program assumes a data file already exists with some specific data.
			File IOFileDirectory = new File(getIOFileDirectory(chapterNumber, programName));
			File datafile = new File(IOFileDirectory, "coffee.dat");
			if (datafile.exists()) datafile.delete();
			File defaultDatafile = new File(IOFileDirectory, "coffee-original.dat");
			if (defaultDatafile.exists()) {
				try {
					Files.copy(defaultDatafile.toPath(), datafile.toPath());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	

	// In chapter 14, the classes are associated to the main programs in a specific way.
	// The association cannot be deduced just from the program names and class names.
	// Therefore, they are explicitly grouped here.
	private String[][] getChapter14TestSets() {
		String extension = ProgramExtension;
		String sets[][] = {
				{"program14-1", "classlisting14-3"},
				{"program14-2", "classlisting14-4"},
				{"program14-3", "classlisting14-4"},
				{"program14-p672", "classlisting14-5", "classlisting14-6", "classlisting14-7"},
				{"program14-p673", "classlisting14-5", "classlisting14-6", "classlisting14-7"},
				{"program14-4", "classlisting14-8"},
				{"program14-5", "classlisting14-8", "classlisting14-9"},
                {"program14-6", "classlisting14-10", "classlisting14-11", "classlisting14-12"},
                {"program14a-6", "classlisting14-10", "classlisting14-11", "classlisting14-12"},
                {"program14-p689", "classlisting14-p689a", "classlisting14-p689b"},
		};
		for (int i=0; i<sets.length; i++) {
			for (int j=0; j<sets[i].length; j++) {
				if (!sets[i][j].endsWith(extension))
					sets[i][j] = sets[i][j] + extension;
			}
		}
		return sets;
	}
	private String getProgramInObjectOrientedTestSet(String[] set) {
		for (String filename : set) {
			if (filename.startsWith("program")) return filename;
		}
		return null;
	}

	
	// The example programs in the book are ordered by number in the form:
	// programX-Y.txt where X is the chapter number and Y is the program number within the chapter.
	// In some chapters, the programs are sequentially dependent. Therefore, the programs should be
	// sorted by the Y value before they are tested in batch.
	private File[] sortProgramsByNumber(File[] programFiles) {
		String extension = ProgramExtension;
		String pattern = "-([0-9]+)" + extension;
		
		List<File> list = new ArrayList<File>(Arrays.asList(programFiles));
		Collections.sort(list, new Comparator<File>() {
			@Override
			public int compare(File o1, File o2) {
				String name1 = o1.getName();
				String name2 = o2.getName();
				int programNumber1;
				int programNumber2;
				Matcher matcher1 = Pattern.compile(pattern).matcher(name1);
				Matcher matcher2 = Pattern.compile(pattern).matcher(name2);
				if (matcher1.find() && matcher2.find()) {
					programNumber1 = Integer.valueOf(matcher1.group(1));
					programNumber2 = Integer.valueOf(matcher2.group(1));
					if (programNumber1<programNumber2) return -1;
					else if (programNumber1==programNumber2) return 0;
					else return 1;
				}
				else {
					return name1.compareTo(name2);
				}
			}
			
		});
		return list.toArray(new File[0]);
	}

	private String getProgramSourceDirectory(int chapterNumber) {
		// This is the location of the program. It is found under source because the programs 
		// would also be visible as examples in the main program.
		String programRoot = "src/";

		// Chapters 2 to 13 are simple standalone programs.
		if (chapterNumber>=2 && chapterNumber<=13) {
			return PathUtils.joinAll(programRoot, ResSettings.StandAloneProgramDirectory, ResSettings.ChapterLabel + chapterNumber);
		}
		// Chapter 14 is object oriented programs.
		else if (chapterNumber==14) {
			return PathUtils.joinAll(programRoot, ResSettings.ObjectOrientedProgramDirectory, ResSettings.ChapterLabel + chapterNumber);
		}
		else {
			IOUtils.println("Chapters must be numbered 2 to 14.");
			return null;
		}
	}
	private String getChapterDataDirectory(int chapterNumber) {
		return "testbed-ch" + chapterNumber;
	}
	private String getStdinDataDirectory(int chapterNumber, String programName) {
		String extension = ProgramExtension;
		// Trim off extension from the program name.
		if (programName.endsWith(extension))
			programName = programName.substring(0, programName.indexOf(extension));
		return PathUtils.joinAll(testing_directory, getChapterDataDirectory(chapterNumber), programName + "-data");
	}
	private String getStdoutDataDirectory(int chapterNumber, String programName) {
		String extension = ProgramExtension;
		// Trim off extension from the program name.
		if (programName.endsWith(extension))
			programName = programName.substring(0, programName.indexOf(extension));
		return PathUtils.joinAll(testing_directory, getChapterDataDirectory(chapterNumber), programName + "-out");
	}
	private String getIOFileDirectory(int chapterNumber, String programName) {
		String extension = ProgramExtension;
		// Trim off extension from the program name.
		if (programName.endsWith(extension))
			programName = programName.substring(0, programName.indexOf(extension));
		
		if (chapterNumber==10) {
			// Chapter 10 has grouping because the the data files are shared in a specific way
			int group = getChapter10grouping(programName);
			return PathUtils.joinAll(testing_directory, getChapterDataDirectory(chapterNumber), "datafiles"+group);
		}
		else {
			// Default: No grouping
			return PathUtils.joinAll(testing_directory, getChapterDataDirectory(chapterNumber), "datafiles");
		}
	}
	
	
	
	public void runChapterExampleTests(int chapterNumber) {
		
		// Set up the directory where the programs are located.
		File programsDir = new File(getProgramSourceDirectory(chapterNumber));
		
		if (chapterNumber>=2 && chapterNumber<=9 || chapterNumber>=12 && chapterNumber<=13) {
			// Find all program files in the chapter.
			File[] programFiles = getVisibleFiles(programsDir);
			
			programFiles = sortProgramsByNumber(programFiles);
			
			for (File p : programFiles) {
				// For each program file, locate its data directory and output directory.
				// The location of the data for stdin and stdout are completely separated from the program location.
				runSingleProgramTest(p.getAbsolutePath(), getStdinDataDirectory(chapterNumber, p.getName()), getStdoutDataDirectory(chapterNumber, p.getName()));
			}
		}
		else if (chapterNumber==10) {
			// Find all program files in the chapter.
			File[] programFiles = getVisibleFiles(programsDir);
			programFiles = sortProgramsByNumber(programFiles);
			// For each program file, locate its data directory and output directory.
			for (File p : programFiles) {
				// Chapter 10 requires reading and writing of data files. Set data file directory before testing.
				Settings.SetDataFileDirectory(getIOFileDirectory(chapterNumber, p.getName()));
				
				setupChapter10FileDependency(p.getName());
				// The location of the data for stdin and stdout are completely separated from the program location.
				runSingleProgramTest(p.getAbsolutePath(), getStdinDataDirectory(chapterNumber, p.getName()), getStdoutDataDirectory(chapterNumber, p.getName()));
				
				// For Chapter 10, reset data file directory when done.
				Settings.ResetDataFileDirectory();
			}
		}
		else if (chapterNumber==11) {
			// Find all program files in the chapter.
			File[] programFiles = getVisibleFiles(programsDir);
			programFiles = sortProgramsByNumber(programFiles);
			// For each program file, locate its data directory and output directory.
			for (File p : programFiles) {
				// Chapter 11 requires reading and writing of data files. Set data file directory before testing.
				Settings.SetDataFileDirectory(getIOFileDirectory(chapterNumber, p.getName()));
				setupChapter11FileDependency(p.getName());
				// The location of the data for stdin and stdout are completely separated from the program location.
				runSingleProgramTest(p.getAbsolutePath(), getStdinDataDirectory(chapterNumber, p.getName()), getStdoutDataDirectory(chapterNumber, p.getName()));
				// For Chapter 11, reset data file directory when done.
				Settings.ResetDataFileDirectory();
			}
			
		}
		else if (chapterNumber==14) {
			String[][] programSets = getChapter14TestSets();
			for (String[] set : programSets) {
				String programName = getProgramInObjectOrientedTestSet(set);
				
				// Run the Object-oriented program
				runObjectOrientedProgramTest(programsDir.getAbsolutePath(), set, getStdinDataDirectory(chapterNumber, programName), getStdoutDataDirectory(chapterNumber, programName));
				
			}
		}


	}
	public void runChapterExampleTests() {
		// The last example in chapter 13 intentionally causes a stack overflow as an illustration.
		// After running that example, the application will not be able to recover.
		// Therefore, to test all examples, run chapter 14 first, then other chapters.
		// Keep the stack overflow example to the very end.
		runChapterExampleTests(14);

		for (int chapterNumber=2; chapterNumber<=13; chapterNumber++) {
			runChapterExampleTests(chapterNumber);
		}
	}
	
	
	
	public static void main(String args[]) {
		if (TESTING) {
			new BookExampleTests().runChapterExampleTests();
		}
	}
}
