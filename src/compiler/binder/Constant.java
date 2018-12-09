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

import compiler.data.DataType;
import compiler.declarations.ConstantDeclaration;
import compiler.literal.BindedLiteral;
import compiler.main.Settings;
import compiler.tokenizers.LineTokenizer;
import compiler.tokenizers.Token;
import compiler.util.BugTrap;
import compiler.util.IOUtils;

public class Constant extends BindedLiteral {
	public static final boolean Debug = Settings.Debug;

	private String name;
	private DataType value;
	
	public static ConstantsMap globals;
	
	static {
		globals = new ConstantsMap();
		globals.put(Token.TabLabel, new Constant(Token.TabLabel, new compiler.data.String(Token.Tab)));
	}
	
	// Reserved for global constants.
	private Constant(String name, DataType value) {
		this.name = name;
		this.value = value;
	}

	public Constant(ConstantDeclaration declaration, Context context) throws BugTrap {
		DataType type = DataType.parse(declaration.getDatatypeString());
		this.value = type; // Record the designated type here. Later, may use it to verify any initial values.
		
		// Check the name to see if it is an array.
		LineTokenizer tokenizer = new LineTokenizer(declaration.getName());
		if (tokenizer.isArrayName()) {
			this.name = tokenizer.parseName();
			throw new BugTrap(context, "Constant cannot be an array.");
		}
		else { // Simple data type.
			this.name = declaration.getName();
			
			if (Debug) IOUtils.println("Value = " + declaration.getValue().toStringInBracket());
			// Constants are always initialized.
			
			DataType initialValue = (DataType) declaration.getValue().evaluate(context);
			// Call setValue() to ensure type compatibility.
			setValue(initialValue);
		}
		
		context.bind(this);
	}

	
	public String getName() {
		return name;
	}
	
	public DataType getValue() {
		return value;
	}

	
	// Always call setValue() to assign value to the symbol because
	// it provides compatibility check. 
	private void setValue(DataType value) throws BugTrap {
		if (this.value.getType()==value.getType())
			this.value = value;
		else if (this.value instanceof compiler.data.Integer) {
			this.value = value.toInteger();
		}
		else if (this.value instanceof compiler.data.Real) {
			this.value = value.toReal();
		}
		else if (this.value instanceof compiler.data.Character) {
			this.value = value.toCharacter();
		}
		else if (this.value instanceof compiler.data.String) {
			this.value = value.toDString();
		}
		else {
			throw new BugTrap(this, value + " not convertible to " + this.value.getType());
		}
	}
	
	public void print(int indent) {
		IOUtils.printIndented(indent, "Constant: ");
	}
	
	
	@Override
	public String valueToString() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public DataType evaluate(Context context) {
		return getValue();
	}


}
