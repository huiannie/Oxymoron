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
package compiler.classes;

import java.util.HashMap;
import java.util.Set;

import compiler.blocks.Method;
import compiler.main.Settings;
import compiler.util.IOUtils;

public class ConstructorsMap {
	public static final boolean Debug = Settings.Debug;

	HashMap<Constructor, Method> map = new HashMap<Constructor, Method>();
	
	public ConstructorsMap() {
		
	}
	
	
	public void print(int indent) {
		IOUtils.printIndented(indent, getClass().getSimpleName() + " entries:");
		for (Constructor key : map.keySet()) {
			IOUtils.printIndented(indent+1, key + " -> ");
			map.get(key).print(indent+2);
		}
		IOUtils.printIndented(indent, "End of " + getClass().getSimpleName());
	}


	public boolean containsKey(Constructor key) {
		return map.containsKey(key);
	}


	public Method put(Constructor key, Method method) {
		return map.put(key, method);
	}


	public Method get(Constructor key) {
		return map.get(key);
	}
	
	public Set<Constructor> keyset() {
		return map.keySet();
	}

	public boolean isEmpty() {
		return map.size()==0;
	}
	
}
