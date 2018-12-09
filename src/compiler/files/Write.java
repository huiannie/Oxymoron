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
import compiler.literal.Literal;
import compiler.main.Settings;
import compiler.parser.Scope;
import compiler.tokenizers.LineMatcher;
import compiler.tokenizers.LineTokenizer;
import compiler.util.BugTrap;
import compiler.util.IOUtils;

public class Write extends FileCommand {
	public static final boolean Debug = Settings.Debug;
	
	public static final String StartKeyword = "Write";

	Literal variableName;
	ArrayList<Expression> expressions;

	public Write(int lineNumber) {
		super(lineNumber, StartKeyword);
	}

	public static boolean isAWrite(String line) {
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
		
		// Syntax: Write fileVariable expression1 [, expression2]... where expression can be anything

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
		
		
		// Get source expressions
		String remainder = tokenizer.getRemainder();
		// Use Expression parser to convert the remainder to an arraylist of expressions.
		expressions = new ExpressionParser(remainder, scope.getStringsMap(), parent).parseToList();
	}
	
	@Override
	public int execute(Context context) throws BugTrap {
		OutputFile fileValue = (OutputFile) getValue(context, variableName);

		for (Expression expr : expressions) {
			if (Debug) IOUtils.println("expr=" + expr.toStringInBracket());
			DataType value = (DataType) expr.evaluate(context);
			fileValue.write(value);
		}

		return ExitCode_OK;
	}
}
