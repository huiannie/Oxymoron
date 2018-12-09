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

import compiler.binder.Constant;
import compiler.binder.Context;
import compiler.blocks.Block;
import compiler.data.DataType;
import compiler.expression.Expression;
import compiler.expressionparser.ExpressionParser;
import compiler.literal.UnbindedLiteral;
import compiler.main.Settings;
import compiler.parser.Scope;
import compiler.tokenizers.LineMatcher;
import compiler.tokenizers.LineTokenizer;
import compiler.util.BugTrap;
import compiler.util.IOUtils;

public class ConstantDeclaration extends UnbindedLiteral {
	public static final boolean Debug = Settings.Debug;

	public static final String StartKeyword = "Constant";

	String datatypeString;
	String name;
	Expression valueExpression;
	
	public ConstantDeclaration(String datatypeString, String name, Expression valueExpression) {
		this.datatypeString = datatypeString;
		this.name = name;
		this.valueExpression = valueExpression;
	}
	
	
	public String getDatatypeString() {
		return datatypeString;
	}
	
	public String getName() {
		return name;
	}
	
	public Expression getValue() {
		return valueExpression;
	}

	
	public void print(int indent) {
		IOUtils.printIndented(indent, this.getClass().getSimpleName() 
				+ " type: " + datatypeString
				+ " name: " + name
				+ " value: " + valueExpression
				);
	}


	public static boolean isAConstant(String line) {
		if (LineMatcher.matchStart(line, StartKeyword)) return true;
		else return false;
	}
	

	

	
	public static ConstantDeclaration parse(String line, Scope scope, Block callerBlock) throws BugTrap {
		// Syntax: Constant DataType name = value
		ConstantDeclaration constantDeclaration = null;
		String datatypeString;
		String name;
		String valueString;
		
		LineTokenizer tokenizer = new LineTokenizer(line);
		
		if (!tokenizer.isKeyword(StartKeyword)) throw new BugTrap(callerBlock, line, StartKeyword + ": Keyword missing");
		tokenizer.parseKeyword(StartKeyword);

		if (!tokenizer.isPrimitiveDataType()) throw new BugTrap(callerBlock, line, "DataType missing");
		datatypeString = tokenizer.parsePrimitiveDataType();

		if (!tokenizer.isName()) throw new BugTrap(callerBlock, line, "name missing");
		name = tokenizer.parseName();
		
		if (!tokenizer.isAssignSign()) throw new BugTrap(callerBlock, line, "Missing = sign.");
		tokenizer.parseAssignSign();
				
		if (tokenizer.isName())
			valueString = tokenizer.parseName();
		else 
			valueString = tokenizer.getRemainder();

		Expression expr = new ExpressionParser(valueString, scope.getStringsMap(), callerBlock).parse();
		constantDeclaration = new ConstantDeclaration(datatypeString, name, expr);
		return constantDeclaration;
	}
	
	
	@Override
	public String valueToString() {
		return "unbinded " + datatypeString + " name=" + name + valueExpression;
	}


	
	
	@Override
	public DataType evaluate(Context context) throws BugTrap {
		// Constants must be binded to the parent context.
		Context constContext = context;
		if (context.getBlock() instanceof Declaration) {
			constContext = context.getParent();
		}

		Constant constant = new Constant(this, constContext);
		return constant.getValue();
	}

	
}
