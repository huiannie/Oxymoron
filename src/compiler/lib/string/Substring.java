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
import compiler.data.DataType;
import compiler.lib.LibFunction;
import compiler.tokenizers.Token;
import compiler.util.BugTrap;

public class Substring extends LibFunction {

	// String substring(String value1, Integer value2, Integer value3)
	public static final String name = "substring";
	private static final String Param1 = "value1";
	private static final String Param2 = "value2";
	private static final String Param3 = "value3";
	public static final String parameters = compiler.data.String.StartKeyword + Token.space + Param1 + ","
											+ compiler.data.Integer.StartKeyword + Token.space + Param2 + "," 
											+ compiler.data.Integer.StartKeyword + Token.space + Param3;
	public static final String returnType = compiler.data.String.StartKeyword;

	
	public Substring() throws BugTrap {
		super(name, parameters, returnType);
	}

	@Override
	public int execute(Context context) throws BugTrap {
		DataType value1 = context.getVariable(Param1).getValue();
		DataType value2 = context.getVariable(Param2).getValue();
		DataType value3 = context.getVariable(Param3).getValue();
		
		java.lang.String x;
		
		if (value1 instanceof compiler.data.String && value2 instanceof compiler.data.Integer && value3 instanceof compiler.data.Integer) {
			int start = ((compiler.data.Integer) value2).getValue();
			int end = ((compiler.data.Integer) value3).getValue();
			x = ((compiler.data.String) value1).getValue();
			setReturnValue( new compiler.data.String(x.substring(start, end+1)) , context);
		}
		else {
			throw new BugTrap(this, "invalid argument " + value1.valueToString() 
								+ " or " + value2.valueToString() + " or " + value3.valueToString() );
		}
		return ExitCode_OK;
	}
}
