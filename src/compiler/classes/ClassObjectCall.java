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

import compiler.binder.Context;
import compiler.blocks.Block;
import compiler.data.DataType;
import compiler.expression.Expression;
import compiler.literal.ArrayCall;
import compiler.literal.Literal;
import compiler.literal.MethodCall;
import compiler.literal.SymbolCall;
import compiler.literal.UnbindedLiteral;
import compiler.literal.ValueType;
import compiler.main.Settings;
import compiler.util.BugTrap;
import compiler.util.IOUtils;

/* Function calls are calls to evaluate a function.
 * They are different from function declarations.
 * A function must already have been declared before
 * it can be binded by a function call.
 */

public class ClassObjectCall extends UnbindedLiteral {
	public static final boolean Debug = Settings.Debug;

	String name;
	Block parent;
	Literal argument;
	ArrayCall arrayCall;
	
	
	// Syntax 1: classType.methodCall
	// Syntax 2: classArray[x][y].methodCall

	public ClassObjectCall(String name, Literal argument, Block parent) {
		this.name = name;
		this.parent = parent;
		this.argument = argument;
		this.arrayCall = null;
	}

	public ClassObjectCall(ArrayCall arrayCall, Literal argument, Block parent) {
		this.name = arrayCall.getName();
		this.parent = parent;
		this.arrayCall = arrayCall;
		this.argument = argument;
	}

	public String getName() {
		return name;
	}


	public void print(int indent) {
		IOUtils.printIndented(indent, "Object call: "
							+ " name=" + name);
	}

	public void setArgument(Literal argument) {
		this.argument = argument;
	}
	public Literal getArguments() {
		return argument;
	}
	

	public static boolean isClassObjectCallArgument(Object argument) {
		if (!(argument instanceof Expression) 
			|| !((Expression)argument).isLiteral()) return false;
		
		Literal literal = ((Expression)argument).getLiteral();
		if (literal instanceof SymbolCall 
			|| literal instanceof ArrayCall
			|| literal instanceof MethodCall
			|| literal instanceof ClassObjectCall)
			return true;
		return false;
	}
	

	
	@Override
	public String valueToString() {
		return name;
	}


	@Override
	public DataType evaluate(Context context) throws BugTrap {
		ClassObject classObject = context.getClassObject(name);
		
		// Evaluate() is only to invoke the method. It expects the method to have been declared elsewhere.
		// Before invocation, the parameter binding must be done first.
		
		
		if (argument instanceof MethodCall) {
			MethodCall methodCall = (MethodCall) argument;
			
			if (Debug) IOUtils.println("Going to invoke method " + methodCall.getName() + " in object " + name); 
			
			ValueType classValue = classObject.getValue();
			DataType returnValue = null;
			if (classValue instanceof ClassType)
				returnValue = ((ClassType)classValue).evaluate(methodCall, context);
			else if (classValue instanceof ClassArray) {
				returnValue = (new ClassArrayCall(arrayCall)).evaluate(context).evaluate(methodCall, context);
			}

			if (Debug) IOUtils.println("Returning from method " + methodCall.getName() + " in object " + name); 
			
			return returnValue;
		}
		else {
			// Not ready to handle class fields that are public.
			throw new BugTrap(context, this, "Direct call to public fields in class discouraged." + name);
		}
	}


}
