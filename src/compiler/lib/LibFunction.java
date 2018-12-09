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
package compiler.lib;

import compiler.blocks.Function;
import compiler.tokenizers.Token;
import compiler.util.BugTrap;
import compiler.util.IOUtils;

public abstract class LibFunction extends Function {
	public static final int StandardLibrary = -1;

	public LibFunction(String name, String parameters, String returnType) throws BugTrap {
		super(StandardLibrary, StandardLibrary);
		
		String header = 
						 Function.StartKeyword + Token.space 
						+ returnType + Token.space + name + Token.space
						+ Token.RoundOpen + parameters + Token.RoundClose;
		if (Debug) IOUtils.println("Creating library function header=" + header);

		parseHeader(header);
	}

}
