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
package compiler.binder;

import compiler.data.Array;
import compiler.data.DataType;
import compiler.declarations.ParameterDeclaration;
import compiler.declarations.VariableDeclaration;
import compiler.files.FileDataType;
import compiler.literal.BindedLiteral;
import compiler.main.Settings;
import compiler.tokenizers.LineTokenizer;
import compiler.util.BugTrap;
import compiler.util.IOUtils;

public class Variable extends BindedLiteral {
	public static final boolean Debug = Settings.Debug;

	protected String name;
	private DataType value;
	
	public Variable(VariableDeclaration variableDeclaration, Context context) throws BugTrap {
		DataType type = DataType.parse(variableDeclaration.getDatatypeString());
		
		
		this.value = type; // Record the designated type here. Later, may use it to verify any initial values.
		
		// Check the name to see if it is an array.
		// Format: A[1], A[1][2] etc.
		LineTokenizer tokenizer = new LineTokenizer(variableDeclaration.getName());
		if (tokenizer.isArrayName()) {
			this.name = tokenizer.parseName();
			this.value = Array.parse(type.getType(), variableDeclaration.getName(), variableDeclaration.getInitialValues(), context);
		}
		else { // Name suggests the variable is either a primitive data type or a file data type.
			
			if (type.isPrimitive()) {
				this.name = variableDeclaration.getName();
				if (variableDeclaration.isInitialized()) {
					DataType initialValue = (DataType) variableDeclaration.getInitialValues().get(0).evaluate(context);
					// Call setValue() to ensure type compatibility.
					setValue(initialValue);
				}
				else
					value = type;				
			}
			else if (type.isFile()) {
				this.name = variableDeclaration.getName();
				if (variableDeclaration.isInitialized()) {
					DataType initialValue = (DataType) variableDeclaration.getInitialValues().get(0).evaluate(context);
					// Call setValue() to ensure type compatibility.
					setValue(initialValue);
				}
				else
					value = (FileDataType) type;

				// Since this is a file, check for mode.
				if (variableDeclaration.getMode()!=null) {
					((FileDataType) value).setMode(variableDeclaration.getMode());
				}
			}			
			else {
				throw new BugTrap(context, " bad data type: " + type.getType());
			}
			
		}
		
		context.bind(this);
	}
	
	
	
	// For parameter passing by value, the parameter is declared as a variable.
	public Variable(ParameterDeclaration declaration, Context context) throws BugTrap {
		DataType type = DataType.parse(declaration.getDatatypeString());
	
		this.value = type; // Record the designated type here. Later, may use it to verify any initial values.
		
		
		// Check the name to see if it is an array. 
		LineTokenizer tokenizer = new LineTokenizer(declaration.getName());
		if (tokenizer.isArrayName()) {
			this.name = tokenizer.parseName();
			this.value = Array.parse(type.getType(), declaration.getName(), null, context);
		}
		// If the name is an array that appears in parameter declaration,
		// it has format A[], A[][] etc.
		else if (tokenizer.isArrayNameWithEmptyIndexBracket()) {
			this.name = tokenizer.parseName();
			if (Debug) IOUtils.println("new Variable: " + this.name + " requires dummy array.");
			this.value = new Array(type);
		}
		else { // Simple data type.
			this.name = declaration.getName();
			// Parameters are never initialized. But they are binded at runtime.
			value = DataType.parse(declaration.getDatatypeString());			
		}
		
		context.bind(this);
	}
	
	
	
	public String getName() {
		return name;
	}
	
	public DataType getValue() {
		return value;
	}

	public void setValue(DataType newValue) throws BugTrap {
		if (this.value.getType()==newValue.getType()) {
			this.value = newValue;
		}
		else if (this.value instanceof compiler.data.Integer) {
			this.value = newValue.toInteger();
		}
		else if (this.value instanceof compiler.data.Real) {
			this.value = newValue.toReal();
		}
		else if (this.value instanceof compiler.data.Character) {
			this.value = newValue.toCharacter();
		}
		else if (this.value instanceof compiler.data.String) {
			this.value = newValue.toDString();
		}
		else {
			throw new BugTrap(this, newValue + " not convertible to " + this.value.getType());
		}
	}

	public String getType() {
		return value.getType();
	}

	public void print(int indent) {
		IOUtils.printIndented(indent, "Variable: "
				+ " name=" + name);
	}

	@Override
	public String valueToString() {
		return value.valueToString();
	}

	@Override
	public DataType evaluate(Context context) throws BugTrap {
		return getValue();
	}

}
