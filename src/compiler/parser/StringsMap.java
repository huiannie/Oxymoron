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

import compiler.util.IOUtils;

public class StringsMap {
	HashMap<String, String> map = new HashMap<String, String>();
	
	public StringsMap() {
		
	}
	
	
	public void print(int indent) {
		IOUtils.printIndented(indent, "StringsMap entries:");
		for (String key : map.keySet()) {
			IOUtils.printIndented(indent+1, key + " -> " + map.get(key));
		}
		IOUtils.printIndented(indent, "End of StringsMap.");
	}


	public boolean containsKey(String key) {
		return map.containsKey(key);
	}


	public String put(String key, String value) {
		return map.put(key, value);
	}


	public String get(String key) {
		return map.get(key);
	}
}
