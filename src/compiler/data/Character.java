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

import compiler.util.BugTrap;
import compiler.util.IOUtils;

public class Character extends PrimitiveDataType {
	public static final java.lang.String StartKeyword = "Character";

	char value;
	public Character() {
	}
	public Character(char value) {
		this.value = value;
	}
	public char getValue() {
		return value;
	}
	public java.lang.String getType() {
		return StartKeyword;
	}	
	public static boolean matchesType(java.lang.String name) {
		return (name.equals(StartKeyword));
	}
	
	public void print(int indent) {
		IOUtils.printIndented(indent, "DataType: " + " value=" + value);
	}

	@Override
	public boolean equals(Object object) {
		if (!(object instanceof DataType)) return false;
		DataType value2 = (DataType) object;
		if (this.getType()!=value2.getType()) return false;
		if (this.getValue() != ((compiler.data.Character) value2).getValue()) return false;
		return true;
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
	public String toDString() {
		return new compiler.data.String("" + value);
	}
	@Override
	public Character toCharacter() {
		return new compiler.data.Character(value);
	}
	@Override
	public DataType copy() {
		return new compiler.data.Character(value);
	}
}
