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
import compiler.binder.Variable;
import compiler.blocks.Block;
import compiler.data.DataType;
import compiler.declarations.ConstantDeclaration;
import compiler.expression.Expression;
import compiler.expressionparser.ExpressionParser;
import compiler.literal.Literal;
import compiler.literal.SymbolCall;
import compiler.main.Settings;
import compiler.parser.Scope;
import compiler.tokenizers.LineMatcher;
import compiler.tokenizers.LineTokenizer;
import compiler.tokenizers.Token;
import compiler.util.BugTrap;
import compiler.util.IOUtils;

public class Select extends Block {
	public static final boolean Debug = Settings.Debug;

	public static final String StartKeyword = "Select";
	public static final String CaseKeyword = "Case";
	public static final String DefaultKeyword = "Default";
	public static final String EndKeyword = "End Select";

	private static final String Colon = Token.Colon;
	
	static final DataType defaultCase = new compiler.data.String(DefaultKeyword);
	
	String name;  // variable name
	private ArrayList<Literal> caseValues = new ArrayList<Literal>();

	public Select(int startLineNumber, int endLineNumber) {
		super(startLineNumber, endLineNumber);
		hasHeaderAndFooter = true;
	}

	
	public static boolean startsWithAKeyword(String line) {
		if (LineMatcher.matchStart(line, CaseKeyword)) return true;
		else if (LineMatcher.matchStart(line, DefaultKeyword)) return true;
		else if (LineMatcher.matchStart(line, EndKeyword)) return true;
		else if (LineMatcher.matchStart(line, StartKeyword)) return true;
		return false;
	}
	
	
	public static boolean isAStart(String line) {
		String conditionToken = Token.OneCharacterOrMore;
		return (LineMatcher.matchStart(line, StartKeyword, conditionToken));
	}

	public static boolean isAnEnd(String line) {
		return (LineMatcher.matchExact(line, EndKeyword));
	}
	

	
	public void parse(ArrayList<String> code, Scope scope, Block parent) throws BugTrap {
		if (this.parent==null) this.parent = parent;

		
		int lineNumber = getStartLineNumber();
		String line1 = code.get(lineNumber);

		// Syntax: Select variable
		LineTokenizer tokenizer = new LineTokenizer(line1);
		
		if (!tokenizer.isKeyword(StartKeyword)) throw new BugTrap(this, StartKeyword + ": Keyword missing");
		tokenizer.parseKeyword(StartKeyword);

		if (!tokenizer.isName()) throw new BugTrap(this, "Missing variable name");
		name = tokenizer.parseName();
		
		
		// Find the cases and keep track of the line positions of the keywords
		ArrayList<Integer> positions = new ArrayList<Integer>();
		
		int start = getStartLineNumber() + 1;
		int end = getEndLineNumber() - 1;

		for (int index=start; index<end; index++) {
			String line = code.get(index);
			
			if (LineMatcher.matchStart(line, CaseKeyword)) {
				
				LineTokenizer tokenizer2 = new LineTokenizer(line);
				tokenizer2.parseKeyword(CaseKeyword);
				// Drop the colon at the end of the string.
				String remainder2 = tokenizer2.getRemainder();
				remainder2 = remainder2.substring(0, remainder2.lastIndexOf(Colon)).trim();

				
				// Must be constant or number or string
				Expression expr = new ExpressionParser(remainder2, scope.getStringsMap(), this).parse();
				Literal value = expr.getLiteral();
				if (value==null) throw new BugTrap(this, "Case value must be simple value. ");
				
				if (value instanceof DataType) {
					caseValues.add((DataType) value);
				}
				else if (value instanceof ConstantDeclaration) {
					caseValues.add((SymbolCall) value);					
				}
				else
					throw new BugTrap(this, "Case value must be simple value. ");
					
				positions.add(index);
			}
			else if (LineMatcher.matchExact(line, DefaultKeyword + ":")) {
				caseValues.add(defaultCase);
				positions.add(index);
			}
		}
		
		// Check syntax
		if (positions.get(0)!=start) throw new BugTrap(this, "invalid content after Select");
		
		if (caseValues.size()>=1) {
			for (int k=0; k<caseValues.size()-1; k++) {
				if (caseValues.get(k)==defaultCase) 
					throw new BugTrap(this, "default case misplaced " + DefaultKeyword);
			}			
		}
		else {
			throw new BugTrap(this, "Missing " + CaseKeyword);
		}
		
		
		// Create the subblocks with proper scopes.
		for (int p=0; p<positions.size(); p++) {
			// Find the start and end of a block
			int blockStart = positions.get(p)+1;
			int blockEnd = p+1<positions.size() ? positions.get(p+1) : end;
			
			if (blockStart>=blockEnd) throw new BugTrap(this, "Empty case.");

			Block caseblock = new Block(blockStart, blockEnd);
			add(caseblock);
		}
		
		// Parse each block
		for (Block subblock : subblocks)
			subblock.parse(code, scope, this);
		
		if (Debug) IOUtils.printIndented(2, this.getClass().getSimpleName()+" is now parsed.");
	}
	
	
	
	
	@Override
	public int execute(Context context) throws BugTrap {
		Variable variable = context.getVariable(name);
		DataType value = variable.getValue();
		
		// Check each condition until one of them hits.
		for (int index=0; index<subblocks.size(); index++) {
			DataType caseValue = (DataType) caseValues.get(index).evaluate(context);
			if (Debug) IOUtils.println("Comparing " + value.valueToString() + " with " + caseValue.valueToString());
			if (value.equals(caseValue) && caseValue!=defaultCase) {
				Context subContext = new Context(subblocks.get(index), context);
				return subblocks.get(index).execute(subContext);
			}
			else if (caseValue==defaultCase) {
				Context subContext = new Context(subblocks.get(index), context);
				return subblocks.get(index).execute(subContext);
			}			
		}
		return ExitCode_OK;
	}
}
