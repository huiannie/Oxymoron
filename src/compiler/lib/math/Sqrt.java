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
package compiler.lib.math;

import compiler.binder.Context;
import compiler.data.DataType;
import compiler.lib.LibFunction;
import compiler.tokenizers.Token;
import compiler.util.BugTrap;

public class Sqrt extends LibFunction {

	// Integer random(Integer startValue, Integer endValue)
	public static final String name = "sqrt";
	private static final String Param = "value";
	public static final String parameters = compiler.data.Real.StartKeyword + Token.space + Param;
	public static final String returnType = compiler.data.Real.StartKeyword;

	
	public Sqrt() throws BugTrap {
		super(name, parameters, returnType);
	}

	@Override
	public int execute(Context context) throws BugTrap {
		DataType value = context.getVariable(Param).getValue();
		
		float x;
		if (value instanceof compiler.data.Real) {
			x = ((compiler.data.Real) value).getValue();
			float r = (float) Math.sqrt(x);
			setReturnValue( new compiler.data.Real(r) , context);
		}
		else if (value instanceof compiler.data.Integer) {
			x = ((compiler.data.Integer) value).getValue();
			float r = (float) Math.sqrt(x);
			setReturnValue( new compiler.data.Real(r) , context);
		}
		else {
			throw new BugTrap(this, "invalid argument " + value.valueToString());
		}
		return ExitCode_OK;
	}
}
