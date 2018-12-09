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
package compiler.declarations;

import java.util.ArrayList;

import compiler.binder.Context;
import compiler.binder.Variable;
import compiler.blocks.Block;
import compiler.data.DataType;
import compiler.expression.Expression;
import compiler.expressionparser.ExpressionParser;
import compiler.literal.Literal;
import compiler.literal.UnbindedLiteral;
import compiler.main.Settings;
import compiler.parser.Scope;
import compiler.tokenizers.LineMatcher;
import compiler.tokenizers.LineTokenizer;
import compiler.tokenizers.Token;
import compiler.util.BugTrap;
import compiler.util.IOUtils;

public class VariableDeclaration extends UnbindedLiteral {
	public static final boolean Debug = Settings.Debug;

	public static final String StartKeyword = "Declare";

	String datatypeString;
	String modeString;
	String name;
	ArrayList<Expression> initialValueExpressions;
	
	public VariableDeclaration(String datatypeString, String name, ArrayList<Expression> initialValueExpressions) {
		this.datatypeString = datatypeString;
		this.name = name;
		this.modeString = null;
		this.initialValueExpressions = initialValueExpressions;
	}
	public VariableDeclaration(String datatypeString, String modeString, String name, ArrayList<Expression> initialValueExpressions) {
		this.datatypeString = datatypeString;
		this.name = name;
		this.modeString = modeString;
		this.initialValueExpressions = initialValueExpressions;
	}
	
	
	public String getDatatypeString() {
		return datatypeString;
	}
	
	public String getName() {
		return name;
	}
	
	public ArrayList<Expression> getInitialValues() {
		return initialValueExpressions;
	}
	
	public boolean isInitialized() {
		return initialValueExpressions!=null;
	}
	
	public void print(int indent) {
		IOUtils.printIndented(indent, this.getClass().getSimpleName() 
				+ " type: " + datatypeString
				+ " name: " + name
				+ " initial value: " + (initialValueExpressions==null?"uninitialized":initialValueExpressions)
				);
	}
	
	
	@Override
	public String valueToString() {
		return "unbinded " + datatypeString + " name=" + name + initialValueExpressions;
	}

	
	
	/* A variable declaration may declare more than one variable within a single line. 
	 * Therefore, return a list of variables.
	 */

	public static boolean isADeclare(String line) {
		if (LineMatcher.matchStart(line, StartKeyword, Token.PrimitiveDataTypeName)) return true;
		else if (LineMatcher.matchStart(line, StartKeyword, Token.FileDataTypeName)) return true;
		else return false;
	}
	
	public String getMode() {
		return modeString;
	}

	
	public static ArrayList<Literal> parse(String line, Scope scope, Block callerBlock) throws BugTrap {
		// For primitive data types and array of primitive data types
		// Syntax 1: Declare PrimitiveDataType name1 [= value1] [, name2 [= value2]]... 
		// Syntax 2: Declare PrimitiveDataType name[] [= value1,value2,...]
		
		// For file data types and array of file data types. Initialization not permitted.
		// Syntax 3: Declare FileDataType [mode] name
		// Syntax 4: Declare FileDataType [mode] name[]
	
		ArrayList<Literal> vars = new ArrayList<Literal>();
		
		String datatypeString;
		
		LineTokenizer tokenizer = new LineTokenizer(line);
		
		if (!tokenizer.isKeyword(StartKeyword)) throw new BugTrap(callerBlock, line, StartKeyword + ": Keyword missing");
		tokenizer.parseKeyword(StartKeyword);


		
		if (tokenizer.isPrimitiveDataType()) {
			datatypeString = tokenizer.parsePrimitiveDataType();
			
			// Match Syntax 2: Declare PrimitiveDataType name[] [= value1,value2,...]
			if (tokenizer.isArrayName()) {
				String name = tokenizer.parseArrayName();
				String valueString = null;
				ArrayList<Expression> exprs = null;
				
				if (tokenizer.isAssignSign()) {
					tokenizer.parseAssignSign();
					valueString = tokenizer.getRemainder();
					exprs = new ExpressionParser(valueString, scope.getStringsMap(), callerBlock).parseToList();
				}
				VariableDeclaration var = new VariableDeclaration(datatypeString, name, exprs);
				vars.add(var);
			}
			// Match Syntax 1: Declare PrimitiveDataType name1 [= value1] [, name2 [= value2]]... 
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
					VariableDeclaration var = new VariableDeclaration(datatypeString, name, exprs);
					vars.add(var);
				}			
			}
			else 
				throw new BugTrap(callerBlock, line, "name missing");
		}
		else if (tokenizer.isFileDataType()) {
			datatypeString = tokenizer.parseFileDataType();
			
			// Check for the optional mode
			String modeString = null;
			if (tokenizer.isFileMode()) {
				modeString = tokenizer.parseFileMode();
			}
			
			// Match Syntax 4: Declare FileDataType [mode] name[]
			if (tokenizer.isArrayName()) {
				String name = tokenizer.parseArrayName();
				ArrayList<Expression> exprs = null;
				
				if (tokenizer.isAssignSign()) throw new BugTrap(callerBlock, line, "Variable initializaton for file data type not permitted.");
				VariableDeclaration var = new VariableDeclaration(datatypeString, modeString, name, exprs);
				vars.add(var);
			}
			// Match Syntax 3: Declare FileDataType [mode] name
			else if (tokenizer.isName()) {
				String remainder = tokenizer.getRemainder();

				String args[] = remainder.split(",");
				for (String arg : args) {
					LineTokenizer tokenizer2 = new LineTokenizer(arg);
					
					String name = tokenizer2.parseName();
					ArrayList<Expression> exprs = null;

					if (tokenizer2.isAssignSign()) throw new BugTrap(callerBlock, line, "Variable initializaton for file data type not permitted.");
					VariableDeclaration var = new VariableDeclaration(datatypeString, modeString, name, exprs);
					vars.add(var);
				}			
			}
			else 
				throw new BugTrap(callerBlock, line, "name missing");
		}
		else {
			throw new BugTrap(callerBlock, line, "DataType missing");
		}
		return vars;
	}


	@Override
	public DataType evaluate(Context context) throws BugTrap {
		// Variables must be binded to the parent of the Declaration block's context.
		Context varContext = context;
		if (context.getBlock() instanceof Declaration) {
			varContext = context.getParent();
		}
		Variable variable = new Variable(this, varContext);
		return variable.getValue();
	}
}
