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
package compiler.expressionparser;

import compiler.util.IOUtils;

class ParsedSquareBracketList extends ParsedList {
	private static final long serialVersionUID = -6918614334584134042L;
	
	void print(int indent) {
		IOUtils.printIndented(indent, "Printing Square-bracketed list: " + getAllElementsAsString());
	}

	public String getAllElementsAsString() {
		String s = "";
		for (Object obj : this)
			s += getElementAsString(obj) + " ";
		return "[ " + s + "]";
	}

}
