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

public class Insert extends LibModule {

	// void insert(String value1, Integer value2, String value3)
	public static final String name = "insert";
	private static final String Param1 = "value1";
	private static final String Param2 = "value2";
	private static final String Param3 = "value3";
	public static final String parameters = compiler.data.String.StartKeyword + Token.space + VariableRef.Keyword + Token.space + Param1 + ","
											+ compiler.data.Integer.StartKeyword + Token.space + Param2 + "," 
											+ compiler.data.String.StartKeyword + Token.space + Param3;

	
	public Insert() throws BugTrap {
		super(name, parameters);
	}

	@Override
	public int execute(Context context) throws BugTrap {
		DataType value1 = context.getVariable(Param1).getValue();
		DataType value2 = context.getVariable(Param2).getValue();
		DataType value3 = context.getVariable(Param3).getValue();
		
		java.lang.String x;
		java.lang.String y;
		
		if (value1 instanceof compiler.data.String && value2 instanceof compiler.data.Integer && value3 instanceof compiler.data.String) {
			int start = ((compiler.data.Integer) value2).getValue();

			x = ((compiler.data.String) value1).getValue();
			y = ((compiler.data.String) value3).getValue();
			
			if (start<0 || start>x.length()-1) throw new BugTrap(this, "index " + start + " exceeds string length.");
			
			compiler.data.String newValue = new compiler.data.String( x.substring(0, start) + y + x.substring(start) );
			
			Variable var = context.getVariable(Param1);
			var.setValue(newValue);
		}
		else {
			throw new BugTrap(this, "invalid argument " + value1.valueToString() 
								+ " or " + value2.valueToString() + " or " + value3.valueToString() );
		}
		return ExitCode_OK;
	}
}
