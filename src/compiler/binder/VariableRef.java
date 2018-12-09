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
import compiler.main.Settings;
import compiler.util.BugTrap;
import compiler.util.IOUtils;

public class VariableRef extends Variable {
	public static final boolean Debug = Settings.Debug;

	public static final String Keyword = "Ref";

	Variable referenceVariable = null;
	int[] indices = null;
	
	// For parameter passing by reference, the parameter is declared as a VariableRef.
	
	// If the argument is a simple variable, bind it.
	public VariableRef(ParameterDeclaration declaration, Context context, Variable referenceVariable) throws BugTrap {
		super(declaration, context);
		this.referenceVariable = referenceVariable;
	}

	
	public VariableRef(ParameterDeclaration declaration, Context context, Variable referenceVariable, int[] indices) throws BugTrap {
		super(declaration, context);
		this.referenceVariable = referenceVariable;
		this.indices = indices;
	}


	
	public boolean hasIndices() {
		return indices!=null;
	}
	
	@Override
	public DataType getValue() {
		if (hasIndices()) {
			if (Debug) IOUtils.println("getValue(): Variable Ref " + name + " mapped to " + referenceVariable.getName() + Array.indicesToString(indices));
			Array array = (Array) referenceVariable.getValue();
			if (Debug) IOUtils.println("  retrieving value " + array.getElement(indices).valueToString());
			return array.getElement(indices);
		}
		return referenceVariable.getValue();
	}
	
	@Override
	public String getType() {
		return referenceVariable.getType();
	}

	@Override
	public void setValue(DataType newValue) throws BugTrap {
		if (hasIndices()) {
			if (Debug) IOUtils.println("setValue(): Variable Ref " + name + " mapped to " + referenceVariable.getName() + Array.indicesToString(indices));
			Array array = (Array) referenceVariable.getValue();
			array.setElement(newValue, indices);
			return;
		}
		referenceVariable.setValue(newValue);
	}


	@Override
	public void print(int indent) {
		IOUtils.printIndented(indent, "VariableRef: "
				+ " name=" + name);
	}

	@Override
	public String valueToString() {
		return referenceVariable.valueToString();
	}

	@Override
	public DataType evaluate(Context context) throws BugTrap {
		return referenceVariable.getValue();
	}

}
