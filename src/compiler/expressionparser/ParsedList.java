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

import java.util.ArrayList;

import compiler.expression.Expression;
import compiler.expression.Operator;
import compiler.util.IOUtils;

public class ParsedList extends ArrayList<Object> {
	private static final long serialVersionUID = -6918614334584134042L;
	
	
	void print(int indent) {
		IOUtils.printIndented(indent, "Printing list: " + getAllElementsAsString());
	}
	
	
	public String getAllElementsAsString() {
		String s = "";
		for (Object obj : this)
			s += getElementAsString(obj) + " ";
		return s;
	}

	public String getElementAsString(Object obj) {
		if (obj instanceof String) {
			return "S:"+(String) obj;
		}
		else if (obj instanceof Operator) {
			return "Op:"+((Operator)obj).getKeyword();
		}
		else if (obj instanceof Expression) {
			return "E:"+((Expression)obj).toStringInBracket();
		}
		else if (obj instanceof Wildcard) {
			return "??:"+((Wildcard)obj).getName();
		}
		else if (obj instanceof ParsedList) {
			return ((ParsedList) obj).getAllElementsAsString();
		}
		else {
			return "Obj";
			
		}		
	}
}
