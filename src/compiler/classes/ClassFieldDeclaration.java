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

import java.util.ArrayList;

import compiler.binder.Context;
import compiler.blocks.Block;
import compiler.data.DataType;
import compiler.declarations.VariableDeclaration;
import compiler.expression.Expression;
import compiler.expressionparser.ExpressionParser;
import compiler.literal.Literal;
import compiler.main.Settings;
import compiler.parser.Scope;
import compiler.tokenizers.LineMatcher;
import compiler.tokenizers.LineTokenizer;
import compiler.tokenizers.Token;
import compiler.util.BugTrap;

public class ClassFieldDeclaration extends VariableDeclaration {
	public static final boolean Debug = Settings.Debug;

	protected static final String AccessToken = ClassBlock.AccessToken;

	private static final String PrimitiveDataTypeName = Token.PrimitiveDataTypeName;
	private static final String ValidNameTag = Token.ValidNameTagPattern;
	
	private String access;
	
	public ClassFieldDeclaration(String access, String datatypeString, String name, ArrayList<Expression> initialValueExpressions) {
		super(datatypeString, name, initialValueExpressions);
		this.access = access;
	}
	
	
	public String getAccess() {
		return access;
	}
	
	/* A class field declaration may declare more than one field within a single line. 
	 * Therefore, return a list of class fields.
	 */
	
	public static boolean isAClassField(String line) {
		// Syntax: access DataType name
		if (LineMatcher.matchStart(line, AccessToken, PrimitiveDataTypeName, ValidNameTag)) return true;

		return false;
	}
	
	public static ArrayList<Literal> parse(String line, Scope scope, Block callerBlock) throws BugTrap {
		// Syntax 1: access PrimitiveDataType name1 [= value1] [, name2 [= value2]]... 
		// Syntax 2: access PrimitiveDataType name[] [= value1,value2,...]
		
		ArrayList<Literal> vars = new ArrayList<Literal>();
				
		LineTokenizer tokenizer = new LineTokenizer(line);
		
		if (!tokenizer.isAccessModifier()) throw new BugTrap (callerBlock, line, "Access modifier missing");
		String access = tokenizer.parseAccessModifier();
		
		
		if (!tokenizer.isPrimitiveDataType()) throw new BugTrap(callerBlock, line, "DataType missing");
		String datatypeString = tokenizer.parsePrimitiveDataType();

		// Match Syntax 2: access PrimitiveDataType name[] [= value1,value2,...]
		if (tokenizer.isArrayName()) {
			String name = tokenizer.parseArrayName();
			String valueString = null;
			ArrayList<Expression> exprs = null;
			
			if (tokenizer.isAssignSign()) {
				tokenizer.parseAssignSign();
				valueString = tokenizer.getRemainder();
				exprs = new ExpressionParser(valueString, scope.getStringsMap(), callerBlock).parseToList();
			}
			ClassFieldDeclaration var = new ClassFieldDeclaration(access, datatypeString, name, exprs);
			vars.add(var);
		}
		// Match Syntax 1: access PrimitiveDataType name1 [= value1] [, name2 [= value2]]... 
		else if (tokenizer.isName()) {
			String remainder = tokenizer.getRemainder();

			String args[] = remainder.split(",");
			for (String arg : args) {
				LineTokenizer tokenizer2 = new LineTokenizer(arg);
				
				String name = tokenizer2.parseName();
				String valueString = null;
				ArrayList<Expression> exprs = null;
				
				if (tokenizer2.isAssignSign()) {
					tokenizer2.parseAssignSign();
					valueString = tokenizer2.getRemainder();
					Expression expr = new ExpressionParser(valueString, scope.getStringsMap(), callerBlock).parse();
					exprs = new ArrayList<Expression>();
					exprs.add(expr);

				}
				ClassFieldDeclaration var = new ClassFieldDeclaration(access, datatypeString, name, exprs);
				vars.add(var);
			}			
		}
		else 
			throw new BugTrap(callerBlock, line, "name missing");
		
		
		return vars;
	}


	@Override
	public DataType evaluate(Context context) throws BugTrap {
		throw new BugTrap(context, "not supposed to run evaluate()");
	}
}
