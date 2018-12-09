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
import compiler.declarations.Declaration;
import compiler.declarations.VariableDeclaration;
import compiler.expression.Expression;
import compiler.expressionparser.ExpressionParser;
import compiler.literal.Literal;
import compiler.literal.UnbindedLiteral;
import compiler.literal.ValueType;
import compiler.main.Settings;
import compiler.parser.Scope;
import compiler.tokenizers.LineMatcher;
import compiler.tokenizers.LineTokenizer;
import compiler.tokenizers.Token;
import compiler.util.BugTrap;
import compiler.util.IOUtils;

public class ClassObjectDeclaration extends UnbindedLiteral {
	public static final boolean Debug = Settings.Debug;

	public static final String StartKeyword = "Declare";
	private static final String ValidNameTag = Token.ValidNameTagPattern;

	ClassType type;
	String modeString;
	String name;
	ArrayList<Expression> initialValueExpressions;
	
	public ClassObjectDeclaration(ClassType classType, String name, ArrayList<Expression> initialValueExpressions) {
		this.type = classType;
		this.name = name;
		this.modeString = null;
		this.initialValueExpressions = initialValueExpressions;
	}
	public ClassObjectDeclaration(ClassType classType, String modeString, String name, ArrayList<Expression> initialValueExpressions) {
		this.type = classType;
		this.name = name;
		this.modeString = modeString;
		this.initialValueExpressions = initialValueExpressions;
	}
	
	
	public ClassType getClasstype() {
		return type;
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
				+ " type: " + type.getType()
				+ " name: " + name
				+ " initial value: " + (initialValueExpressions==null?"uninitialized":initialValueExpressions)
				);
	}
	
	
	@Override
	public String valueToString() {
		return "unbinded " + type.getType() + " name=" + name + initialValueExpressions;
	}

	
	
	/* A class object declaration may declare one object within a single line. 
	 * Therefore, return the object.
	 */
	
	public static boolean isAClassObject(String line) {
		// Syntax: New className objectName(argumentList)
		
		// First make sure it is not a variable declaration (which is the keyword Declare followed by a primitive type.
		if (VariableDeclaration.isADeclare(line)) return false;

		// Then check if it is a plausible class object declaration.
		else if (LineMatcher.matchStart(line, StartKeyword, ValidNameTag, ValidNameTag)) return true;
		return false;
	}
	
	


	
	public static ArrayList<Literal> parse(String line, Scope scope, Block callerBlock) throws BugTrap {
		// For primitive data types and array of primitive data types
		// Syntax 1: Declare ClassType name1 [= value1] [, name2 [= value2]]... 
		// Syntax 2: Declare ClassType name[] [= value1,value2,...]
		
	
		ArrayList<Literal> vars = new ArrayList<Literal>();
		
		String classtypeString;
		
		LineTokenizer tokenizer = new LineTokenizer(line);
		
		if (!tokenizer.isKeyword(StartKeyword)) throw new BugTrap(callerBlock, line, StartKeyword + ": Keyword missing");
		tokenizer.parseKeyword(StartKeyword);

		if (!tokenizer.isClassType()) throw new BugTrap(callerBlock, line, "Missing class name");
		classtypeString = tokenizer.parseClassType();
		
		if (scope.getClassesMap()==null || !scope.getClassesMap().containsKey(classtypeString)) throw new BugTrap(callerBlock, line, "Class " + classtypeString + " is not defined.");
		ClassType classType = scope.getClassesMap().get(classtypeString).getType();
		

		
		{
			// Match Syntax 2: Declare ClassType name[] [= value1,value2,...]
			if (tokenizer.isArrayName()) {
				String name = tokenizer.parseArrayName();
				String valueString = null;
				ArrayList<Expression> exprs = null;
				
				if (tokenizer.isAssignSign()) {
					tokenizer.parseAssignSign();
					valueString = tokenizer.getRemainder();
					exprs = new ExpressionParser(valueString, scope.getStringsMap(), callerBlock).parseToList();
				}
				ClassObjectDeclaration var = new ClassObjectDeclaration(classType, name, exprs);
				vars.add(var);
			}
			// Match Syntax 1: Declare ClassType name1 [= value1] [, name2 [= value2]]... 
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
					ClassObjectDeclaration var = new ClassObjectDeclaration(classType, name, exprs);
					vars.add(var);
				}			
			}
			else 
				throw new BugTrap(callerBlock, line, "name missing");
		}

		return vars;
	}


	@Override
	public ValueType evaluate(Context context) throws BugTrap {
		// Objects must be binded to the parent of the Declaration block's context.
		Context objContext = context;
		if (context.getBlock() instanceof Declaration) {
			objContext = context.getParent();
		}
		
		ClassObject obj = new ClassObject(this, objContext);
		return obj.getValue();
	}


}
