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
package compiler.expression;

import compiler.binder.Context;
import compiler.classes.ClassObjectCall;
import compiler.classes.ConstructorCall;
import compiler.data.DataType;
import compiler.declarations.ConstantDeclaration;
import compiler.literal.ArrayCall;
import compiler.literal.Literal;
import compiler.literal.MethodCall;
import compiler.literal.SymbolCall;
import compiler.literal.ValueType;
import compiler.tokenizers.Token;
import compiler.util.BugTrap;

public class Expression {
	private static final boolean ShortCircuitBooleanEvaluation = true;
	
	public static final String keywords[] = {Token.RoundOpen, Token.RoundClose, 
											Token.SquareOpen, Token.SquareClose,
											Add.Keyword, Subtract.Keyword, Multiply.Keyword, Divide.Keyword, Mod.Keyword, Pow.Keyword,
											Equal.Keyword, NotEqual.Keyword, GreaterThan.Keyword, GreaterEqual.Keyword, LessThan.Keyword, LessEqual.Keyword,
											And.Keyword, Or.Keyword, Not.Keyword};
	
	public static final Operator operators[] = {
			Add.op, Subtract.op, Multiply.op, Divide.op, Mod.op, Pow.op,
			Equal.op, NotEqual.op, GreaterThan.op, GreaterEqual.op, LessThan.op, LessEqual.op,
			And.op, Or.op, Not.op};

	
	public String raw; // for debugging purpose
	Expression left;
	Expression right;
	Operator operator;

	Literal literal;

	public Expression(Literal literal) {
		this.literal = literal;
	}
	public Expression(Expression left, Operator operator, Expression right) {
		this.left = left;
		this.right = right;
		this.operator = operator;
		this.literal = null;
	}
	
	public boolean isLiteral() {
		return literal!=null;
	}
	public Literal getLiteral() {
		return literal;
	}
	
	
	// The following is used in Array
	public java.lang.String toStringInBracket() {
		if (literal!=null) {
			// Check the type of the literal
			String type = null;
			String s = null;
			if (literal instanceof ConstantDeclaration) {
				s = ((ConstantDeclaration)literal).getName();
				type="{c}";
			}
			else if (literal instanceof DataType) {
				s = literal.valueToString();
				type="{"+((DataType)literal).getType()+"}";
			}
			else if (literal instanceof MethodCall) {
				s = ((MethodCall)literal).getName();
				MethodCall fn = (MethodCall)literal;
				String args = Token.RoundOpen;
				for (int index=0; index<fn.getArguments().size(); index++) {
					Expression e = fn.getArguments().get(index);
					args += e.toStringInBracket();
					if (index<fn.getArguments().size()-1)
						args += Token.Comma;
				}
				args += Token.RoundClose;
				s += args;
				type="{fn}";
			}
			else if (literal instanceof ArrayCall) {
				s = ((ArrayCall)literal).valueToString();
				type="{a}";
			}
			else if (literal instanceof ClassObjectCall) {
				s = ((ClassObjectCall)literal).valueToString();
				type="{c}";
			}
			else if (literal instanceof SymbolCall) {
				s = ((SymbolCall)literal).valueToString();
				type="{v}";
			}
			else if (literal instanceof ConstructorCall) {
				s = ((ConstructorCall)literal).valueToString();
				type="{v}";
			}
			return s+type;
		}
		else {
			if (left==null)  // unary
				return Token.RoundOpen + operator.getKeyword() + " " + right.toStringInBracket() + Token.RoundClose;
			else  // binary
				return Token.RoundOpen + left.toStringInBracket() + " " + operator.getKeyword() 
						+ " " + right.toStringInBracket() + Token.RoundClose;
		}
	}
	
	
	public ValueType evaluate(Context context) throws BugTrap {
		if (isLiteral()) return literal.evaluate(context);
		else {
			Literal leftValue = null;
			Literal rightValue = null;
			
			if (left!=null) leftValue = left.evaluate(context);

			if (ShortCircuitBooleanEvaluation) {
				// impose short circuit. When the first time is ready, no need to evaluate the second term.
				if ((operator instanceof And) && (leftValue instanceof compiler.data.Boolean)) {
					// AND: If the first term is false, then the outcome must be false.
					if (!((compiler.data.Boolean)leftValue).getValue()) return new compiler.data.Boolean(false);
				}
				// impose short circuit. When the first time is ready, no need to evaluate the second term.
				if ((operator instanceof Or) && (leftValue instanceof compiler.data.Boolean)) {
					// OR: If the first term is true, then the outcome must be true.
					if (((compiler.data.Boolean)leftValue).getValue()) return new compiler.data.Boolean(true);
				}				
			}

			if (right!=null) rightValue = right.evaluate(context);
			
			return operator.evaluate(leftValue, rightValue);
		}
	}

}
