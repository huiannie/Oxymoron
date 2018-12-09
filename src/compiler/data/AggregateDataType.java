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

import compiler.files.FileDataType;
import compiler.util.BugTrap;

public abstract class AggregateDataType extends DataType {
	
	
	// Once an object is declared of this type, it must be NOT primitive.
	public boolean isPrimitive() {
		return false;
	}
	// Once an object is declared of this type, it must NOT be file.
	public boolean isFile() {
		return false;
	}
	// Once an object is declared of this type, it must be aggregate
	public boolean isAggregate() {
		return true;
	}

	// Aggregate data type (currently only array) is a group of other data types.
	public static DataType parse(java.lang.String type) throws BugTrap {
		if (isPrimitive(type))
			return PrimitiveDataType.parse(type);
		else if (isFile(type))
			return FileDataType.parse(type);
		throw new BugTrap(type + ": type not recognized!");
	}
	
	// Aggregate data type (currently only array) is a group of other data types.
	public static DataType parse(java.lang.String type, java.lang.String value) throws BugTrap {
		if (isPrimitive(type))
			return PrimitiveDataType.parse(type, value);
		else if (isFile(type))
			return FileDataType.parse(type, value);
		throw new BugTrap(type + ": type not recognized!");
	}
	
	public static AggregateDataType parseValue(java.lang.String value) throws BugTrap {
		throw new BugTrap("Aggregate data values are supposed to by primitive or file data types.");
	}
	
	public java.lang.String valueToString() {
		return "Undone!";
	}

}
