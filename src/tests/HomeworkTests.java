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

import compiler.main.Settings;
import compiler.util.IOUtils;

public class HomeworkTests extends Tests {
	
	
	// For testing purpose, turn on the flag and provide a suitable directory where files are tested.
	private static final boolean TESTING = true;

	


	private void testHW(String rootdir, String srcs[]) {
		// Source is the reference directory.
		// The data directory is named after the source directory with the -data extension.
		// The output directory is named after the source directory with the -out extension.
		for (int index=0; index<srcs.length; index++) {
			String progdir = rootdir + srcs[index];
			String datadir = rootdir + srcs[index] + "-data";
			String outputdir = rootdir + srcs[index] + "-out";
			runBatchTest(progdir, datadir, outputdir);
		}
	}
	
	public void runHWTests(int HWnumber) {
		// hw1 includes chapter 2
		String rootdir_HW1 = testing_directory + "testbed-hw1/";
		String srcs_HW1[] = {"ch2-echo"};
		
		// hw2 includes chapters 3 and 4
		String rootdir_HW2 = testing_directory + "testbed-hw2/";
		String srcs_HW2[] = {"ch3-method", "ch4-if", "ch4-select"};
		
		// hw3 includes chapters 5 and 6
		String rootdir_HW3 = testing_directory + "testbed-hw3/";
		String srcs_HW3[] = {"ch5-dowhile", "ch5-for", "ch5-while", "ch6-fint", "ch6-fstring"};

		// hw4 includes chapters 8 and 10
		String rootdir_HW4 = testing_directory + "testbed-hw4/";
		String dataDir_HW4 = rootdir_HW4 + "data/";
		String srcs_HW4[] = {"ch8-average", "ch8-total", "ch10-readAndCompare", "ch10-readAndShow", "ch10-write"};

		if (HWnumber==1) {
			// Test hw1
			testHW(rootdir_HW1, srcs_HW1);
		}
		else if (HWnumber==2) {
			// Test hw2
			testHW(rootdir_HW2, srcs_HW2);
		}
		else if (HWnumber==3) {
			// Test hw3
			testHW(rootdir_HW3, srcs_HW3);
		}
		else if (HWnumber==4) {
			// Test hw4  (Chapter 10 requires reading and writing of data files. Set data file directory before testing.)
			Settings.SetDataFileDirectory(dataDir_HW4);
			testHW(rootdir_HW4, srcs_HW4);
			IOUtils.println("\nAny output files written by the sample test programs have been saved to directory: " + dataDir_HW4);
			// Reset data file directory when done.
			Settings.ResetDataFileDirectory();
		}
		else {
			IOUtils.println("HW" + HWnumber + " is not available");
		}
	}
	
	public void runHWTests() {
		runHWTests(1);
		runHWTests(2);
		runHWTests(3);
		runHWTests(4);
	}
	
	public static void main(String args[]) {
		if (TESTING) {
			new HomeworkTests().runHWTests();
		}
	}
}
