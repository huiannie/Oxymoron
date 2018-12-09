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
package compiler.conditionals;

import java.util.ArrayList;

import compiler.binder.Context;
import compiler.blocks.Block;
import compiler.data.DataType;
import compiler.expression.Expression;
import compiler.expressionparser.ExpressionParser;
import compiler.main.Settings;
import compiler.parser.Scope;
import compiler.tokenizers.LineMatcher;
import compiler.tokenizers.LineTokenizer;
import compiler.tokenizers.Token;
import compiler.util.BugTrap;
import compiler.util.IOUtils;

public class If extends Block {
	public static final boolean Debug = Settings.Debug;
	
	public static final String StartKeyword = "If";
	public static final String ThenKeyword = "Then";
	public static final String ElseKeyword = "Else";
	public static final String ElseIfKeyword = "Else If";
	public static final String EndKeyword = "End If";
	
	
	// Since we don't know how many else-ifs, the number of conditions is variable
	ArrayList<String> keywords = new ArrayList<String>();	
	ArrayList<Expression> conditions = new ArrayList<Expression>();

	

	public If(int startLineNumber, int endLineNumber) {
		super(startLineNumber, endLineNumber);
		hasHeaderAndFooter = true;
	}


	public static boolean startsWithAKeyword(String line) {
		if (LineMatcher.matchStart(line, ElseIfKeyword)) return true;
		else if (LineMatcher.matchStart(line, ElseKeyword)) return true;
		else if (LineMatcher.matchStart(line, EndKeyword)) return true;
		else if (LineMatcher.matchStart(line, StartKeyword)) return true;
		// Note: The keyword Then should not appear at the start of a line.
		return false;
	}

	
	
	public static boolean isAStart(String line) {
		String conditionToken = Token.OneCharacterOrMore;
		return (LineMatcher.matchStart(line, StartKeyword, conditionToken));
	}

	public static boolean isAnEnd(String line) {
		return (LineMatcher.matchExact(line, EndKeyword));
	}
	
	
	
	
	public void addIfValue(String keyword, Expression expression) {
		keywords.add(keyword);
		conditions.add(expression);
	}
	
