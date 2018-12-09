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
package compiler.loops;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import compiler.binder.Context;
import compiler.binder.Variable;
import compiler.blocks.Block;
import compiler.data.DataType;
import compiler.expression.Expression;
import compiler.expressionparser.ExpressionParser;
import compiler.main.Settings;
import compiler.parser.Scope;
import compiler.tokenizers.LineMatcher;
import compiler.tokenizers.LineTokenizer;
import compiler.util.BugTrap;
import compiler.util.IOUtils;

public class For extends Block {
	public static final boolean Debug = Settings.Debug;

	public static final String StartKeyword = "For";
	public static final String EndKeyword = "End For";
	public static final String ToKeyword = "To";
	public static final String StepKeyword = "Step";
	
	String counterName;
	Expression fromExpression;
	Expression toExpression;
	Expression stepExpression;
	
	public For(int startLineNumber, int endLineNumber) {
		super(startLineNumber, endLineNumber);
		hasHeaderAndFooter = true;
	}
	
	public static boolean startsWithAKeyword(String line) {
		if (LineMatcher.matchStart(line, EndKeyword)) return true;
		else if (LineMatcher.matchStart(line, StartKeyword)) return true;
		// Note: The keywords To and Step should not occur at the start of a line.
		return false;
	}
	

	
	public static boolean isAStart(String line) {
		String conditionToken = ".+";
		return (LineMatcher.matchStart(line, StartKeyword, conditionToken));
	}

	public static boolean isAnEnd(String line) {
		return (LineMatcher.matchExact(line, EndKeyword));
	}
	
	
	
	public void parse(ArrayList<String> code, Scope scope, Block parent) throws BugTrap {
		if (this.parent==null) this.parent = parent;

		
		int lineNumber = getStartLineNumber();
		String line1 = code.get(lineNumber);

		// Syntax: For variable = expression1 To expression2 Step expression3

		LineTokenizer tokenizer = new LineTokenizer(line1);
		
		if (!tokenizer.isKeyword(StartKeyword)) throw new BugTrap(this, StartKeyword + ": Keyword missing");
		tokenizer.parseKeyword(StartKeyword);

		if (!tokenizer.isName()) throw new BugTrap(this, StartKeyword + ": counter missing");
		counterName = tokenizer.parseName();
		
		if (!tokenizer.isAssignSign()) throw new BugTrap(this, StartKeyword + ": = missing");
		tokenizer.parseAssignSign();

		String remainder = tokenizer.getRemainder();

		// After the = sign, there should be 3 or 5 fields
		
		{	
			// Since the expressions can be very diverse in form, 
			// cannot parse them from left to right using LineTokenizer.
			// We need a customized parser for our case.

			String arg1, arg2, arg3;

			// First, find the "To" keyword.
			Matcher matcher = Pattern.compile("\\b" + ToKeyword + "\\b").matcher(remainder);
			if (!matcher.find()) throw new BugTrap(this, StartKeyword + ": missing " + ToKeyword);

			arg1 = remainder.substring(0, matcher.start()).trim();

			// Next, check if there is a "Step" keyword.
			// If yes, then we have two more expressions, one before and one after the keyword.
			// If no, then we only have one expression.
			remainder = remainder.substring(matcher.end()).trim();

			
			
			Matcher matcher2 = Pattern.compile("\\b" + StepKeyword + "\\b").matcher(remainder);
			if (matcher2.find()) {
				arg2 = remainder.substring(0, matcher2.start()).trim();
				arg3 = remainder.substring(matcher2.end()).trim();
			}
			else {
				arg2 = remainder;
				arg3 = "1";  // default is to set step size to 1.
			}
			if (Debug) {
				IOUtils.println("arg1=" + arg1 + " arg2=" + arg2 + " arg3=" + arg3);
			}
			
			fromExpression = new ExpressionParser(arg1, scope.getStringsMap(), parent).parse();
			toExpression = new ExpressionParser(arg2, scope.getStringsMap(), parent).parse();
			stepExpression = new ExpressionParser(arg3, scope.getStringsMap(), parent).parse();
			
		}		
		
		super.parse(code, scope, parent);
	}
	
	
	
	int toInteger(DataType value) throws BugTrap {
		float x;
		if (value instanceof compiler.data.Real) {
			x = ((compiler.data.Real) value).getValue();
			int r = (int) Math.floor(x);
			return r;
		}
		else if (value instanceof compiler.data.Integer) {
			x = ((compiler.data.Integer) value).getValue();
			int r = (int) Math.floor(x);
			return r;
		}
		else {
			throw new BugTrap(this, "Invalid value " + value.valueToString());
		}
	}
	
	float toFloat(DataType value) throws BugTrap {
		float x;

		if (value instanceof compiler.data.Real) {
			x = ((compiler.data.Real) value).getValue();
			return x;
		}
		else if (value instanceof compiler.data.Integer) {
			x = ((compiler.data.Integer) value).getValue();
			return x;
		}
		else {
			throw new BugTrap(this, "Invalid value " + value.valueToString());
		}
	}
	
	
	@Override
	public int execute(Context context) throws BugTrap {
		int exitCode;

		Variable counter = context.getVariable(counterName);
		counter.setValue((DataType) fromExpression.evaluate(context));

		DataType start = (DataType) fromExpression.evaluate(context);
		DataType end = (DataType) toExpression.evaluate(context);
		DataType step = (DataType) stepExpression.evaluate(context);

		if (compiler.data.Integer.matchesType(counter.getType())) {
			int startValue = toInteger(start);
			int current = toInteger(counter.evaluate(context));
			int endValue = toInteger(end);
			int update = toInteger(step);

			boolean done = !((startValue<=current && current<=endValue && update>0) || (endValue<=current && current<=startValue && update<0));
			while (!done) {
				// If the loop is given its own subcontext, then it is allowed to declare local variables.
				Context subContext = new Context(this, context);
				
				exitCode = super.execute(subContext);
				if (exitCode==ExitCode_Return) return exitCode;
				
				current = current + update;
				counter.setValue(new compiler.data.Integer(current));
				done = !((startValue<=current && current<=endValue) || (endValue<=current && current<=startValue));
			}
			return ExitCode_OK;
		}
		else if (compiler.data.Real.matchesType(counter.getType())) {
			float startValue = toFloat(start);
			float current = toFloat(counter.evaluate(context));
			float endValue = toFloat(end);
			float update = toFloat(step);

			boolean done = !((startValue<=current && current<=endValue && update>0) || (endValue<=current && current<=startValue && update<0));
			while (!done) {
				// If the loop is given its own subcontext, then it is allowed to declare local variables.
				Context subContext = new Context(this, context);

				exitCode = super.execute(subContext);
				if (exitCode==ExitCode_Return) return exitCode;

				current = current + update;
				counter.setValue(new compiler.data.Real(current));
				done = !((startValue<=current && current<=endValue) || (endValue<=current && current<=startValue));
			}
			return ExitCode_OK;
		}
		throw new BugTrap(this, "Bad data type for counter.");
	}

}
