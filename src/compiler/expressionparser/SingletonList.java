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

import compiler.blocks.Block;
import compiler.data.DataType;
import compiler.expression.Expression;
import compiler.expression.Operator;
import compiler.main.Settings;
import compiler.parser.StringsMap;
import compiler.util.BugTrap;
import compiler.util.IOUtils;

class SingletonList extends NestedList {
	private static final boolean Debug = Settings.Debug;	
	

	private String node = null;
	
	public SingletonList(String node) { this.node = node; }
	public String getNode() { return node; }
	public int size() { return 1; }
	
	public String flatten() {
		return node;
	}
	
	public Object interpret(StringsMap stringsMap, Block block) throws BugTrap {
		if (node==Comma) return node;
		if (node==Period) return node;

		if (node==RoundOpen) throw new BugTrap(block, "Bad token " + node);
		if (node==RoundClose) throw new BugTrap(block, "Bad token " + node);
		if (node==SquareOpen) throw new BugTrap(block, "Bad token " + node);
		if (node==SquareClose) throw new BugTrap(block, "Bad token " + node);


		// Check the type of node. If it is an operator, then it cannot be an expression
		for (Operator op : operators) {
			if (node==op.getKeyword()) {
				return op;
			}
		}
		
		// 1. Check if the string is in the stringsMap. If yes, it is a string
		// 2. Check if the string is not in the stringsMap, it might be:
		//	a variable, an array variable, a function call, a method call.
		if (stringsMap.containsKey(node)) {
			if (Debug) IOUtils.println(SingletonList.class.getName()+": "
										+ node + " is found in StringsMap as: " 
										+ stringsMap.get(node));
			
			DataType string = DataType.parse(compiler.data.String.StartKeyword, stringsMap.get(node));
			if (string==null) throw new BugTrap(block, node + "is bad data ");
			
			return new Expression(string);
		}
				
		else {
			// Try to convert the node value to a DataType.
			// Acceptable DataTypes are the primitive data types and the file data types.
			// The DataType cannot be a string because all strings must be mapped from the stringsMap.

			DataType value = DataType.parseValue(node);
			if (value==null) throw new BugTrap(block, node + "is bad data.");

			if (!compiler.data.String.matchesType(value.getType())) {
				if (Debug) IOUtils.println(SingletonList.class.getName()+": " 
										+ node + " is found as data: " 
										+ value.valueToString());
				return new Expression(value);
			}
			else {
				if (Debug) IOUtils.println("Found wildcard " + node);
				
				// If DataType is string, then it must be misclassified.
				return new Wildcard(node);
			}
		}
	}
}
