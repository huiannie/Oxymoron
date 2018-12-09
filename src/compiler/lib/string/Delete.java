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
package compiler.lib.string;

import compiler.binder.Context;
import compiler.binder.Variable;
import compiler.binder.VariableRef;
import compiler.data.DataType;
import compiler.lib.LibModule;
import compiler.tokenizers.Token;
import compiler.util.BugTrap;

public class Delete extends LibModule {

	// void delete(String value1, Integer value2, Integer value3)
	public static final String name = "delete";
	private static final String Param1 = "value1";
	private static final String Param2 = "value2";
	private static final String Param3 = "value3";
	public static final String parameters = compiler.data.String.StartKeyword + Token.space + VariableRef.Keyword + Token.space + Param1 + ","
											+ compiler.data.Integer.StartKeyword + Token.space + Param2 + "," 
											+ compiler.data.Integer.StartKeyword + Token.space + Param3;

	
	public Delete() throws BugTrap {
		super(name, parameters);
	}

	@Override
	public int execute(Context context) throws BugTrap {
		DataType value1 = context.getVariable(Param1).getValue();
		DataType value2 = context.getVariable(Param2).getValue();
		DataType value3 = context.getVariable(Param3).getValue();
		
		java.lang.String x;
		
		if (value1 instanceof compiler.data.String && value2 instanceof compiler.data.Integer && value3 instanceof compiler.data.Integer) {
			// Both start and end are included in the deletion.
			int start = ((compiler.data.Integer) value2).getValue();
			int end = ((compiler.data.Integer) value3).getValue();

			x = ((compiler.data.String) value1).getValue();

			Variable var = context.getVariable(Param1);

			compiler.data.String newValue;
			if (start>0 && end<x.length()-1)
				newValue = new compiler.data.String( x.substring(0, start) + x.substring(end+1, x.length()) );
			else if (start==0)
				newValue = new compiler.data.String( x.substring(end+1, x.length()) );
			else if (end==x.length()-1)
				newValue = new compiler.data.String( x.substring(0, start) );
			else
				throw new BugTrap(this, "Bad index " + start + " and/or " + end);
				
			var.setValue(newValue);
		}
		else {
			throw new BugTrap(this, "invalid argument " + value1.valueToString() 
								+ " or " + value2.valueToString() + " or " + value3.valueToString() );
		}
		return ExitCode_OK;
	}
}
