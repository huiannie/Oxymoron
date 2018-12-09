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

import compiler.blocks.Program;
import compiler.classes.ClassBlocksMap;
import compiler.main.Settings;
import compiler.util.BugTrap;
import compiler.util.IOUtils;

public class FlowParser {
	public static final boolean Debug = Settings.Debug;
	
	Scope scope = null;
	Program program;
	
	public FlowParser(Scope scope, ClassBlocksMap map) throws BugTrap {
		this.scope = scope;
		this.scope.setClassesMap(map);
		parse();
	}
	
	void parse() throws BugTrap {
		program = new Program(0, scope.getLines().size(), scope.getFilename(), scope.getClassesMap());
		program.parse(scope.getLines(), scope, null);

		if (Debug) program.print(2);
		if (Debug) IOUtils.println("****************** Producing program for " + scope.getFilename() + " ***************");
	}

	public Program getProgram() {
		return program;
	}

}
