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
package compiler.lib;

import compiler.lib.files.Eof;
import compiler.util.BugTrap;
import compiler.util.IOUtils;

public class LibFiles extends Library {
	
	public static final String names[] = {Eof.name};
	
	public static LibFiles library;
	
	static {
		library = new LibFiles();
		try {
			library.create();
		} catch (BugTrap e) {
			IOUtils.println(LibFiles.class.getName() + "Failed to create library.");
		}
	}

	public void create() throws BugTrap {
		create(new Eof());
	}
}
