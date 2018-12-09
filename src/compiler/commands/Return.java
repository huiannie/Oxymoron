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

import java.util.ArrayList;

import compiler.binder.Context;
import compiler.blocks.Block;
import compiler.blocks.Function;
import compiler.data.DataType;
import compiler.expression.Expression;
import compiler.expressionparser.ExpressionParser;
import compiler.main.Settings;
import compiler.parser.Scope;
import compiler.tokenizers.LineMatcher;
import compiler.tokenizers.LineTokenizer;
import compiler.util.BugTrap;
import compiler.util.IOUtils;

public class Return extends Command {
	public static final boolean Debug = Settings.Debug;

	public static final String StartKeyword = "Return";
	
	Expression expression;
	
	public Return(int lineNumber) {
		super(lineNumber, StartKeyword);
	}

	public static boolean isAReturn(String line) {
		if (LineMatcher.matchStart(line, StartKeyword)) return true;
		else return false;
	}
	
	
	@Override
	public void print(int indent) {
		IOUtils.printIndented(indent, getClass().getSimpleName() 
								+ " range: " + startLineNumber + "-" + endLineNumber);
		if (expression!=null) {
			IOUtils.printIndented(indent+1, "expression = " + expression.toStringInBracket());
		}
	}



	@Override
	public void parse(ArrayList<String> code, Scope scope, Block parent) throws BugTrap {
		if (this.parent==null) this.parent = parent;

		int lineNumber = getStartLineNumber();
		String line = code.get(lineNumber);
		
		// Syntax: Return expression (which is a tree with one root)

		LineTokenizer tokenizer = new LineTokenizer(line);
		
		if (!tokenizer.isKeyword(StartKeyword)) throw new BugTrap(this, StartKeyword + ": Keyword missing");
		tokenizer.parseKeyword(StartKeyword);

		// Use the expression parser to convert the remainder to an expression.
		String remainder = tokenizer.getRemainder();
		expression = new ExpressionParser(remainder, scope.getStringsMap(), parent).parse();
	}
	
	
	@Override
	public int execute(Context context) throws BugTrap {
		// Find the function
		Context functionContext = context.findEnclosingFunctionContext();
		Function function = (Function) functionContext.getBlock();
		function.setReturnValue((DataType) expression.evaluate(context), functionContext);
		return ExitCode_Return;		
	}

}
