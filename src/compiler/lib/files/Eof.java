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
package compiler.lib.files;

import compiler.binder.Context;
import compiler.data.DataType;
import compiler.files.InputFile;
import compiler.lib.LibFunction;
import compiler.main.Settings;
import compiler.tokenizers.Token;
import compiler.util.BugTrap;
import compiler.util.IOUtils;

public class Eof extends LibFunction {
	public static final boolean Debug = Settings.Debug;

	// Real eof(InputFile value)
	public static final String name = "eof";
	private static final String Param = "value";
	public static final String parameters = InputFile.StartKeyword + Token.space + Param;
	public static final String returnType = compiler.data.Boolean.StartKeyword;

	
	public Eof() throws BugTrap {
		super(name, parameters, returnType);
	}

	@Override
	public int execute(Context context) throws BugTrap {
		DataType value = context.getVariable(Param).getValue();
		
		if (Debug) { IOUtils.println(getClass().getSimpleName() + " checking eof(" + value.getType() + ")"); }
		
		if (value instanceof InputFile) {
			if (((InputFile) value).hasNext()) {
				setReturnValue(new compiler.data.Boolean(false), context );
			}
			else {
				setReturnValue(new compiler.data.Boolean(true), context );
			}
		}
		else {
			throw new BugTrap(this, "invalid argument " + value.valueToString());
		}
		return ExitCode_OK;
	}

}
