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
import compiler.classes.ClassObjectCall;
import compiler.expression.Add;
import compiler.expression.And;
import compiler.expression.Divide;
import compiler.expression.Equal;
import compiler.expression.Expression;
import compiler.expression.GreaterEqual;
import compiler.expression.GreaterThan;
import compiler.expression.LessEqual;
import compiler.expression.LessThan;
import compiler.expression.Mod;
import compiler.expression.Multiply;
import compiler.expression.Not;
import compiler.expression.NotEqual;
import compiler.expression.Operator;
import compiler.expression.Or;
import compiler.expression.Pow;
import compiler.expression.Subtract;
import compiler.literal.ArrayCall;
import compiler.literal.Literal;
import compiler.literal.MethodCall;
import compiler.literal.SymbolCall;
import compiler.main.Settings;
import compiler.parser.StringsMap;
import compiler.tokenizers.Token;
import compiler.util.BugTrap;
import compiler.util.IOUtils;

class NestedList {
	private static final boolean Debug = Settings.Debug;	
	
	public static final String RoundOpen = Token.RoundOpen;
	public static final String RoundClose = Token.RoundClose;
	public static final String SquareOpen = Token.SquareOpen;
	public static final String SquareClose = Token.SquareClose;
	public static final String Comma = Token.Comma;
	public static final String Period = Token.Period;
	
	public static final String keywords[] = Expression.keywords;
	public static final Operator operators[] = Expression.operators;

	
	private ArrayList<NestedList> elements = null;
	
	public NestedList() { elements = new ArrayList<NestedList>(); }
	public void add(NestedList element) { elements.add(element); }
	public int size() { return elements.size(); }
	public NestedList get(int index) { return elements.get(index); }

	public static NestedList parse(Queue<String> tokenList) throws BugTrap {
		NestedList list = parseNested(tokenList);
		if (tokenList.isEmpty()) return list;
		else {
			NestedList outerlist = new NestedList();
			outerlist.add(list);
			while (!tokenList.isEmpty()) {
				NestedList appendlist = parseNested(tokenList);
				for (NestedList e : appendlist.elements)
					outerlist.add(e);
			}
			return outerlist;
		}
	}
	
	private static NestedList parseNested(Queue<String> tokenList) throws BugTrap {
		NestedList list = null;

		String peek = (String) tokenList.peek();
		if (peek==RoundOpen) {
			list = new RoundBracketList();
			tokenList.poll();
		}
		else if (peek==SquareOpen) {
			list = new SquareBracketList();
			tokenList.poll();
		}
		else list = new NestedList();

		while (!tokenList.isEmpty()) {
			String token = (String) tokenList.peek();
			
			if (token==RoundOpen) {
				// Do not remove token. Pass it to the recursive part.
				NestedList sublist = parseNested(tokenList);
				list.add(sublist);
			}
			else if (token==RoundClose) {
				tokenList.poll();
				if (!(list instanceof RoundBracketList))
					throw new BugTrap(token + " cannot find opening match.");
				return list;
			}
			else if (token==SquareOpen) {
				// Do not remove token. Pass it to the recursive part.
				NestedList sublist = parseNested(tokenList);
				list.add(sublist);				
			}
			else if (token==SquareClose) {
				tokenList.poll();
				if (!(list instanceof SquareBracketList))
					throw new BugTrap(token + " cannot find opening match.");
				return list;
			}
			else {
				list.add( new SingletonList(token) );	
				tokenList.poll();
			}
		}
		if (list instanceof RoundBracketList) throw new BugTrap(" cannot find closing match.");
		else if (list instanceof SquareBracketList) throw new BugTrap(" cannot find closing match.");
		else return list;				
	}
	
	public String flatten() {
		String s = "";
		for (NestedList l : elements) {
			s += l.flatten();
		}
		if (this instanceof RoundBracketList)
			return RoundOpen + s + RoundClose;
		else if (this instanceof SquareBracketList)
			return SquareOpen + s + SquareClose;
		else return s;
	}
	
	
	
