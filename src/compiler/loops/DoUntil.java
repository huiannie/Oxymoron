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
package compiler.loops;

import java.util.ArrayList;

import compiler.binder.Context;
import compiler.blocks.Block;
import compiler.expression.Expression;
import compiler.expressionparser.ExpressionParser;
import compiler.literal.Literal;
import compiler.main.Settings;
import compiler.parser.Scope;
import compiler.tokenizers.LineMatcher;
import compiler.tokenizers.LineTokenizer;
import compiler.util.BugTrap;
import compiler.util.IOUtils;

public class DoUntil extends Block {
	public static final boolean Debug = Settings.Debug;

	public static final String StartKeyword = "Do";
	public static final String EndKeyword = "Until";
	
	Expression condition=null;

	public DoUntil(int startLineNumber, int endLineNumber) {
		super(startLineNumber, endLineNumber);
		hasHeaderAndFooter = true;
	}
	
	
	
	public void print(int indent) {
		IOUtils.printIndented(indent, "DoUntil loop:");
		IOUtils.printIndented(indent+2, "Condition: " + (condition==null?"unknown":condition.toStringInBracket()));
		for (Block block : subblocks) {
			block.print(indent+2);
		}
		IOUtils.printIndented(indent, "End DoUntil loop");
	}

	public void parse(ArrayList<String> code, Scope scope, Block parent) throws BugTrap {
		if (this.parent==null) this.parent = parent;

		int start = getStartLineNumber();
		int end = getEndLineNumber() - 1;
		String line1 = code.get(start);
		String line2 = code.get(end);

		// Syntax: 
		// Do
		// Until condition
		
		if (!LineMatcher.matchExact(line1, StartKeyword)) throw new BugTrap(this, StartKeyword + " keyword missing");

		LineTokenizer tokenizer = new LineTokenizer(line2);
		if (!tokenizer.isKeyword(EndKeyword)) throw new BugTrap(this, EndKeyword + " Keyword missing");
		tokenizer.parseKeyword(EndKeyword);

		String remainder = tokenizer.getRemainder();
		condition = new ExpressionParser(remainder, scope.getStringsMap(), this).parse();
				
		super.parse(code, scope, parent);
	}

	
	@Override
	public int execute(Context context) throws BugTrap {
		boolean result;
		int exitCode;
		
		do {
			// If the loop is given its own subcontext, then it is allowed to declare local variables.
			Context subContext = new Context(this, context);
			
			exitCode = super.execute(subContext);
			if (exitCode==ExitCode_Return) return exitCode;
			
			Literal literal = condition.evaluate(context);		
			if (!(literal instanceof compiler.data.Boolean)) throw new BugTrap(this, "Expression should evaluate to True or False");
			result = ((compiler.data.Boolean) literal).getValue();
		} while (!result);

		return ExitCode_OK;
	}
}
