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
import compiler.declarations.ParameterDeclaration;
import compiler.literal.BindedLiteral;
import compiler.literal.ValueType;
import compiler.main.Settings;
import compiler.tokenizers.LineTokenizer;
import compiler.util.BugTrap;

public class ClassObject extends BindedLiteral {
	public static final boolean Debug = Settings.Debug;

	ValueType value;
	String name;
	

	public ClassObject(ClassObjectDeclaration declaration, Context context) throws BugTrap {
		ClassType type = declaration.getClasstype();

		this.value = type; // Record the designated type here. Later, may use it to verify any initial values.
		
		// Check the name to see if it is an array.
		// Format: A[1], A[1][2] etc.
		LineTokenizer tokenizer = new LineTokenizer(declaration.getName());
		if (tokenizer.isArrayName()) {
			this.name = tokenizer.parseName();
			this.value = ClassArray.parse(type, declaration.getName(), declaration.getInitialValues(), context);
		}
		else { // Name suggests the variable is either a primitive data type or a file data type.
			this.name = declaration.getName();
			if (declaration.isInitialized()) {
				ClassType initialValue = (ClassType) declaration.getInitialValues().get(0).evaluate(context);
				// Call setValue() to ensure type compatibility.
				setValue(initialValue);
			}
			else
				value = type;			
		}
		context.bind(this);		
	}
	
	// Use this constructor to bind a class object to the same value.
	// Class objects are effectively pointers. When a classType is passed as parameter into a method,
	// the method simply creates a pointer to point to the same classType. No new classType body is created.
	public ClassObject(ParameterDeclaration declaration, ClassType value, Context context) throws BugTrap {
		this.name = declaration.getName();
		this.value = value;
		context.bind(this);	 
	}
	
	
	
	public ValueType getValue() {
		return value;
	}
	
	public void setValue(ClassType newValue) throws BugTrap {
		if (this.value instanceof ClassType) {
			ClassBlock oldType = ((ClassType) value).classBlock;
			ClassBlock newType = newValue.classBlock;
			if (newType==oldType || newType.isExtendedFrom(oldType))
				this.value = newValue;
			else 
				throw new BugTrap(this, newValue.getType() + " not convertible to " + this.value.getType());
		}
	}

	
	@Override
	public String valueToString() {
		return "Class " + value.getType() + " object name: " + name;
	}
	


	public String getName() {
		return name;
	}

	

	
	@Override
	public ValueType evaluate(Context context) throws BugTrap {
		return value;
	}


}