	public Object interpret(StringsMap stringsMap, Block callerBlock) throws BugTrap {
		ParsedList parsedList;
		
		if (Debug) IOUtils.println("Now interpreting " + this.flatten());
		
		// Retain the nature of the bracket.
		if (this instanceof RoundBracketList) {
			parsedList = new ParsedRoundBracketList();
			if (Debug) IOUtils.println("Found a round bracket list");
		}
		else if (this instanceof SquareBracketList) {
			parsedList = new ParsedSquareBracketList();
			if (Debug) IOUtils.println("Found a square bracket list");
		}
		else {
			parsedList = new ParsedList();
			if (Debug) IOUtils.println("Found an un-bracketed list");
		}
		
		
		// First, interpret each element in the list.
		for (NestedList e : elements) {
			parsedList.add(e.interpret(stringsMap, callerBlock));
		}
		if (Debug) {
			IOUtils.println("Collected elements ");
			parsedList.print(2);
		}
		
		// now interpret this list.
		// Repeat this process until there is no further reduction.
		boolean changed;
		do {
			changed = false;
			changed |= parseOperationsByPrecedence(parsedList);
			changed |= removeRedundantBrackets(parsedList);
			
			changed |= parseFunctionCalls(parsedList, callerBlock);
			changed |= removeRedundantBrackets(parsedList);

			changed |= parseArrayCalls(parsedList, callerBlock);
			changed |= removeRedundantBrackets(parsedList);
			
			changed |= parseClassObjectCalls(parsedList, callerBlock);
			changed |= removeRedundantBrackets(parsedList);
			
			changed |= parseSimpleVariables(parsedList, callerBlock);
			changed |= removeRedundantBrackets(parsedList);

		} while (changed);
		
		
		return parsedList;
	}

	
	// Whether a list is a parameter list depends on what is preceding it.
	// If it is preceded by a wildcard, then yes, it is.
	boolean isParameterList(ParsedList objectList, int index) {
		Object object = objectList.get(index);
		if (!(object instanceof ParsedRoundBracketList)) return false;
		if (index==0) return false;

		// But if the object is preceded by Fn in the objectList, then it is certainly a parameter list
		if (objectList.get(index-1) instanceof Wildcard) return true;
		
		// If the object is (), it cannot be decided
		// If the object is (Expression), it cannot be decided.
		return false;
	}
	
	
	boolean removeRedundantBrackets(ParsedList objectList) throws BugTrap {
		boolean changed = false;
		int index=0; 
		while (index<objectList.size()) {
			if (!isParameterList(objectList, index) 
				&& (objectList.get(index) instanceof ParsedRoundBracketList)) {
				
				ParsedRoundBracketList roundlist = (ParsedRoundBracketList) objectList.get(index);
				int size = roundlist.size();
				
				if (size!=1 || !(roundlist.get(0) instanceof Expression))
					throw new BugTrap("Error in Round-bracketed list " + roundlist.getAllElementsAsString());
				Expression exp = (Expression) roundlist.get(0);

				objectList.set(index, exp);
				changed = true;
			}
			index++;
		}
		return changed;
	}



