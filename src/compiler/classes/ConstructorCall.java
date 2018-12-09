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
package compiler.classes;

import java.util.ArrayList;

import compiler.binder.Context;
import compiler.blocks.Block;
import compiler.expression.Expression;
import compiler.expressionparser.ExpressionParser;
import compiler.literal.Literal;
import compiler.literal.MethodCall;
import compiler.literal.UnbindedLiteral;
import compiler.main.Settings;
import compiler.parser.Scope;
import compiler.tokenizers.LineTokenizer;
import compiler.util.BugTrap;
import compiler.util.IOUtils;

public class ConstructorCall extends UnbindedLiteral {
	public static final boolean Debug = Settings.Debug;

	public static final String StartKeyword = "New";
	
	
	private String name;
	private Block parent;
	private ArrayList<Expression> argumentsList;

	public ConstructorCall(String name, ArrayList<Expression> argumentsList, Block parent) {
		this.name = name;
		this.argumentsList = argumentsList;
		this.parent = parent;
	}	
	

	@Override
	public String valueToString() {
		return name;
	}
	
	public void print(int indent) {
		IOUtils.printIndented(indent, "Constructor call for class " + name);
		for (Expression e : argumentsList) {
			IOUtils.printIndented(indent+1, "Constructor call argument " + e.toStringInBracket());
		}
		IOUtils.printIndented(indent, "End of constructor call for class " + name);
	}
	
	
	public static Literal parse(String line, Scope scope, Block parent) throws BugTrap {
		// Syntax: New className(argumentList)
						
		LineTokenizer tokenizer = new LineTokenizer(line);

		// Check for the keyword "New"
		if (!tokenizer.isKeyword(StartKeyword)) throw new BugTrap(parent, "Missing keyword " + StartKeyword);
		tokenizer.parseKeyword(StartKeyword);
		
		
		// Use the Expression parser to convert the remainder to an expression.
		// Make sure to check that the root node of the expression is a function call or a class object call.
		String remainder = tokenizer.getRemainder();
		Expression expression = new ExpressionParser(remainder, scope.getStringsMap(), parent).parse();
		
		
		if (!(expression.getLiteral() instanceof MethodCall)) throw new BugTrap(parent, "Missing constructor call");
		MethodCall literal = (MethodCall) expression.getLiteral();

		// The expression parser only recognize the call as a method call. 
		// Replace the method call by a constructor call.
		ConstructorCall constructorCall = new ConstructorCall(literal.getName(), literal.getArguments(), parent);
		
		return constructorCall;
	}

	
	
	
	@Override
	public ClassType evaluate(Context context) throws BugTrap {
		// Evaluate() is only to invoke the method. It expects the method to have been declared elsewhere.
		// Before invocation, the parameter binding must be done first.
		
		
		ClassType classType = context.getClassType(name);
		if (Debug) IOUtils.println("Constuctor Call to: " + name + ": " + (classType==null?"not found": "found"));

		// A new classType instance is created. It is binded. The context of this classType has no parent.
		Context classTypeContext = new Context(classType.classBlock, null);
		classType.setRootContext(classTypeContext);
		classType.bindConstructor(context, argumentsList);
		return classType;
		
	}


}
