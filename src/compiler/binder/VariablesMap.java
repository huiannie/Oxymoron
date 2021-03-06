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
package compiler.binder;

import java.util.HashMap;
import java.util.Set;

import compiler.main.Settings;
import compiler.util.IOUtils;

public class VariablesMap {
	public static final boolean Debug = Settings.Debug;

	HashMap<String, Variable> map = new HashMap<String, Variable>();
	
	public VariablesMap() {
		
	}
	
	
	public void print(int indent) {
		IOUtils.printIndented(indent, this.getClass().getSimpleName() + " entries:");
		for (String key : map.keySet()) {
			IOUtils.printIndented(indent+1, key + " -> ");
			map.get(key).print(indent+2);
		}
		IOUtils.printIndented(indent, "End of " + this.getClass().getSimpleName());
	}


	public boolean containsKey(String key) {
		return map.containsKey(key);
	}


	public Variable put(String key, Variable variable) {
		return map.put(key, variable);
	}


	public Variable get(String key) {
		return map.get(key);
	}
	
	public Set<String> keyset() {
		return map.keySet();
	}
	
}