	boolean parseOperations(ParsedList objectList, Operator operators[]) throws BugTrap {
		boolean changed = false;
		
		// Check if it is ready to parse operations.
		// The list is ready only when all the elements are either Expressions or Operators.
		for (int i=0; i<objectList.size(); i++)
			if (!(objectList.get(i) instanceof Operator) &&
				!(objectList.get(i) instanceof Expression) &&
				(objectList.get(i) != Comma))
				return false;
		

		// Scan through the list once and replace any simple unary or binary operations by an expression
		int index = 0;
		while (index<objectList.size()) {
			Operator operator = null;
			for (Operator op : operators) {
				if (objectList.get(index)==op)
					operator = op;
			}
			// current object is not an operator. Skip it.
			if (operator==null) { index++; continue; }

			int leftIndex = index-1;
			int rightIndex = index+1;
				

			// Cases where an operator can be either unary or binary: +, -, Not
			if ((operator==Subtract.op && leftIndex<0) ||
				 (operator==Subtract.op && (objectList.get(leftIndex) instanceof Operator)) ||
				 (operator==Add.op && leftIndex<0) ||
				 (operator==Add.op && (objectList.get(leftIndex) instanceof Operator)) ||
				 (operator==Not.op) ) {
				Object left = null;
				Object right = objectList.get(rightIndex);
				
				// operand(s) of current operator is not ready. Skip this operator.
				if (!(right instanceof Expression)) { index++; continue; }
				
				// Build a subtree with the operator as parent and the single operand as right child.
				// The left child is empty. 
				// Then replace the operator and operand by the subtree. TokenList should reduce size by 1.
				Expression exp = new Expression((Expression)left, operator, (Expression)right);
				objectList.set(index, exp);
				objectList.remove(rightIndex);
				changed = true;
			}
			
			// Cases where the operator can only be binary
			else {
				Object left = objectList.get(leftIndex);
				Object right = objectList.get(rightIndex);
				
				// operand(s) of current operator is not ready. Skip this operator.
				if (!(left instanceof Expression) || !(right instanceof Expression)) { index++; continue; }

				
				// Build a subtree with the operator as parent and the two operands as children.
				// Then replace the operator and operand by the subtree. TokenList should reduce size by 2.
				Expression exp = new Expression((Expression)left, operator, (Expression)right);
				objectList.set(index, exp);
				objectList.remove(rightIndex);
				objectList.remove(leftIndex);
				changed = true;
			}
		}
		return changed;
	}
	
	

	
	
	boolean parseOperationsByPrecedence(ParsedList objectList) throws BugTrap {

		boolean changed = false;

		// Check precedence
		changed |= parseOperations(objectList, new Operator[]{Pow.op});
		changed |= parseOperations(objectList, new Operator[]{Multiply.op, Divide.op, Mod.op});
		changed |= parseOperations(objectList, new Operator[]{Add.op, Subtract.op});
		
		changed |= parseOperations(objectList, new Operator[]{Equal.op, NotEqual.op, GreaterThan.op, GreaterEqual.op, LessThan.op, LessEqual.op});
		
		changed |= parseOperations(objectList, new Operator[]{Not.op});
		changed |= parseOperations(objectList, new Operator[]{And.op});
		changed |= parseOperations(objectList, new Operator[]{Or.op});
		if (Debug) {
			IOUtils.println("After checking precedence: objectList size = " + objectList.size());
			objectList.print(0);
		}
		return changed;
	}

	
	
	boolean parseFunctionCalls(ParsedList objectList, Block callerBlock) throws BugTrap {
		
		boolean changed = false;
		
		// Scan through the list once and replace any simple potential function call by an expression
		int index = 0;
		while (index<objectList.size()-1) { // only go up to the second last index in the list because a function must have an argument.
			Object obj = objectList.get(index);
			Object nextObject = objectList.get(index+1);
			if (obj instanceof Wildcard 
				&& nextObject instanceof ParsedRoundBracketList 
				&& MethodCall.isFunctionCallArgumentList((ParsedRoundBracketList) nextObject)) {

				if (Debug) IOUtils.println("Found function call");
				
				Wildcard wildcard = (Wildcard) obj;
				
				MethodCall functionCall = MethodCall.parse(wildcard.getName(), (ParsedRoundBracketList) nextObject, callerBlock);

				objectList.set(index, new Expression(functionCall));
				objectList.remove(index+1);
				changed = true;
			}
			index++;
		} 
		if (Debug) {
			IOUtils.println("After checking function call: objectList size = " + objectList.size());
			objectList.print(0);
		}
		return changed;
	}

