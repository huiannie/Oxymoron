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
package compiler.blocks;
import java.util.ArrayList;

import compiler.binder.Context;
import compiler.main.Settings;
import compiler.parser.Scope;
import compiler.tokenizers.LineMatcher;
import compiler.tokenizers.LineTokenizer;
import compiler.util.BugTrap;
import compiler.util.IOUtils;

public class Module extends Method {
	public static final boolean Debug = Settings.Debug;
	
	public static final String StartKeyword = "Module";
	public static final String EndKeyword = "End Module";

	
	public static final String Main = "main";

	
	public Module(int startLineNumber, int endLineNumber) {
		super(startLineNumber, endLineNumber);
		hasHeaderAndFooter = true;
	}

	public static boolean startsWithAKeyword(String line) {
		if (LineMatcher.matchStart(line, EndKeyword)) return true;
		else if (LineMatcher.matchStart(line, StartKeyword)) return true;
		return false;
	}

	public static boolean isAStart(String line) {

		if (LineMatcher.matchStart(line, StartKeyword)) return true;
		else if (LineMatcher.matchStart(line, AccessToken, StartKeyword)) return true;
		else return false;
	}

	public static boolean isAnEnd(String line) {
		return (LineMatcher.matchExact(line, EndKeyword));
	}

	
	public void print(int indent) {
		super.print(indent, null);
	}
		
	public void parseHeader(String line) throws BugTrap {
		// Syntax: [access] Method name (argumentsList)
		
		LineTokenizer tokenizer = new LineTokenizer(line);
		
		if (tokenizer.isAccessModifier())
			access = tokenizer.parseAccessModifier();

		if (!tokenizer.isKeyword(StartKeyword)) throw new BugTrap(this, StartKeyword + ": Keyword missing");
		tokenizer.parseKeyword(StartKeyword);

		if (!tokenizer.isName()) throw new BugTrap(this, "name missing");
		name = tokenizer.parseName();

		if (!tokenizer.isParameters()) throw new BugTrap(this, "missing or bad parameter list");
		parametersList = tokenizer.parseParameters();

		if (Debug) IOUtils.println(getClass().getSimpleName() + " " + name + " parsed header: " + line);
	}
	
	@Override
	public void parse(ArrayList<String> code, Scope scope, Block parent) throws BugTrap {
		String line = code.get(getStartLineNumber());
		parseHeader(line);
		super.parse(code, scope, parent);
	}
	
	
	
	
	@Override
	public int execute(Context context) throws BugTrap {
		// Binding must be called within MethodCall.evaluate() before calling the evaluate() in this class.
		super.execute(context);
		return ExitCode_OK;
	}

}
