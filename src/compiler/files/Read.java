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

import java.util.ArrayList;

import compiler.binder.Context;
import compiler.blocks.Block;
import compiler.data.DataType;
import compiler.expression.Expression;
import compiler.expressionparser.ExpressionParser;
import compiler.literal.ArrayCall;
import compiler.literal.Literal;
import compiler.literal.SymbolCall;
import compiler.main.Settings;
import compiler.parser.Scope;
import compiler.tokenizers.LineMatcher;
import compiler.tokenizers.LineTokenizer;
import compiler.util.BugTrap;
import compiler.util.IOUtils;

public class Read extends FileCommand {
	public static final boolean Debug = Settings.Debug;
	
	public static final String StartKeyword = "Read";
	
	Literal variableName;
	ArrayList<Literal> destVariableNames = new ArrayList<Literal>();


	public Read(int lineNumber) {
		super(lineNumber, StartKeyword);
	}

	public static boolean isARead(String line) {
		if (LineMatcher.matchStart(line, StartKeyword)) return true;
		else return false;
	}
	
	@Override
	public void print(int indent) {
		IOUtils.printIndented(indent, getClass().getSimpleName() 
								+ " range: " + startLineNumber + "-" + endLineNumber);
		// TODO
	}



	@Override
	public void parse(ArrayList<String> code, Scope scope, Block parent) throws BugTrap {
		if (this.parent==null) this.parent = parent;

		int lineNumber = getStartLineNumber();
		String line = code.get(lineNumber);
		
		// Syntax: Read fileVariable variable1 [, variable2]..

		LineTokenizer tokenizer = new LineTokenizer(line);
		
		if (!tokenizer.isKeyword(StartKeyword)) throw new BugTrap(this, StartKeyword + ": Keyword missing");
		tokenizer.parseKeyword(StartKeyword);

		// Get file variable name
		if (tokenizer.isArrayName()) {
			String arrayname = tokenizer.parseArrayName();
			Expression expr = new ExpressionParser(arrayname, scope.getStringsMap(), parent).parse();
			if (!expr.isLiteral()) throw new BugTrap(this, "Bad variable " + arrayname);
			variableName = expr.getLiteral();
		}
		else if (tokenizer.isName()) {
			String name = tokenizer.parseName();
			Expression expr = new ExpressionParser(name, scope.getStringsMap(), parent).parse();
			if (!expr.isLiteral()) throw new BugTrap(this, "Bad variable " + name);
			variableName = expr.getLiteral();
		}
		else {
			throw new BugTrap(this, "unrecognized name");
		}
		
		

		// Get destination variable names
		String remainder = tokenizer.getRemainder();
		String args[] = remainder.split(",");
		for (String arg : args) {
			LineTokenizer tokenizer2 = new LineTokenizer(arg);
			
			if (tokenizer2.isArrayName()) {
				String arrayname = tokenizer2.parseArrayName();
				Expression expr = new ExpressionParser(arrayname, scope.getStringsMap(), parent).parse();
				if (!expr.isLiteral()) throw new BugTrap(this, "Bad variable " + arrayname);
				destVariableNames.add(expr.getLiteral());
			}
			else if (tokenizer2.isName()) {
				String name = tokenizer2.parseName();
				Expression expr = new ExpressionParser(name, scope.getStringsMap(), parent).parse();
				if (!expr.isLiteral()) throw new BugTrap(this, "Bad variable " + name);
				destVariableNames.add(expr.getLiteral());
				
				if (Debug) IOUtils.println("Parsing read to variable " + expr.getLiteral().valueToString());
			}
			else {
				throw new BugTrap(this, "unrecognized name");
			}
		}

		
	}
	
	@Override
	public int execute(Context context) throws BugTrap {
		
		InputFile fileValue = (InputFile) getValue(context, variableName);

		if (Debug) IOUtils.println("Executing read to variable ");
		
		for (Literal literal : destVariableNames) {
			if (Debug) {
				IOUtils.println("reading variable " + literal.valueToString());
			}
			
			if (literal instanceof SymbolCall) {
				SymbolCall variableCall = (SymbolCall) literal;
				String type = variableCall.getDataType(context);
				if (Debug) {
					IOUtils.println(getClass().getSimpleName() + ": going to save input to variable " + variableCall.getName());
				}
				DataType newValue = fileValue.read(type);
				variableCall.setValue(context, newValue);
			}
			else if (literal instanceof ArrayCall) {
				ArrayCall arrayCall = (ArrayCall) literal;
				String type = arrayCall.getDataType(context);
				DataType newValue = fileValue.read(type);
				arrayCall.setValue(context, newValue);
			}
		}
		
		return ExitCode_OK;
	}
}
