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
import compiler.commands.Return;
import compiler.data.DataType;
import compiler.main.Settings;
import compiler.parser.Scope;
import compiler.tokenizers.LineMatcher;
import compiler.tokenizers.LineTokenizer;
import compiler.tokenizers.Token;
import compiler.util.BugTrap;
import compiler.util.IOUtils;

public class Function extends Method {
	public static final boolean Debug = Settings.Debug;
	
	public static final String StartKeyword = "Function";
	public static final String EndKeyword = "End Function";
	
	private static final String returnValueToken = Token.ValidNameTagPattern;

	
	private DataType returnType = null;
	
	public Function(int startLineNumber, int endLineNumber) {
		super(startLineNumber, endLineNumber);
		hasHeaderAndFooter = true;
	}
	
	
	public void print(int indent) {
		super.print(indent, "Return type: " + (returnType==null?"unknown":returnType.getType()));
	}

	
	public static boolean startsWithAKeyword(String line) {
		if (LineMatcher.matchStart(line, EndKeyword)) return true;
		else if (LineMatcher.matchStart(line, StartKeyword)) return true;
		return false;
	}

	public static boolean isAStart(String line) {
		if (LineMatcher.matchStart(line, StartKeyword, returnValueToken)) {  // "End" is not considered a valid returnValueToken.
			return true;
		}
		else if (LineMatcher.matchStart(line, AccessToken, StartKeyword, returnValueToken)) {
			return true;
		}
		else return false;
	}

	public static boolean isAnEnd(String line) {
		return (LineMatcher.matchExact(line, EndKeyword));
	}
	

	
	
	public void parseHeader(String line) throws BugTrap {
		// Syntax: [access] Function DataType name (argumentsList)
		
		LineTokenizer tokenizer = new LineTokenizer(line);
		
		if (tokenizer.isAccessModifier())
			access = tokenizer.parseAccessModifier();

		if (!tokenizer.isKeyword(StartKeyword)) throw new BugTrap(this, StartKeyword + ": Keyword missing");
		tokenizer.parseKeyword(StartKeyword);

		String returnTypeString;
		if (tokenizer.isArrayDataType()) {
			returnTypeString = tokenizer.parseArrayDataType();
		}
		else if (tokenizer.isPrimitiveDataType()) {
			returnTypeString = tokenizer.parsePrimitiveDataType();
		}
		else
			throw new BugTrap(this, "DataType missing");
		returnType = DataType.parse(returnTypeString);


		if (!tokenizer.isName()) throw new BugTrap(this, "name missing");
		name = tokenizer.parseName();
		

		if (!tokenizer.isParameters()) throw new BugTrap(this, "missing or bad parameter list");
		parametersList = tokenizer.parseParameters();

		if (Debug) IOUtils.println(getClass().getSimpleName() + " " + name + " parsed header: " + line);
	}

	// A primitive check for return statements. As long as one is found, it passes.
	private boolean hasReturnStatement(ArrayList<String> code) {
		for (int index=getStartLineNumber()+1; index<getEndLineNumber()-1; index++) {
			String line = code.get(index);
			if (Return.isAReturn(line)) return true;
		}
		return false;
	}
	
	@Override
	public void parse(ArrayList<String> code, Scope scope, Block parent) throws BugTrap {
		String line = code.get(getStartLineNumber());
		parseHeader(line);
		if (!hasReturnStatement(code)) throw new BugTrap(this, "Missing Return statement.");
		super.parse(code, scope, parent);
	}

	public void setReturnValue(DataType returnValue, Context context) {
		context.setReturnValue(returnValue);
	}

	public DataType getReturnValue(Context context) throws BugTrap {
		return context.getReturnValue();
	}
	
	
	@Override
	public int execute(Context context) throws BugTrap {
		// Reset the return value before evaluating.
		// Binding must be called within MethodCall.evaluate() before calling the evaluate() in this class.
		super.execute(context);
		return ExitCode_OK;
	}
}
