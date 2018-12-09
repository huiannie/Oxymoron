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

import compiler.binder.Context;
import compiler.binder.Variable;
import compiler.binder.VariableRef;
import compiler.blocks.Block;
import compiler.data.DataType;
import compiler.main.Settings;
import compiler.util.BugTrap;
import compiler.util.IOUtils;

/* A String call emulates Array call.
 */

public class StringCall extends ArrayCall {
	public static final boolean Debug = Settings.Debug;

	
	public StringCall(String name, Block parent) {
		super(name, parent);
	}


	@Override
	public String getDataType(Context context) throws BugTrap {
		Variable variable = context.getVariable(name);
		if (!(variable.getValue() instanceof compiler.data.String)) throw new BugTrap(context, "String expected.");
		return variable.getValue().getType();
	}
	
	
	@Override
	public void setValue(Context context, DataType newValue) throws BugTrap {
		Variable variable = context.getVariable(name);
		if (!(variable.getValue() instanceof compiler.data.String)) throw new BugTrap(context, "String expected.");

		compiler.data.String string = (compiler.data.String) variable.getValue();
		int indices[] = getIndices(context);
		if (indices.length>1) throw new BugTrap(context, "String index should have only 1 dimension.");

		if (Debug) {
			String s = indicesToString(indices);
			IOUtils.println(getClass().getSimpleName()+".setValue on " + variable.getName() + " indices: " + s);
			if (variable instanceof VariableRef) {
				IOUtils.println(variable.getName() + " is a VariableRef ");
			}
		}
		string.setElement(newValue.toCharacter(), indices[0]);
	}
	
	@Override
	public DataType evaluate(Context context) throws BugTrap {
		// evaluate() is only to get the array element. 
		// To set the element use setValue().
		
		if (Debug) IOUtils.println(getClass().getSimpleName() +" to :" + name);
		Variable variable = context.getVariable(name);
		if (!(variable.getValue() instanceof compiler.data.String)) throw new BugTrap(context, "String is expected.");

		compiler.data.String string = (compiler.data.String) variable.getValue();
		int indices[] = getIndices(context);
		if (indices.length>1) throw new BugTrap(context, "String index should have only 1 dimension.");
		
		if (Debug) {
			String s = indicesToString(indices);
			IOUtils.println(getClass().getSimpleName() +": " + name + s);
			IOUtils.println(getClass().getSimpleName() +": " + name + s + " value is " + string.getElement(indices[0]).valueToString());
		}

		return string.getElement(indices[0]);
	}

	
}
