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
package compiler.parser;

import java.util.HashMap;
import java.util.Set;

import compiler.blocks.Method;
import compiler.lib.Library;
import compiler.main.Settings;
import compiler.util.IOUtils;

public class MethodsMap {
	public static final boolean Debug = Settings.Debug;

	HashMap<String, Method> map = new HashMap<String, Method>();
	
	public MethodsMap() {
		
	}
	
	
	public void print(int indent) {
		IOUtils.printIndented(indent, "MethodsMap entries:");
		for (String key : map.keySet()) {
			IOUtils.printIndented(indent+1, key + " -> ");
			map.get(key).print(indent+2);
		}
		IOUtils.printIndented(indent, "End of MethodsMap.");
	}


	public boolean containsKey(String key) {
		return map.containsKey(key);
	}


	public Method put(String key, Method method) {
		return map.put(key, method);
	}


	public Method get(String key) {
		return map.get(key);
	}
	
	public Set<String> keyset() {
		return map.keySet();
	}
	
	public void load(Library library) {
		for (String name : library.methodsMap.keyset()) {
			this.map.put(name, library.methodsMap.get(name));
		}

	}


	public boolean isEmpty() {
		return map.size()==0;
	}
}