	boolean parseArrayCalls(ParsedList objectList, Block callerBlock) throws BugTrap {
		
		boolean changed = false;
		
		// Syntax of an array access call: A variableArray that is already declared, followed by at least one [ Expression ]
		// Example: A[Expr][Expr]
		// Scan through the list once and replace any simple potential function call by an expression
		int index = 0;

		
		while (index<objectList.size()-1) { // only go up to the second last index in the list because an array must have at least an open square bracket.

			//if (Debug) IOUtils.println("At index " + index + " of list size " + objectList.size());
			
			Object obj = objectList.get(index);
			Object nextobj = objectList.get(index+1);

			if (obj instanceof Wildcard && nextobj instanceof ParsedSquareBracketList){
				String name = ((Wildcard) obj).getName();
				
				// if (Debug) IOUtils.println("Found an array name at index " + index + " [..] at index " + (index+1));
				
				ArrayList<Expression> arguments = new ArrayList<Expression>();

				int dimension = 0;
				int j = index+1;
				boolean done = false;
				do {
					if (j<objectList.size() && (objectList.get(j) instanceof ParsedSquareBracketList)) {
						int argsize = ((ParsedSquareBracketList) objectList.get(j)).size();
						
						if (argsize!=1) throw new BugTrap(callerBlock, "bad array argument in []");

						Object arg = ((ParsedSquareBracketList) objectList.get(j)).get(0);
						if (argsize==1 && arg instanceof Expression) {
							// Found one dimension.
							arguments.add((Expression) arg);
							dimension++;
							j = j + 1;
						}
						else {
							throw new BugTrap(callerBlock, "array index size = " + argsize);
						}
					}
					else {
						done = true;
					}
				} while (!done);
				
				// After checking for dimensions, check the object after the last 
				// close square bracket that was matched. 
				if (j==objectList.size() || !(objectList.get(j) instanceof ParsedSquareBracketList)) {
					// All arguments are ready
					ArrayCall arrayCall = new ArrayCall(name, callerBlock);
					arrayCall.setArguments(arguments);
					
					// Replace the wildcard by a call to the array.
					objectList.set(index, new Expression(arrayCall));

					// Delete all [exp][exp]... that have been parsed after obj.
					for (int d=0; d<dimension; d++) {
						objectList.remove(index+1);
					}
					changed = true;
				}
				else {
					// Some arguments may still need to be converted to a single expression. Skip this time.
				}
			}
			index++;
		}		
		if (Debug) {
			IOUtils.println("After checking array access call: objectList size = " + objectList.size());
			objectList.print(0);
		}
		return changed;		
	}
	
	
	boolean parseClassObjectCalls(ParsedList objectList, Block callerBlock) throws BugTrap {
		boolean changed = false;
		
		// Scan through the list once and replace any simple potential function call by an expression
		int index = 0;
		while (index<objectList.size()-2) { // only go up to the third last index in the list because a class object must have a dot and an argument.
			Object obj = objectList.get(index);
			Object nextObject = objectList.get(index+1);
			Object nextnextObject = objectList.get(index+2);

			if (Debug) {
				if (obj instanceof Wildcard) IOUtils.println(" checking wildcard object " + ((Wildcard) obj).getName());
			}
			
			// Handle simple class object of the form abc.xxx
			if (obj instanceof Wildcard && nextObject==Period 
				&& ClassObjectCall.isClassObjectCallArgument(nextnextObject)) {
				
				Wildcard wildcard = (Wildcard) obj;
				Literal literal = ((Expression)nextnextObject).getLiteral();
				ClassObjectCall classObjectCall = new ClassObjectCall(wildcard.getName(), literal, callerBlock);
				objectList.set(index, new Expression(classObjectCall));
				// Remove the period and the argument after the period.
				objectList.remove(index+1);
				objectList.remove(index+1);
				changed = true;
			}
			// Handle simple class array object of the form abc[1][2].xxx
			else if (obj instanceof Expression && ((Expression)obj).isLiteral() 
					&& (((Expression)obj).getLiteral() instanceof ArrayCall)
					&& nextObject==Period 
					&& ClassObjectCall.isClassObjectCallArgument(nextnextObject)) {
				Literal literal = ((Expression)nextnextObject).getLiteral();
				ClassObjectCall classObjectCall = new ClassObjectCall((ArrayCall) ((Expression)obj).getLiteral(), literal, callerBlock);
				objectList.set(index, new Expression(classObjectCall));
				// Remove the period and the argument after the period.
				objectList.remove(index+1);
				objectList.remove(index+1);
				changed = true;
			}
			
			index++;
		} 
		if (Debug) {
			IOUtils.println("After checking class fields: objectList size = " + objectList.size());
			objectList.print(0);
		}
		return changed;
	}
	
