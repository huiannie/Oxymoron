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
package compiler.files;

import compiler.binder.Context;
import compiler.commands.Command;
import compiler.literal.ArrayCall;
import compiler.literal.Literal;
import compiler.literal.SymbolCall;
import compiler.main.Settings;
import compiler.tokenizers.LineMatcher;
import compiler.util.BugTrap;

public abstract class FileCommand extends Command {
	public static final boolean Debug = Settings.Debug;
	
	
	protected FileCommand(int lineNumber, String name) {
		super(lineNumber, name);
	}


	public static boolean startsWithAKeyword(String line) {
		if (LineMatcher.matchStart(line, Close.StartKeyword)) return true;
		else if (LineMatcher.matchStart(line, Delete.StartKeyword)) return true;
		else if (LineMatcher.matchStart(line, Open.StartKeyword)) return true;
		else if (LineMatcher.matchStart(line, Read.StartKeyword)) return true;
		else if (LineMatcher.matchStart(line, Rename.StartKeyword)) return true;
		else if (LineMatcher.matchStart(line, Write.StartKeyword)) return true;
		else if (LineMatcher.matchStart(line, Print.StartKeyword)) return true;
		return false;
	}
	

	public static boolean isAFileCommand(String line) {
		if (Close.isAClose(line)) return true;
		else if (Delete.isADelete(line)) return true;
		else if (Open.isAnOpen(line)) return true;
		else if (Read.isARead(line)) return true;
		else if (Rename.isARename(line)) return true;
		else if (Write.isAWrite(line)) return true;
		else if (Print.isAPrint(line)) return true;
		return false;
	}

	public static FileCommand create(String line, int lineNumber) {
		if (Close.isAClose(line)) return new Close(lineNumber);
		else if (Delete.isADelete(line)) return new Delete(lineNumber);
		else if (Open.isAnOpen(line)) return new Open(lineNumber);
		else if (Read.isARead(line)) return new Read(lineNumber);
		else if (Rename.isARename(line)) return new Rename(lineNumber);
		else if (Write.isAWrite(line)) return new Write(lineNumber);
		else if (Print.isAPrint(line)) return new Print(lineNumber);
		return null;
	}
	
	protected FileDataType getValue(Context context, Literal variableName) throws BugTrap {
		if (variableName instanceof SymbolCall) {
			SymbolCall variableCall = (SymbolCall) variableName;
			FileDataType fileValue = (FileDataType) variableCall.evaluate(context);
			return fileValue;
		}
		else if (variableName instanceof ArrayCall) {
			ArrayCall arrayCall = (ArrayCall) variableName;
			FileDataType fileValue = (FileDataType) arrayCall.evaluate(context);
			return fileValue;
		}
		throw new BugTrap(this, "Variable does not contain a file data type");
	}
}
