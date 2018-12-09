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

import java.io.PrintStream;
import java.util.ArrayList;

import compiler.binder.Context;
import compiler.blocks.Block;
import compiler.data.DataType;
import compiler.expression.Expression;
import compiler.expressionparser.ExpressionParser;
import compiler.main.Settings;
import compiler.parser.Scope;
import compiler.tokenizers.LineMatcher;
import compiler.tokenizers.LineTokenizer;
import compiler.util.BugTrap;
import compiler.util.IOUtils;

public class Display extends Command {
	public static final boolean Debug = Settings.Debug;
	
	public static final String StartKeyword = "Display";

	

	ArrayList<Expression> expressions;

	public Display(int lineNumber) {
		super(lineNumber, StartKeyword);
	}

	public static boolean isADisplay(String line) {
		if (LineMatcher.matchStart(line, StartKeyword)) return true;
		else return false;
	}

	public ArrayList<Expression> getExpressions() {
		return expressions;
	}
	
	@Override
	public void print(int indent) {
		IOUtils.printIndented(indent, getClass().getSimpleName() 
								+ " range: " + startLineNumber + "-" + endLineNumber);
		if (expressions!=null) {
			for (Expression e : expressions) {
				IOUtils.printIndented(indent+1, "expression = " + e.toStringInBracket());
			}
		}
	}


	
	@Override
	public void parse(ArrayList<String> code, Scope scope, Block parent) throws BugTrap {
		if (this.parent==null) this.parent = parent;

		int lineNumber = getStartLineNumber();
		String line = code.get(lineNumber);
		
		// Syntax 1: Display
		// Syntax 2: Display expr [, expr] where expr can be anything

		LineTokenizer tokenizer = new LineTokenizer(line);
		
		if (!tokenizer.isKeyword(StartKeyword)) throw new BugTrap(this, StartKeyword + ": Keyword missing");
		tokenizer.parseKeyword(StartKeyword);

		String remainder = tokenizer.getRemainder();

		if (remainder.length()==0) {
			// Without argument.
			expressions = new ArrayList<Expression>();
		}
		else {
			// With argument.
			// Use Expression parser to convert the remainder to an arraylist of expressions.
			expressions = new ExpressionParser(remainder, scope.getStringsMap(), parent).parseToList();
		}
	}
	
	
	@Override
	public int execute(Context context) throws BugTrap {
		PrintStream outstream = Settings.GetOutstream();
		
		for (Expression expr : expressions) {
			
			if (Debug) IOUtils.println(getClass().getSimpleName() + ": expr=" + expr.toStringInBracket());
			DataType value = (DataType) expr.evaluate(context);

			outstream.print(value.valueToString());
		}
		outstream.println();
		return ExitCode_OK;
	}

}