	public void print(int indent) {
		
		IOUtils.printIndented(indent, "If-statement: range " + startLineNumber + "-" + endLineNumber);
		
		for (int index=0; index<keywords.size(); index++) {
			IOUtils.printIndented(indent, keywords.get(index) + " block");
			if (conditions.get(index)!=null) {
				IOUtils.printIndented(indent+2, "Condition: " + conditions.get(index).toStringInBracket());
			}
			if (index<subblocks.size())
				subblocks.get(index).print(indent+2);
		}
	}
	

	
	public void parse(ArrayList<String> code, Scope scope, Block parent) throws BugTrap {
		if (this.parent==null) this.parent = parent;
		
		int lineNumber = getStartLineNumber();
		String line1 = code.get(lineNumber);

		// Syntax: If expression Then

		LineTokenizer tokenizer = new LineTokenizer(line1);
		
		if (!tokenizer.isKeyword(StartKeyword)) throw new BugTrap(this, StartKeyword + ": Keyword missing");
		tokenizer.parseKeyword(StartKeyword);

		
		// Drop the keyword "Then" at the end of the string.
		String remainder = tokenizer.getRemainder();
		if (remainder.lastIndexOf(ThenKeyword)<0) throw new BugTrap(this, ThenKeyword + ": Keyword missing");
		remainder = remainder.substring(0, remainder.lastIndexOf(ThenKeyword)).trim();
		
		// Use the Expression parser to convert the remainder to an expression.
		Expression condition1 = new ExpressionParser(remainder, scope.getStringsMap(), parent).parse();
		addIfValue(StartKeyword, condition1);
		

		
		// Start parsing content of block. Since blocks can be nested, 
		// we can't know the end of a block until we find the next keyword, which could be Else-If, Else or End If.
		// So the block that is added is always closed (not started) with the keyword detected.
		
		int index = getStartLineNumber() + 1;
		
		int startSubBlockLineNumber = index;
		
		while (index<getEndLineNumber()) { 
			Block subblock = scope.findBlockAtLine(index);
			
			if (subblock==null) {

				String line = code.get(index);

				// Check if the current line is End If
				if (LineMatcher.matchExact(line, EndKeyword)) {
					addIfValue(EndKeyword, null);
					add(new Block(startSubBlockLineNumber, index));  // this block ends with the given keyword
					startSubBlockLineNumber = index+1;
				}
				
				// Check if current line is Else
				else if (LineMatcher.matchExact(line, ElseKeyword)) {
					addIfValue(ElseKeyword, null);
					add(new Block(startSubBlockLineNumber, index));  // this block ends with the given keyword
					startSubBlockLineNumber = index+1;
				}

				// Check if current line is Else If, in which case, recur.
				else if (LineMatcher.matchStart(line, ElseIfKeyword)) {
					add(new Block(startSubBlockLineNumber, index));  // this block ends with the given keyword
					startSubBlockLineNumber = index+1;
					
					LineTokenizer tokenizer2 = new LineTokenizer(line);
					tokenizer2.parseKeyword(ElseIfKeyword);
					// Drop the keyword "Then" at the end of the string.
					String remainder2 = tokenizer2.getRemainder();
					remainder2 = remainder2.substring(0, remainder2.lastIndexOf(ThenKeyword)).trim();

					// Use the Expression parser to convert the remainder to an expression.
					Expression condition2 = new ExpressionParser(remainder2, scope.getStringsMap(), parent).parse();
					addIfValue(ElseIfKeyword, condition2);
				}
				else {
					throw new BugTrap(this, "cannot parse line: " + code.get(index));
				}
				
				index++;
			}
			else {
				index = subblock.getEndLineNumber();
			}
		}
		
		
		if (Debug) {
			IOUtils.println("If block keywords: in range " + getStartLineNumber() + "-" + getEndLineNumber());
			for (String keyword : keywords) {
				IOUtils.println("  keyword: " + keyword);
			}
		}
		
		if (keywords.size()>=2) {
			// The End If must be at the end
			if (keywords.get(keywords.size()-1)!=EndKeyword) throw new BugTrap(this, "missing or misplaced " + EndKeyword);
			// Any Else must not occur before the second last keyword
			for (int k=0; k<keywords.size()-2; k++) {
				if (keywords.get(k)==ElseKeyword) throw new BugTrap(this, "missing or misplaced " + ElseKeyword);
			}
			if (keywords.size()!=subblocks.size()+1) throw new BugTrap(this, "missing a block.");
		}
		else {
			throw new BugTrap(this, "Missing " + EndKeyword);
		}

		// After created all subblocks, now parse them.
		for (Block subblock : subblocks)
			subblock.parse(code, scope, this);
	}
	
	
	
	@Override
	public int execute(Context context) throws BugTrap {
		// Check each condition until one of them hits.
		for (int index=0; index<subblocks.size(); index++) {
			if (keywords.get(index)==StartKeyword || keywords.get(index)==ElseIfKeyword) {
				// Check if the condition is satisfied before deciding whether to evaluate the block.
				Expression expression = conditions.get(index);
				DataType result = (DataType) expression.evaluate(context);
				if (result instanceof compiler.data.Boolean) {
					if (((compiler.data.Boolean) result).getValue()) {
						Context subContext = new Context(subblocks.get(index), context);
						// Once a subblock is evaluated, don't go further to other subblocks.
						return subblocks.get(index).execute(subContext);
					}
				}
			}
			else if (keywords.get(index)==ElseKeyword) {
				// Always evaluate the default when all the If's fail.
				Context subContext = new Context(subblocks.get(index), context);
				return subblocks.get(index).execute(subContext);
			}
		}
		return ExitCode_OK;
	}
}
