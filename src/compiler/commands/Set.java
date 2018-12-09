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
import compiler.classes.ClassArrayCall;
import compiler.classes.ClassType;
import compiler.classes.ConstructorCall;
import compiler.data.DataType;
import compiler.expression.Expression;
import compiler.expressionparser.ExpressionParser;
import compiler.literal.ArrayCall;
import compiler.literal.Literal;
import compiler.literal.SymbolCall;
import compiler.literal.ValueType;
import compiler.main.Settings;
import compiler.parser.Scope;
import compiler.tokenizers.LineMatcher;
import compiler.tokenizers.LineTokenizer;
import compiler.util.BugTrap;
import compiler.util.IOUtils;

public class Set extends Command {
	public static final boolean Debug = Settings.Debug;

	public static final String StartKeyword = "Set";

	Expression expression;
	Literal variableName;
	
	
	public Set(int lineNumber) {
		super(lineNumber, StartKeyword);
	}

	public static boolean isASet(String line) {
		if (LineMatcher.matchStart(line, StartKeyword)) return true;
		else return false;
	}

	@Override
	public void print(int indent) {
		IOUtils.printIndented(indent, getClass().getSimpleName() 
								+ " range: " + startLineNumber + "-" + endLineNumber);
		IOUtils.printIndented(indent+1, "variable = " + (variableName==null?"unknown":variableName.valueToString()));
		if (expression!=null) {
			IOUtils.printIndented(indent+1, "expression = " + expression.toStringInBracket());
		}
	}


	
	
	@Override
	public void parse(ArrayList<String> code, Scope scope, Block parent) throws BugTrap {
		if (this.parent==null) this.parent = parent;

		int lineNumber = getStartLineNumber();
		String line = code.get(lineNumber);
		
		// Syntax 1: Set name = expression (which is a tree with one root)
		// Syntax 2: Set arrayname = expression (which is a tree with one root)

		LineTokenizer tokenizer = new LineTokenizer(line);
		
		if (!tokenizer.isKeyword(StartKeyword)) throw new BugTrap(this, StartKeyword + ": Keyword missing");
		tokenizer.parseKeyword(StartKeyword);

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
			throw new BugTrap(this, ": unrecognized name");
		}
		
		
		if (!tokenizer.isAssignSign()) throw new BugTrap(this, "Missing = sign.");
		tokenizer.parseAssignSign();


		// For now, require that the dynamic creation of a class object must be called from the Set.
		if (tokenizer.isKeyword(ConstructorCall.StartKeyword)) {
			String remainder = tokenizer.getRemainder();
			expression = new Expression(ConstructorCall.parse(remainder, scope, parent) );
		}
		else {
			// Use the expression parser to convert the remainder to an expression.
			String remainder = tokenizer.getRemainder();
			expression = new ExpressionParser(remainder, scope.getStringsMap(), parent).parse();			
		}
	}
	
	
	
	@Override
	public int execute(Context context) throws BugTrap {
		if (Debug) IOUtils.println("Setting variable " + variableName.valueToString() + " to expression " + expression.toStringInBracket());

		if (variableName instanceof SymbolCall) {
			SymbolCall symbolCall = (SymbolCall) variableName;
			
			ValueType newValue = expression.evaluate(context);
			if (newValue instanceof DataType)
				symbolCall.setValue(context, (DataType) newValue);
			else if (newValue instanceof ClassType)
				symbolCall.setValue(context, (ClassType) newValue);
			else
				throw new BugTrap(this, "Bad assignment ");
		}
		else if (variableName instanceof ArrayCall) {
			ArrayCall arrayCall = (ArrayCall) variableName;
			
			ValueType newValue = expression.evaluate(context);
			if (newValue instanceof DataType)
				arrayCall.setValue(context, (DataType) newValue);
			else if (newValue instanceof ClassType)
				new ClassArrayCall(arrayCall).setValue(context, (ClassType)newValue);
			else
				throw new BugTrap(this, "Bad assignment ");
		}
		return ExitCode_OK;
	}

}
