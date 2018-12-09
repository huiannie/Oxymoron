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
package compiler.literal;

import compiler.binder.Constant;
import compiler.binder.Context;
import compiler.binder.Variable;
import compiler.blocks.Block;
import compiler.classes.ClassObject;
import compiler.classes.ClassType;
import compiler.data.DataType;
import compiler.main.Settings;
import compiler.util.BugTrap;
import compiler.util.IOUtils;

/* In this system, both variables and constants are dynamically evaluated at runtime.
 * Therefore, at the time of parsing, it is not possible to distinguish whether
 * a symbol is a variable or a constant.
 * 
 * A variable/constant access is different from a variable/constant declaration.
 * A variable/constant must be declared before it can be accessed.
 * Access may be for reading or writing.
 */

public class SymbolCall extends UnbindedLiteral {
	public static final boolean Debug = Settings.Debug;
	
	String name;
	Block parent;
	
	public SymbolCall(String name, Block parent) {
		this.name = name;
		this.parent = parent;
	}

	public String getName() {
		return name;
	}


	public void print(int indent) {
		IOUtils.printIndented(indent, "Variable access: "
							+ " name=" + name);
	}
	
	
	@Override
	public String valueToString() {
		return name;
	}

	public Variable getVariable(Context context) throws BugTrap {
		Literal literal = context.getDeclared(name);
		if (literal instanceof Constant) {
			throw new BugTrap(context, name + " is not a variable");
		}
		else if (literal instanceof Variable) {
			return ((Variable) literal);
		}
		throw new BugTrap(context, name + " is a bad symbol.");
	}
	
	public Constant getConstant(Context context) throws BugTrap {
		Literal literal = context.getDeclared(name);
		if (literal instanceof Constant) {
			return ((Constant) literal);
		}
		else if (literal instanceof Variable) {
			throw new BugTrap(context, name + " is not a constant");
		}
		throw new BugTrap(context, name + " is a bad symbol.");
	}
	
	public String getDataType(Context context) throws BugTrap {
		Literal literal = context.getDeclared(name);
		if (literal instanceof Constant) {
			return ((Constant) literal).getValue().getType();
		}
		else if (literal instanceof Variable) {
			return ((Variable) literal).getValue().getType();
		}
		throw new BugTrap(context, name + " is a bad symbol.");
	}
	
	public void setValue(Context context, DataType newValue) throws BugTrap {
		if (Debug) {
			IOUtils.println("Setting variable " + name + " to " + newValue.valueToString());
			context.print(0);
		}
		Variable variable = context.getVariable(name);

		variable.setValue(newValue);
	}
	
	
	public void setValue(Context context, ClassType newValue) throws BugTrap {
		if (Debug) {
			IOUtils.println("Setting classObject " + name + " to " + newValue.valueToString());
			context.print(0);
		}
		ClassObject classObj = context.getClassObject(name);

		classObj.setValue(newValue);
	}

	
	@Override
	public ValueType evaluate(Context context) throws BugTrap {
		// evaluate() is only to get the value. 
		// To set the element use setValue().
		
		Literal literal = context.getDeclared(name);
		if (literal instanceof Constant) {
			return ((Constant) literal).getValue();
		}
		else if (literal instanceof Variable) {
			return ((Variable) literal).getValue();
		}
		else {
			literal = context.getClassObject(name);
			if (literal instanceof ClassObject) {
				return ((ClassObject) literal).getValue();
			}
		}
		throw new BugTrap(context, this, name + " cannot be evaluated.");
	}


	
}
