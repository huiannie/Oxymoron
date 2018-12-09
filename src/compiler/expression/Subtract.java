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

import compiler.data.DataType;
import compiler.literal.Literal;
import compiler.util.BugTrap;

public class Subtract extends Operator {
	public static final String Keyword = "-";
	public static final Subtract op = new Subtract();

	public String getKeyword() {
		return Keyword;
	}
	
	@Override
	public DataType evaluate(Literal leftValue, Literal rightValue) throws BugTrap {

		if (rightValue==null)
			throw new BugTrap(this, "Incompatible arguments " + (leftValue==null?null:leftValue.valueToString()) + " and " + (rightValue==null?null:rightValue.valueToString()));

		if (leftValue==null) {
			if (rightValue instanceof compiler.data.Integer) {
				int value2 = ((compiler.data.Integer) rightValue).getValue();
				return new compiler.data.Integer(-value2);
			}
			else if (rightValue instanceof compiler.data.Real) {
				float value2 = ((compiler.data.Real) rightValue).getValue();
				return new compiler.data.Real(-value2);
			}
			throw new BugTrap(this, "Incompatible arguments " + (rightValue==null?null:rightValue.valueToString()));
		}
		
		
		
		if ((leftValue instanceof compiler.data.Integer) && (rightValue instanceof compiler.data.Integer)) {
			int value1 = ((compiler.data.Integer) leftValue).getValue();
			int value2 = ((compiler.data.Integer) rightValue).getValue();
			return new compiler.data.Integer(value1-value2);
		}
		else if ((leftValue instanceof compiler.data.Real) && (rightValue instanceof compiler.data.Integer)) {
			float value1 = ((compiler.data.Real) leftValue).getValue();
			float value2 = ((compiler.data.Integer) rightValue).getValue();
			return new compiler.data.Real(value1-value2);
		}
		else if ((leftValue instanceof compiler.data.Integer) && (rightValue instanceof compiler.data.Real)) {
			float value1 = ((compiler.data.Integer) leftValue).getValue();
			float value2 = ((compiler.data.Real) rightValue).getValue();
			return new compiler.data.Real(value1-value2);
		}
		else if ((leftValue instanceof compiler.data.Real) && (rightValue instanceof compiler.data.Real)) {
			float value1 = ((compiler.data.Real) leftValue).getValue();
			float value2 = ((compiler.data.Real) rightValue).getValue();
			return new compiler.data.Real(value1-value2);
		}
		
		throw new BugTrap(this, "Incompatible arguments " + leftValue.valueToString() + " and " + rightValue.valueToString());
	}
}
