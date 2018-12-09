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

import compiler.blocks.Method;
import compiler.blocks.Program;
import compiler.parser.MethodsMap;

public class LibrariesBuilder {
	public LibrariesBuilder() {
		
	}
	
	// Create a brand new map
	public static MethodsMap build() {
		MethodsMap libraries = new MethodsMap();
		libraries.load(LibMath.library);
		libraries.load(LibString.library);
		libraries.load(LibFiles.library);
		libraries.load(LibCharacter.library);
		return libraries;
	}
	
	// Load into an existing map
	public static void load(MethodsMap existing, Program program) {
		MethodsMap libraries = build();
		for (String methodName : libraries.keyset()) {
			Method method = libraries.get(methodName);
			method.parent = program;
			existing.put(methodName, method);
		}
		
	}
}