	boolean parseSimpleVariables(ParsedList objectList, Block callerBlock) throws BugTrap {

		boolean changed = false;

		int index = 0;
		while (index<objectList.size()) {
			Object obj = objectList.get(index);
			if (obj instanceof Wildcard) {
				if (index==objectList.size()-1) {
					// This is the last item. It has to be a simple variable.
					String name = ((Wildcard) obj).getName();
					SymbolCall varCall = new SymbolCall(name, callerBlock);
					// Replace the wildcard by a call to the variable.
					objectList.set(index, new Expression(varCall));
					changed = true;
				}
				else {
					Object nextobj = objectList.get(index+1);
					if (!(nextobj instanceof ParsedRoundBracketList) 
						&& !(nextobj instanceof ParsedSquareBracketList)  
						&& nextobj!=Period) {
						// This is NOT the last item. It is not followed by a ( or [.
						// Potentially just a simple variable.
						String name = ((Wildcard) obj).getName();
						SymbolCall varCall = new SymbolCall(name, callerBlock);
						// Replace the wildcard by a call to the variable.
						objectList.set(index, new Expression(varCall));
						changed = true;
					}
				}
			}
			index++;
		}
		if (Debug) {
			IOUtils.println("After checking simple variables: objectList size = " + objectList.size());
			objectList.print(0);
		}
		return changed;
	}

	
	
	
	
