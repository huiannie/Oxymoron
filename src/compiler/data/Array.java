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

import java.util.ArrayList;

import compiler.binder.Constant;
import compiler.binder.Context;
import compiler.expression.Expression;
import compiler.main.Settings;
import compiler.tokenizers.LineTokenizer;
import compiler.util.BugTrap;
import compiler.util.IOUtils;

public class Array extends AggregateDataType {
	public static final boolean Debug = Settings.Debug;
	
	public static final java.lang.String StartKeyword = "Array";

	public static final java.lang.String SquareOpen = "[";
	public static final java.lang.String SquareClose = "]";

	DataType type;
	Object values[];
	int dimension[];
	
	
	public Array(DataType type) {
		this.type = type;
		this.values = null;
		this.dimension = new int[0];
		// Create a dummy empty array.
		// This is only used in parameter declaration.
	}

	
	private Array(DataType type, int dimension[]) throws BugTrap {
		this.type = type;
		this.dimension = dimension;
		
		values = allocate(this.dimension, 0);
		
		if (Debug) {
			IOUtils.println("Array Type=" + type.getType());
			for (int i=0; i<dimension.length; i++) {
				IOUtils.print("[" + dimension[i] + "]");
			}
			IOUtils.println();
		}
	}
	
	private Object[] allocate(int[] indices, int i) throws BugTrap {
		if (i==indices.length-1) {
			DataType[] level0 = new DataType[indices[i]];
			for (int j=0; j<level0.length; j++)
				level0[j] = type.copy();
			return level0;
		}
		else {
			Object[] ptr = new Object[indices[i]];
			for (int p=0; p<indices[i]; p++) ptr[p] = allocate(indices, i+1);
			return ptr;
		}
	}
	
	private Object[] allocateAndCopy(int[] indices, int i, Object[] source) {
		if (i==indices.length-1) {
			DataType[] level0 = new DataType[indices[i]];
			for (int j=0; j<level0.length; j++)
				level0[j] = (DataType) source[j];
			return level0;
		}
		else {
			Object[] ptr = new Object[indices[i]];
			for (int p=0; p<indices[i]; p++) ptr[p] = allocateAndCopy(indices, i+1, source);
			return ptr;
		}
	}


	
	public void print(int indent) {
		IOUtils.printIndented(indent, "Array not ready to print");
	}

	
	
	
	
	
	public java.lang.String getType() {
		return type.getType();
	}	
	public static boolean matchesType(java.lang.String name) {
		return (name.equals(StartKeyword));
	}
	
	public int getDimensions() {
		return dimension.length;
	}

	public static java.lang.String indicesToString(int indices[]) {
		java.lang.String s = "";
		for (int i=0; i<indices.length; i++) s += Array.SquareOpen + indices[i] + Array.SquareClose;
		return s;
	}

	@Override
	public java.lang.String valueToString() {
		java.lang.String brackets = indicesToString(dimension);
		return type.getType() + brackets;
	}
	
	public DataType cast(DataType newValue) throws BugTrap {
		// Check compatibility
		if (this.type.getType()==newValue.getType()) {
			return newValue;
		}
		else if (this.type instanceof compiler.data.Integer) {
			return newValue.toInteger();
		}
		else if (this.type instanceof compiler.data.Real) {
			return newValue.toReal();
		}
		else if (this.type instanceof compiler.data.Character) {
			return newValue.toCharacter();
		}
		else if (this.type instanceof compiler.data.String) {
			return newValue.toDString();
		}
		else {
			throw new BugTrap(this, newValue + " not convertible to " + this.type.getType());
		}
	}
	
	
	public void setElement(DataType newValue, int[] indices) throws BugTrap {
		int dim = indices.length;
		Object ptr = values;
		for (int index=0; index<dim-1; index++) {
			ptr = ((Object[]) ptr)[indices[index]];
		}
		// Check compatibility before assignment.
		((Object[]) ptr)[indices[dim-1]] = cast(newValue);
	}

	public DataType getElement(int[] indices) {
		int dim = indices.length;
		Object ptr = values;
		for (int index=0; index<dim-1; index++) {
			ptr = ((Object[]) ptr)[indices[index]];
		}
		return (DataType) ((Object[]) ptr)[indices[dim-1]];
	}
	
	
	
	
	public static Array parse(java.lang.String dataTypeString, java.lang.String nameAndDimension, 
			ArrayList<Expression> initialValues, Context context) throws BugTrap {

		LineTokenizer tokenizer = new LineTokenizer(nameAndDimension);
		if (!tokenizer.isArrayName()) throw new BugTrap(context, nameAndDimension + "bad syntax.");

		// Array name is of the form: name[A][B]...
		// Remove the name. Focuse on the dimension.
		tokenizer.parseName();
		
		DataType type = DataType.parse(dataTypeString);
		
		ArrayList<java.lang.String> dimensionStrings = new ArrayList<java.lang.String>();
		while (tokenizer.getRemainder().length()>0) {
			dimensionStrings.add(tokenizer.parseArrayIndexWithoutBracket());
		}
		// For now, require all variable declarations to use very simple indices.
		// So the dimension values can only be constants or integers.
		// Any other more complex expressions (such as N+1) will not be accepted.
		
		int dimension[] = new int[dimensionStrings.size()];
		for (int index=0; index<dimension.length; index++) {
			LineTokenizer tokenizer2 = new LineTokenizer(dimensionStrings.get(index));
			if (tokenizer2.isPositiveInteger()) {
				dimension[index] = java.lang.Integer.parseInt(tokenizer2.parsePositiveInteger());
			}
			else if (tokenizer2.isName()) {
				java.lang.String constantName = tokenizer2.parseName();
				Constant constant = context.getConstant(constantName);
				if (constant.getValue() instanceof compiler.data.Integer)
				dimension[index] = ((compiler.data.Integer) constant.getValue()).getValue();
			}
			else {
				throw new BugTrap(context, "Dimension must be either a positive integer or a declared constant.");
			}
		}

		Array array = new Array(type, dimension);

		if (initialValues!=null) {
			for (int i=0; i<initialValues.size(); i++) {
				int linear = i;
				int indices[] = new int[dimension.length];
				for (int j=0; j<indices.length; j++) {
					int base = 1;
					for (int k=j+1; k<indices.length; k++)
						base = base *dimension[k];
					
					indices[j] = linear / base;
					linear = linear % base;
				}
				if (Debug) {
					IOUtils.println("Element " + i + " is " + indicesToString(indices));
				}
				DataType newValue = (DataType) initialValues.get(i).evaluate(context);
				array.setElement(newValue, indices);
			}
		}
		return array;
	}

	@Override
	public Real toReal() throws BugTrap {
		throw new BugTrap(this, "not convertible to Real");
	}
	@Override
	public Integer toInteger() throws BugTrap {
		throw new BugTrap(this, "not convertible to Integer");
	}
	@Override
	public String toDString() throws BugTrap {
		throw new BugTrap(this, "not convertible to String");
	}
	@Override
	public Character toCharacter() throws BugTrap {
		throw new BugTrap(this, "not convertible to Character");
	}
	@Override
	public DataType copy() throws BugTrap {
		Array array = new Array(DataType.parse(type.getType()));
		array.dimension = new int[dimension.length];
		for (int i=0; i<dimension.length; i++) {
			array.dimension[i] = dimension[i];
		}
		array.values = allocateAndCopy(array.dimension, 0, values);
		return array;
	}
	
	
}
