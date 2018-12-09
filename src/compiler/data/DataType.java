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

import compiler.binder.Context;
import compiler.files.FileDataType;
import compiler.literal.ValueType;
import compiler.util.BugTrap;

public abstract class DataType extends ValueType {
	
	public static final java.lang.String StartKeyword = DataType.class.getName();

	public abstract void print(int indent);
	public abstract java.lang.String getType();

	public abstract boolean isPrimitive();
	public abstract boolean isAggregate();
	public abstract boolean isFile();
	
	public static boolean isDataType(java.lang.String name) {
		if (isPrimitive(name)) return true;
		else if (isFile(name)) return true;
		return false;
	}


	// Check if any unknown type is a primitive type. Useful for parsing when no object is yet created.
	public static boolean isPrimitive(java.lang.String name) {
		if (compiler.data.Integer.matchesType(name)) return true;
		else if (compiler.data.Real.matchesType(name)) return true;
		else if (compiler.data.String.matchesType(name)) return true;
		else if (compiler.data.Character.matchesType(name)) return true;
		else if (compiler.data.Boolean.matchesType(name)) return true;
		return false;
	}
	
	// Check if any unknown type is a primitive type. Useful for parsing when no object is yet created.
	public static boolean isFile(java.lang.String name) {
		if (compiler.files.InputFile.matchesType(name)) return true;
		else if (compiler.files.OutputFile.matchesType(name)) return true;
		return false;
	}

	public static DataType parse(java.lang.String type) throws BugTrap {
		if (isPrimitive(type))
			return PrimitiveDataType.parse(type);
		else if (isFile(type))
			return FileDataType.parse(type);
		else
			return AggregateDataType.parse(type);
	}

	
	public static DataType parse(java.lang.String type, java.lang.String value) throws BugTrap {
		if (isPrimitive(type))
			return PrimitiveDataType.parse(type, value);
		else if (isFile(type))
			return FileDataType.parse(type, value);
		else
			return AggregateDataType.parse(type, value);
	}
	
	
	// Currently values only include the primitive data types.
	// Files and arrays are initialized using the primitive data types.
	public static DataType parseValue(java.lang.String value) throws BugTrap {
		return PrimitiveDataType.parseValue(value);			
	}
	
	
	// All data types evaluate to themselves.
	@Override
	public DataType evaluate(Context context) throws BugTrap {
		return this;
	}


	public abstract compiler.data.Real toReal() throws BugTrap;
	public abstract compiler.data.Integer toInteger() throws BugTrap;
	public abstract compiler.data.String toDString() throws BugTrap;
	public abstract compiler.data.Character toCharacter() throws BugTrap;
	public abstract DataType copy() throws BugTrap;
}