	public Expression toExpression(StringsMap stringsMap, Block block) throws BugTrap {
		Object obj = interpret(stringsMap, block);
		
		// Singleton Expression
		if (obj instanceof Expression) {
			Expression exp = (Expression) obj;
			if (Debug) IOUtils.println("Final expression is " + exp.toStringInBracket());
			return exp;
		}
		else if (obj instanceof ParsedSquareBracketList) {
			throw new BugTrap(block, "Square-bracketed list cannot convert to expression ");
		}
		else if (obj instanceof ParsedList) {
			ParsedList list = (ParsedList) obj;
			Expression exp=null;

			// Singleton expression in a list
			if (list.size()==1) {
				exp = (Expression) list.get(0);
				if (Debug) IOUtils.println("Final singleton expression is " + exp.toStringInBracket());
				return exp;
			}
			else
				throw new BugTrap(block, "Cannot convert to expression ");
		}
		throw new BugTrap(block, "Cannot convert to expression ");
	}

	
	public ArrayList<Expression> toExpressionList(StringsMap stringsMap, Block block) throws BugTrap {
		Object obj = interpret(stringsMap, block);

		ArrayList<Expression> exprs = new ArrayList<Expression>();
		
		
		// Singleton Expression
		if (obj instanceof Expression) {
			Expression exp = (Expression) obj;
			if (Debug) IOUtils.println("Final expression is " + exp.toStringInBracket());
			exprs.add(exp);
			return exprs;
		}
		else if (obj instanceof ParsedSquareBracketList) {
			throw new BugTrap(block, "Square-bracketed list cannot convert to expression list");
		}
		else if (obj instanceof ParsedList) {
			ParsedList list = (ParsedList) obj;
			Expression exp;
			
			// Singleton expression in a list
			// Example: A  in a list
			if (list.size()==1) {  // Singleton
				exp = (Expression) list.get(0);
				if (Debug) IOUtils.println("Final singleton expression is " + exp.toStringInBracket());
				exprs.add(exp);
				return exprs;
			}
			// Bracketed multiple-token Expression in a list
			else {
				for (int index=0; index<list.size(); index+=2) {
					if (!(list.get(index) instanceof Expression))
						throw new BugTrap(block, NestedList.class.getName()+": Fail to parse argument");
					exprs.add((Expression)list.get(index));
						
					if (index+1<list.size() && !(list.get(index+1) instanceof String) && list.get(index+1).equals(Comma))
						throw new BugTrap(block, NestedList.class.getName()+": Fail to parse argument");

				}		
				return exprs;
			}

		}
		throw new BugTrap(block, "Cannot convert to expression ");
	}
	
	
	public static void main1(String args[]) throws BugTrap {
		String Gexpressions[] = {"__A + X",
								"F(Y, __B, Z)",
								"G(Y, __B, Z) + __C",
								"G(F(__A, X, Y), __B, Z) - __C * H(Y, __B, Z) + __A MOD X[0+Y][F(Y,Z)]",
								"H[G(F(__A, X, Y), __B, Z) - __C * H(Y, __B, Z) + __A MOD X[0+Y][F(Y,Z)]]",
								"F[__A*(9^__A)]",
								"F[1] [__A*(9^__A)]",
								"F[1][2] + G[__A*(9^__A)]",
								"F[C[0].D(__A, 1)] + 1 + G[1] + __A*(9^__A)",
								};
		String Aexpressions[] = {"F[1]",
								"F[1][2]",
								"F[1][__A]",
								"F[1][__A+1]",
								"F[1][__A][4+2]",
								"F[1][2][4+2]",
								"F[(0+1)]",
								"F[(0+1)+(__A-1)]",
								"F[(0+1)+(__A-1)][1][2]",
								"F[(0+1)+(__A-1)][1][2] + 1",
								"F[(0+1)+(__A-1)][1][2] + G[(((0)))]",
								};
		String Fexpressions[] = {
								"F(0)",
								"F(0, 1, 2)",
								"F(0, 1, (1+A))",
								"F(0, 1, F[1+A])",
				
		};
		String Cexpressions[] = {
				"F.G(0)",
				"F.G(0, 1, 2)",
				"F.G(0, 1, (1+A))",
				"F.G.H(0, 1, F[1+A])",
		};
		String Bexpressions[] = {
				"(0)",
				"(0, 1, 2)",
				"((0)+2)",
				"(((((0)))))",
				"(((((0))))) + 1",
				"(((((0))))) + (1 + 2)",
				"1 + (((((0))))) + 2",
				"1 + (((((0))))) + (1 + 1)",
		};

		String expressions[] = {
				"(1 + 2 + 3) / 4"
		};

		// Set up some dummy values
		Block block = new Block(0, 0);
		StringsMap map = new StringsMap();
		map.put("__A", "ItsMe");
		map.put("__B", "ItsYou");
		map.put("__C", "ItsHim");
		map.put("__D", "ItsHer");

		for (String e : expressions) {
			Queue<String> tokensList = ExpressionTokenizer.tokenize(e);

			try {
				for (Object s : tokensList.toArray())
					IOUtils.print((String)s + " "); 
				IOUtils.println();
				
				NestedList nestedList = NestedList.parse(tokensList);
				IOUtils.println("Expression string " + e + " converted to nested list " + nestedList.flatten());
				Object parsed = nestedList.interpret(map, block);
				if (parsed instanceof ParsedList)
					((ParsedList) parsed).print(0);
			
			}
			catch (BugTrap e2) { 
				e2.printStackTrace();
				IOUtils.println(e + " cannot be parsed. " + e2.getDetails()); 
				return; 
			}
		}
	}
}
