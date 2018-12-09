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

import java.util.ArrayList;

import compiler.binder.Constant;
import compiler.binder.Context;
import compiler.binder.Variable;
import compiler.binder.VariableRef;
import compiler.blocks.Block;
import compiler.data.Array;
import compiler.data.DataType;
import compiler.data.Integer;
import compiler.expression.Expression;
import compiler.main.Settings;
import compiler.util.BugTrap;
import compiler.util.IOUtils;

/* An array access is different from an array declaration.
 * An array must be declared before it can be accessed.
 * Access may be for reading or writing.
 */

public class ArrayCall extends UnbindedLiteral {
	public static final boolean Debug = Settings.Debug;

	private static final int IsArray = 1;
	private static final int IsString = 2;
	
	
	String name;
	Block parent;
	ArrayList<Expression> argumentsList;
	
	public ArrayCall(String name, Block parent) {
		this.name = name;
		this.parent = parent;
	}

	public String getName() {
		return name;
	}
	public Block getParent() {
		return parent;
	}

	

	public void print(int indent) {
		IOUtils.printIndented(indent, this.getClass().getSimpleName() 
							+ ": name=" + name);
	}

	public void setArguments(ArrayList<Expression> argumentsList) {
		this.argumentsList = argumentsList;
	}
	public ArrayList<Expression> getArguments() {
		return argumentsList;
	}
	
	
	
	@Override
	public String valueToString() {
		String s = "";
		for (Expression e : argumentsList) {
			s += "[" + e.toStringInBracket() + "]";
		}
		return name + s;
	}
	
	private int verifyDataType(Variable variable) throws BugTrap {
		if (variable.getValue() instanceof Array) return IsArray;
		else if (variable.getValue() instanceof compiler.data.String) return IsString;
		else
			throw new BugTrap(variable, "Array or String expected.");
	}

	public String getDataType(Context context) throws BugTrap {
		Variable variable = context.getVariable(name);
		verifyDataType(variable);
		return variable.getValue().getType();
	}
	
	public Variable getVariable(Context context) throws BugTrap {
		Literal literal = context.getDeclared(name);
		if (literal instanceof Constant) {
			throw new BugTrap(context, name + " is not a variable");
		}
		else if (literal instanceof Variable) {
			return ((Variable) literal);
		}
		throw new BugTrap(context, name + " is a bad symbol");
	}
	
	public Constant getConstant(Context context) throws BugTrap {
		Literal literal = context.getDeclared(name);
		if (literal instanceof Constant) {
			return ((Constant) literal);
		}
		else if (literal instanceof Variable) {
			throw new BugTrap(context, name + "is not a constant");
		}
		throw new BugTrap(context, name + "is a bad symbol");
	}
	public int[] getIndices(Context context) throws BugTrap {
		int indices[] = new int[argumentsList.size()];
		for (int i=0; i<indices.length; i++) {
			DataType value = (DataType) argumentsList.get(i).evaluate(context);
			if (!(value instanceof compiler.data.Integer)) throw new BugTrap(context, "Array has bad index.");
			indices[i] = ((Integer) value).getValue();
		}
		return indices;
	}
	
	public void setValue(Context context, DataType newValue) throws BugTrap {
		Variable variable = context.getVariable(name);
		
		// DataType can only be array or string. All other types have been rejected by the verification.
		if (verifyDataType(variable)==IsArray) {
			Array array = (Array) variable.getValue();
			int indices[] = getIndices(context);
			array.setElement(newValue, indices);
			
			if (Debug) {
				String s = indicesToString(indices);
				IOUtils.println(getClass().getSimpleName()+".setValue on " + variable.getName() + " indices: " + s);
				if (variable instanceof VariableRef) {
					IOUtils.println(variable.getName() + " is a VariableRef ");
				}
			}
		}
		else {
			compiler.data.String string = (compiler.data.String) variable.getValue();
			int indices[] = getIndices(context);
			if (indices.length>1) throw new BugTrap(context, "String index should have only 1 dimension.");
			string.setElement(newValue.toCharacter(), indices[0]);
			
			if (Debug) {
				String s = indicesToString(indices);
				IOUtils.println(getClass().getSimpleName()+".setValue on " + variable.getName() + " indices: " + s);
				if (variable instanceof VariableRef) {
					IOUtils.println(variable.getName() + " is a VariableRef ");
				}
			}
		}

	}

	public String indicesToString(int indices[]) {
		String s = "";
		for (int i=0; i<indices.length; i++) {
			s+="[" + indices[i] + "]";
		}
		return s;
	}
	
	@Override
	public DataType evaluate(Context context) throws BugTrap {
		// evaluate() is only to get the array/string element. 
		// To set the element use setValue().
		
		if (Debug) IOUtils.println(getClass().getSimpleName() + " to :" + name);
		Variable variable = context.getVariable(name);
		
		// DataType can only be array or string. All other types have been rejected by the verification.
		if (verifyDataType(variable)==IsArray) {
			Array array = (Array) variable.getValue();
			int indices[] = getIndices(context);
			if (Debug) {
				String s = indicesToString(indices);
				IOUtils.println(getClass().getSimpleName() + ": " + name + s);
				IOUtils.println(getClass().getSimpleName() + ": " + name + s + " value is " + array.getElement(indices).valueToString());
			}
			return array.getElement(indices);			
		}
		else {
			compiler.data.String string = (compiler.data.String) variable.getValue();
			int indices[] = getIndices(context);
			if (indices.length>1) throw new BugTrap(context, "String index should have only 1 dimension.");
			if (Debug) {
				String s = indicesToString(indices);
				IOUtils.println(getClass().getSimpleName() +": " + name + s);
				IOUtils.println(getClass().getSimpleName() +": " + name + s + " value is " + string.getElement(indices[0]).valueToString());
			}
			return string.getElement(indices[0]);
		}

	}


}
