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

import java.io.PrintStream;

public class Settings {
	public static final boolean Debug = false;
	
	public static final java.io.InputStream instream = System.in;
	private static PrintStream outstream = System.out;
	private static PrintStream errstream = System.err;
	
	private static final String DefaultTestDataFileDirectory = "output/";
	private static String DataFileDirectory = DefaultTestDataFileDirectory;
	
	public static final String Title = "Oxymoron";
	public static final String TitleLong = "Oxymoron Pseudocode Compiler";

	public static final boolean tracklines = true;
	
	public static String getDataFileDirectory() {
		return DataFileDirectory;
	}
	public static void SetDataFileDirectory(String dataFileDirectory) {
		// Make sure that any custom directory must end with "/"
		if (!dataFileDirectory.endsWith("/")) dataFileDirectory = dataFileDirectory + "/";
		DataFileDirectory = dataFileDirectory;
	}
	public static void ResetDataFileDirectory() {
		DataFileDirectory = DefaultTestDataFileDirectory;
	}
	
	public static void SetOutstream(PrintStream outstream) {
		Settings.outstream = outstream;
	}
	public static PrintStream GetOutstream() {
		return outstream;
	}
	public static void SetErrstream(PrintStream errstream) {
		Settings.errstream = errstream;
	}
	public static PrintStream GetErrstream() {
		return errstream;
	}

}
