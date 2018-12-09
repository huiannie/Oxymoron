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
package compiler.commands;

import compiler.blocks.Block;
import compiler.files.FileCommand;
import compiler.main.Settings;
import compiler.tokenizers.LineMatcher;

public abstract class Command extends Block {
	public static final boolean Debug = Settings.Debug;
	
	private String name;

	protected Command(int lineNumber, String name) {
		super(lineNumber, lineNumber+1);
		this.name = name;
		hasHeaderAndFooter = false; // All single-line commands are not wrapped by keywords.
	}
	
	public String getName() { return name; }
	
	public static boolean startsWithAKeyword(String line) {
		if (LineMatcher.matchStart(line, Call.StartKeyword)) return true;
		else if (LineMatcher.matchStart(line, Display.StartKeyword)) return true;
		else if (LineMatcher.matchStart(line, Input.StartKeyword)) return true;
		else if (LineMatcher.matchStart(line, Return.StartKeyword)) return true;
		else if (LineMatcher.matchStart(line, Set.StartKeyword)) return true;
		else if (FileCommand.startsWithAKeyword(line)) return true;
		return false;
	}

	
	public static boolean isACommand(String line) {
		if (Call.isACall(line)) return true;
		else if (Display.isADisplay(line)) return true;
		else if (Input.isAnInput(line)) return true;
		else if (Return.isAReturn(line)) return true;
		else if (Set.isASet(line)) return true;
		else if (FileCommand.isAFileCommand(line)) return true;
		return false;
	}

	public static Command create(String line, int lineNumber) {
		if (Call.isACall(line)) return new Call(lineNumber);
		else if (Display.isADisplay(line)) return new Display(lineNumber);
		else if (Input.isAnInput(line)) return new Input(lineNumber);
		else if (Return.isAReturn(line)) return new Return(lineNumber);
		else if (Set.isASet(line)) return new Set(lineNumber);
		else if (FileCommand.isAFileCommand(line)) return FileCommand.create(line, lineNumber);
		return null;
	}
	
	
}
