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
package compiler.data;

import compiler.tokenizers.Token;
import compiler.util.BugTrap;

public abstract class PrimitiveDataType extends DataType {
	
	
	
	// Once an object is declared of this type, it must be primitive.
	public boolean isPrimitive() {
		return true;
	}
	// Once an object is declared of this type, it must NOT be file.
	public boolean isFile() {
		return false;
	}
	// Once an object is declared of this type, it must NOT be aggregate
	public boolean isAggregate() {
		return false;
	}



	public static PrimitiveDataType parse(java.lang.String type) throws BugTrap {
		if (compiler.data.String.matchesType(type)) 
			return new compiler.data.String();
		else if (compiler.data.Integer.matchesType(type)) {
			return new compiler.data.Integer();
		}
		else if (compiler.data.Character.matchesType(type)) {
			return new compiler.data.Character();
		}
		else if (compiler.data.Real.matchesType(type)) {
			return new compiler.data.Real();
		}
		else if (compiler.data.Boolean.matchesType(type)) {
			return new compiler.data.Boolean();
		}
		throw new BugTrap(type + ": type not recognized!");
	}
	
	public static PrimitiveDataType parse(java.lang.String type, java.lang.String value) throws BugTrap {
		if (compiler.data.String.matchesType(type)) {
			return new compiler.data.String(value);
		}
		else if (compiler.data.Integer.matchesType(type)) {
			return new compiler.data.Integer(java.lang.Integer.parseInt(value));
		}
		else if (compiler.data.Character.matchesType(type)) {
			if (value.matches("\\'.\\'"))
				return new compiler.data.Character(value.charAt(1));
		}
		else if (compiler.data.Real.matchesType(type)) {
			return new compiler.data.Real(Float.parseFloat(value));
		}
		else if (compiler.data.Boolean.matchesType(type)) {
			if (value.equals(compiler.data.Boolean.True))
				return new compiler.data.Boolean(true);
			else if (value.equals(compiler.data.Boolean.False))
				return new compiler.data.Boolean(false);
			else {
				throw new BugTrap(type + " type : value " + value + " not recognized!");
			}
		}

		throw new BugTrap(type + ": type not recognized!");
	}
	
	public static PrimitiveDataType parseValue(java.lang.String value) {
		try {
			return new compiler.data.Integer(java.lang.Integer.parseInt(value));
		} catch (java.lang.Exception e1) {
			try {
				return new compiler.data.Real(Float.parseFloat(value));
			} catch (java.lang.Exception e2) {
				if (value.matches("\\'.\\'")) {
					if (value.equals(Token.Tab)) return (PrimitiveDataType) new compiler.data.Character(Token.Tab.charAt(0));
					return new compiler.data.Character(value.charAt(1));
				}
				else {
					if (value.equals(compiler.data.Boolean.True)) return new compiler.data.Boolean(true);
					else if (value.equals(compiler.data.Boolean.False)) return new compiler.data.Boolean(false);
					else
						// Last resort: Convert value to a string.
						return new compiler.data.String(value);
				} 
			}
		}
	}
	
	public java.lang.String valueToString() {
		if (compiler.data.Integer.matchesType(getType()))
			return "" + ((compiler.data.Integer)this).getValue();
		else if (compiler.data.Real.matchesType(getType()))
			return "" + ((compiler.data.Real)this).getValue();
		else if (compiler.data.String.matchesType(getType()))
			return "" + ((compiler.data.String)this).getValue();
		else if (compiler.data.Character.matchesType(getType()))
			return "" + ((compiler.data.Character)this).getValue();
		else if (compiler.data.Boolean.matchesType(getType())) {
			if (((compiler.data.Boolean)this).getValue())
				return "" + compiler.data.Boolean.True;
			else
				return "" + compiler.data.Boolean.False;
		}
		else return "Value of Data literal is Unknown!!";
	}

}
