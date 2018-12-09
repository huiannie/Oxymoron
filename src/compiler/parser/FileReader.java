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

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;

import compiler.main.Settings;
import compiler.util.BugTrap;

public class FileReader {
	private static final boolean Echo = true;
	
	Scanner scanner;
	
	public FileReader(InputStream instream) {
		scanner = new Scanner(instream);
	}
	public String nextLine() throws BugTrap {
		try {
			String line = scanner.nextLine();
			if (Echo) {
				PrintStream outstream = Settings.GetOutstream();
				outstream.println(line);
			}
			return line;			
		} catch (Exception e) {
			if (e instanceof BugTrap) throw e;
			else throw new BugTrap(e);
		}
	}
	public void close() {
		scanner.close();
	}
}
