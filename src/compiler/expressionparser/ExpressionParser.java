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
import java.util.Queue;

import compiler.blocks.Block;
import compiler.expression.Expression;
import compiler.expression.Operator;
import compiler.main.Settings;
import compiler.parser.StringsMap;
import compiler.tokenizers.Token;
import compiler.util.BugTrap;
import compiler.util.IOUtils;

// precedence:
// 1. ^
// 2. * and / and modulo
// 3. + and -
// 4. NOT
// 5. AND
// 6. OR

public class ExpressionParser {
	private static final boolean Debug = Settings.Debug;	

	public static final String RoundOpen = Token.RoundOpen;
	public static final String RoundClose = Token.RoundClose;
	public static final String Comma = Token.Comma;
	
	public static final String keywords[] = Expression.keywords;
	public static final Operator operators[] = Expression.operators;
	
	
	String expressionString;
	
	StringsMap stringsMap;
	Block block;
	public ExpressionParser(String expressionString, StringsMap stringsMap, Block block) {
		if (Debug) IOUtils.println(ExpressionParser.class.getSimpleName()+": parsing " + expressionString);
		this.expressionString = expressionString;
		this.stringsMap = stringsMap;
		this.block = block;
	}


	
	
	public Expression parse() throws BugTrap {
		Queue<String> tokensList = ExpressionTokenizer.tokenize(expressionString);
		
		NestedList nestedList = NestedList.parse(tokensList);
		if (Debug) IOUtils.println("Expression string converted to nested list " + nestedList.flatten());

		return nestedList.toExpression(stringsMap, block);
	}
	
	public ArrayList<Expression> parseToList() throws BugTrap {
		Queue<String> tokensList = ExpressionTokenizer.tokenize(expressionString);
		
		NestedList nestedList = NestedList.parse(tokensList);
		if (Debug) IOUtils.println("Expression string converted to nested list " + nestedList.flatten());

		return nestedList.toExpressionList(stringsMap, block);
	}

	
	
	public static void main(String args[]) throws BugTrap {
		String expressions[] = {"__A + X",
								"F(Y, __B, Z)",
								"G(Y, __B, Z) + __C",
								"G(F(__A, X, Y), __B, Z) - __C * H(Y, __B, Z) + __A MOD X",

								};

		// Set up some dummy values
		Block block = new Block(0, 0);
		StringsMap map = new StringsMap();
		map.put("__A", "ItsMe");
		map.put("__B", "ItsYou");
		map.put("__C", "ItsHim");
		map.put("__D", "ItsHer");

		for (String e : expressions) {
			IOUtils.println("\n\nTesting with " + e);
			ExpressionParser p = new ExpressionParser(e, map, block);
			Expression exp = p.parse();
			IOUtils.println("Output for " + e + " is: \n" + exp.toStringInBracket() + "\n");
		}
	}
}
