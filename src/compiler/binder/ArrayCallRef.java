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
import compiler.declarations.ParameterDeclaration;
import compiler.literal.ArrayCall;
import compiler.util.BugTrap;
import compiler.util.IOUtils;

public class ArrayCallRef extends Variable {

	public static final String Keyword = "Ref";

	Variable referenceVariable = null;
	ArrayCall referenceArrayCall = null;
	
	// For parameter passing by reference, the parameter is declared as a VariableRef.
	
	// If the argument is a simple variable, bind it.
	public ArrayCallRef(ParameterDeclaration declaration, Context context, Variable referenceVariable) throws BugTrap {
		super(declaration, context);
		this.referenceVariable = referenceVariable;
	}

	// If the argument is an ArrayCall, then bind the call
	public ArrayCallRef(ParameterDeclaration declaration, Context context, ArrayCall referenceArrayCall) throws BugTrap {
		super(declaration, context);
		this.referenceArrayCall = referenceArrayCall;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	public boolean isReferenceVariable() {
		return referenceVariable!=null;
	}
	public boolean isReferenceArrayCall() {
		return referenceArrayCall!=null;
	}
	
	public DataType getValue(Context context) throws BugTrap {
		if (isReferenceVariable())
			return referenceVariable.getValue();
		else
			return referenceArrayCall.evaluate(context);
	}
	
	public String getType() {
		return referenceVariable.getType();
	}

	public void setValue(DataType value) throws BugTrap {
		referenceVariable.setValue(value);
	}


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
