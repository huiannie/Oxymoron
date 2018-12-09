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
package compiler.declarations;

import compiler.binder.Context;
import compiler.binder.Variable;
import compiler.binder.VariableRef;
import compiler.classes.ClassObject;
import compiler.classes.ClassType;
import compiler.data.DataType;
import compiler.data.PrimitiveDataType;
import compiler.expression.Expression;
import compiler.literal.ArrayCall;
import compiler.literal.Literal;
import compiler.literal.SymbolCall;
import compiler.literal.UnbindedLiteral;
import compiler.main.Settings;
import compiler.tokenizers.LineMatcher;
import compiler.tokenizers.Token;
import compiler.util.BugTrap;
import compiler.util.IOUtils;

public class ParameterDeclaration extends UnbindedLiteral {
	public static final boolean Debug = Settings.Debug;

	String datatypeString;
	boolean ref;
	String name;
	Literal binding;
	
	public ParameterDeclaration(String datatypeString, boolean ref, String name) {
		this.datatypeString = datatypeString;
		this.ref = ref;
		this.name = name;
	}
	
	public boolean isReference() {
		return ref;
	}
	
	public String getDatatypeString() {
		return datatypeString;
	}
	
	public String getName() {
		return name;
	}
	
	public void print(int indent) {
		IOUtils.printIndented(indent, this.getClass().getSimpleName() 
				+ " type: " + datatypeString
				+ " " + (ref?VariableRef.Keyword:"")
				+ " name: " + name);
	}
	
	
	
	@Override
	public String valueToString() {
		return "unbinded Ref " + datatypeString + " name=" + name;
	}
	
	public void bind(Context localContext, Context callerContext, Expression expression) throws BugTrap {

		
		// At binding, if the parameter is passed in by reference, 
		// link the reference variable directly to the parameter
		// Note: 
		// ClassObjects are always passed in by value. So it makes no sense to 
		// apply Ref to a ClassObject. 
		if (isReference()) {
			if (!expression.isLiteral()) throw new BugTrap("Passing by reference requires a variable");
			
			if (Debug) IOUtils.println("ParameterDeclaration: bind-by-reference " + name + " to " + expression.getLiteral().valueToString()); 

			boolean localVariableIsSimpleName = LineMatcher.matchExact(name, Token.ValidNameTagPattern);
			
			
			// Caller's argument is a symbol (which could be a variable or constant). 
			// No bracket [] is included in the argument. However, it doesn't mean
			// that the variable/constant cannot be an array.
			// The argument just isn't an ArrayCall.
			if (expression.getLiteral() instanceof SymbolCall) {
				Variable referenceVariable = null;
				Variable argument = ((SymbolCall) expression.getLiteral()).getVariable(callerContext);

				
				// Argument passed in by caller is variable of a primitive data type.
				if (argument.getValue() instanceof PrimitiveDataType) {

					// Local variable is declared with a simple name without bracket
					if (localVariableIsSimpleName) {

						// Perfect match: simple name matches primitive data type.
						referenceVariable = argument;
					}

					// Local variable is declared as a simple name with [] brackets
					else {
						throw new BugTrap(this.getClass().getSimpleName() + 
								": Local variable (" + datatypeString +") " + name 
								+ " does not match argument " + argument.getType());
					}
				}
				// Argument passed in by caller is an array.
				else {

					// Local variable is declared with a simple name without bracket
					if (localVariableIsSimpleName) {
						throw new BugTrap(this.getClass().getSimpleName() + 
								": Local variable (" + datatypeString +") " + name 
								+ " does not match argument " + argument.getType());
						
					}

					// Local variable is declared as a simple name with [] brackets
					else {
						// Perfect match: array name matches array data type.
						referenceVariable = argument;
						

					}

				}
				Variable variable = new VariableRef(this, localContext, referenceVariable);
				if (Debug) IOUtils.println("  binding " + variable.getName() + " to " + argument.getName()); 

			}
			// Caller's argument is an ArrayCall. It means that the argument accesses
			// an element of an array.
			else if (expression.getLiteral() instanceof ArrayCall) {
				ArrayCall referenceArrayCall = null;
				ArrayCall argument = ((ArrayCall) expression.getLiteral());

				// Local variable is declared with a simple name without bracket
				if (localVariableIsSimpleName) {

					// Not-so-perfect match: simple local variable name matches array element argument.
					referenceArrayCall = argument;
				}

				// Local variable is declared as a simple name with [] brackets
				else {
					throw new BugTrap(this.getClass().getSimpleName() + 
							": Local variable (" + datatypeString +") " + name 
							+ " does not match argument " + argument.valueToString());
				}

				Variable variable = new VariableRef(this, localContext, referenceArrayCall.getVariable(callerContext), referenceArrayCall.getIndices(callerContext));
				if (Debug) IOUtils.println("  binding " + variable.getName() + " to " + argument.getName()); 
				
			}
			// Could be some expression that evaluates to some data type.
			// This cannot be binded because the argument is not a variable or array or array element.
			else {
				throw new BugTrap(this.getClass().getSimpleName() + 
						": Local variable (" + datatypeString +") " + name 
						+ " does not match argument " + expression.toStringInBracket());

			}
			
		}
		
		// At binding, if the parameter is passed in by value,
		// evaluate the expression and set a copy of the value of the variable.
		// Need to explicitly copy because aggregate data type are always passed by
		// reference and must be copied.
		//
		// Now, ClassObjects may be passed by the value (of the pointer to the object itself). 
		// Effectively, no new object is created. Just a new pointer is copied. 
		// So the effect of modifying the object within a method will persist when the method completes.
		// It is necessary to distinguish whether the result should be bounded to a variable or to a class object.
		else {
			if (Debug) IOUtils.println("ParameterDeclaration: binding-by-value " + name);
			Literal literal = expression.evaluate(callerContext);
			if (literal instanceof DataType) {
				DataType value = (DataType) literal;
				Variable variable = new Variable(this, localContext);
				variable.setValue(value.copy());
				if (Debug) IOUtils.println("Variable " + variable.getName() + " is binded to " + value.valueToString());
			}
			else if (literal instanceof ClassType) {
				ClassType value = (ClassType) literal;
				ClassObject classObject = new ClassObject(this, value, localContext);
				if (Debug) IOUtils.println("ClassObject " + classObject.getName() + " is binded to " + value.valueToString());
			}
		}
		
	}
	
	@Override
	public DataType evaluate(Context context) throws BugTrap {
		throw new BugTrap("Parameter declaration is not a command by itself. " 
							+ "It is part of the header of a method, "
							+ "and is binded when a method is evaluated."
							+ "Call bind() instead.");
	}
}
