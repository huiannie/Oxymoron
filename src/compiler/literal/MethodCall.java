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
package compiler.literal;

import java.util.ArrayList;

import compiler.binder.Context;
import compiler.blocks.Block;
import compiler.blocks.Function;
import compiler.blocks.Method;
import compiler.blocks.Module;
import compiler.data.DataType;
import compiler.expression.Expression;
import compiler.main.Settings;
import compiler.tokenizers.Token;
import compiler.util.BugTrap;
import compiler.util.IOUtils;

/* Method calls may include Module calls and Function calls.
 * 
 * Module calls are calls to evaluate a method.
 * Function calls are calls to evaluate a function.
 * They are different from method declarations.
 * A method must already have been declared before
 * it can be binded by a method call.
 */

public class MethodCall extends UnbindedLiteral {
	public static final boolean Debug = Settings.Debug;

	private static final String Comma = Token.Comma;
	private String name;
	private Block parent;
	private ArrayList<Expression> argumentsList;
	
	public MethodCall(String name, Block parent) {
		this.name = name;
		this.parent = parent;
	}

	public String getName() {
		return name;
	}


	public void print(int indent) {
		IOUtils.printIndented(indent, "Method call: "
							+ " name=" + name);
	}

	public void setArguments(ArrayList<Expression> argumentsList) {
		this.argumentsList = argumentsList;
	}
	public ArrayList<Expression> getArguments() {
		return argumentsList;
	}
	
	

	
	public static boolean isFunctionCallArgumentList(ArrayList<Object> argumentList) {
		
		for (int index=0; index<argumentList.size(); index+=2) {
			// Expect an expression
			if (!(argumentList.get(index) instanceof Expression))
				return false;
			
			// Every expression is followed by a comma (except the last one)
			if (index+1<argumentList.size() && argumentList.get(index+1)!=Comma)
				return false;
		}
		return true;
	}
	
	// Function calls are distinguished by name(arg1, arg2, ...)
	// There is no keyword.
	// Function calls cannot appear within a module/function declaration.
	// But they can appear in any other places.
	public static MethodCall parse(String name, ArrayList<Object> argumentList, Block callerBlock) throws BugTrap {
		// Assume that the syntax of argumentList has been verified by
		// isFunctionCallArgumentList() before parsing.

		ArrayList<Expression> exprs = new ArrayList<Expression>();
		for (int index=0; index<argumentList.size(); index+=2) {
			// An expression
			exprs.add((Expression)argumentList.get(index));
		}
		MethodCall methodCall = new MethodCall(name, callerBlock);
		
		methodCall.setArguments(exprs);
		
		return methodCall;
	}

	
	@Override
	public String valueToString() {
		return name;
	}


	@Override
	public DataType evaluate(Context context) throws BugTrap {
		// Evaluate() is only to invoke the method. It expects the method to have been declared elsewhere.
		// Before invocation, the parameter binding must be done first.
		
		Method method = context.getMethod(name);
		if (Debug) IOUtils.println("Method Call to: " + name + ": " + (method==null?"not found": "found"));

		// Check if method belongs to a class. If it does, then retrieve the class context.
		// If not, then just use the current context as owner.
		Context ownerContext = context.findOwnerClassContext(method);
		if (ownerContext==null) ownerContext = context.findOwnerProgramContext(method);
		
		if (!ownerContext.allowsAccessTo(method, context)) 
			throw new BugTrap(context, "OwnerContext " + ownerContext.getBlock().getClass().getSimpleName() + " of "
					+ method.getName() + " does not allow access to " + context.getBlock().getClass().getSimpleName() 
					+ " defined in class " + context.findCallerClassContext(method).getBlock().getClass().getSimpleName());

		Context subContext = new Context(method, ownerContext);
		method.bindParameters(subContext, context, argumentsList);
		method.execute(subContext);
		
		if (method instanceof Module) return null;
		else if (method instanceof Function) return ((Function)method).getReturnValue(subContext);
		
		throw new BugTrap(this.parent, name + " is an undeclared method.");
	}
}
