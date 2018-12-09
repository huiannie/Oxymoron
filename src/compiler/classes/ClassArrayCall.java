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
import compiler.data.DataType;
import compiler.data.Integer;
import compiler.expression.Expression;
import compiler.literal.ArrayCall;
import compiler.literal.UnbindedLiteral;
import compiler.main.Settings;
import compiler.util.BugTrap;
import compiler.util.IOUtils;

/* An array access is different from an array declaration.
 * An array must be declared before it can be accessed.
 * Access may be for reading or writing.
 */

public class ClassArrayCall extends UnbindedLiteral {
	public static final boolean Debug = Settings.Debug;

	String name;
	Block parent;
	ArrayList<Expression> argumentsList;
	
	public ClassArrayCall(ArrayCall arrayCall) {
		this.name = arrayCall.getName();
		this.parent = arrayCall.getParent();
		this.argumentsList = arrayCall.getArguments();
	}

	public String getName() {
		return name;
	}


	public void print(int indent) {
		IOUtils.printIndented(indent, this.getClass().getSimpleName() 
							+ ": name=" + name);
	}

	
	@Override
	public String valueToString() {
		String s = "";
		for (Expression e : argumentsList) {
			s += "[" + e.toStringInBracket() + "]";
		}
		return name + s;
	}
	

	public String getClassType(Context context) throws BugTrap {
		ClassObject classObject = context.getClassObject(name);
		return classObject.getValue().getType();
	}
	
	public ClassObject getClassObject(Context context) throws BugTrap {
		return context.getClassObject(name);
	}
	
	public int[] getIndices(Context context) throws BugTrap {
		int indices[] = new int[argumentsList.size()];
		for (int i=0; i<indices.length; i++) {
			DataType value = (DataType) argumentsList.get(i).evaluate(context);
			if (!(value instanceof compiler.data.Integer)) throw new BugTrap(context, "Array has bad index.");
			indices[i] = ((Integer) value).getValue();
		}
		return indices;
	}
	
	public void setValue(Context context, ClassType newValue) throws BugTrap {
		ClassObject classObject = getClassObject(context);
		ClassArray array = (ClassArray) classObject.getValue();
		int indices[] = getIndices(context);
		array.setElement(newValue, indices);
	}

	public String indicesToString(int indices[]) {
		String s = "";
		for (int i=0; i<indices.length; i++) {
			s+="[" + indices[i] + "]";
		}
		return s;
	}
	
	@Override
	public ClassType evaluate(Context context) throws BugTrap {
		// evaluate() is only to get the array/string element. 
		// To set the element use setValue().

		if (Debug) IOUtils.println(getClass().getSimpleName() + " to :" + name);
		ClassObject classObject = getClassObject(context);
		ClassArray array = (ClassArray) classObject.getValue();
		int indices[] = getIndices(context);
		return array.getElement(indices);		
	}

	
}
