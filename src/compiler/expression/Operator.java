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
import compiler.main.Settings;
import compiler.util.BugTrap;

public abstract class Operator {
	public static final boolean Debug = Settings.Debug;

	public static Operator parse(String keyword) {
		if (keyword==Add.Keyword) return Add.op;
		else if (keyword==Subtract.Keyword) return Subtract.op;
		else if (keyword==Multiply.Keyword) return Multiply.op;
		else if (keyword==Divide.Keyword) return Divide.op;
		else if (keyword==Mod.Keyword) return Mod.op;
		else if (keyword==Pow.Keyword) return Pow.op;

		else if (keyword==Equal.Keyword) return Equal.op;
		else if (keyword==NotEqual.Keyword) return NotEqual.op;
		else if (keyword==GreaterThan.Keyword) return GreaterThan.op;
		else if (keyword==GreaterEqual.Keyword) return GreaterEqual.op;
		else if (keyword==LessThan.Keyword) return LessThan.op;
		else if (keyword==LessEqual.Keyword) return LessEqual.op;
		
		else if (keyword==And.Keyword) return And.op;
		else if (keyword==Or.Keyword) return Or.op;
		else if (keyword==Not.Keyword) return Not.op;
		
		return null;
	}
	
	public abstract String getKeyword();

	public abstract DataType evaluate(Literal leftValue, Literal rightValue) throws BugTrap; 
}
