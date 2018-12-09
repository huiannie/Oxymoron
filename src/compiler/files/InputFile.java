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
package compiler.files;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import compiler.data.DataType;
import compiler.main.Settings;
import compiler.tokenizers.Token;
import compiler.util.BugTrap;

public class InputFile extends FileDataType {
	public static final boolean Debug = Settings.Debug;

	public static final java.lang.String StartKeyword = "InputFile";


	java.lang.String pathName = null;
	Scanner value = null;
	public InputFile() {
	}
	public InputFile(java.lang.String pathName) {
		this.pathName = pathName;
	}
	public InputFile(compiler.data.String pathName) {
		this.pathName = pathName.getValue();
	}
	public Scanner getValue() {
		return value;
	}
	@Override
	public java.lang.String getType() {
		return StartKeyword;
	}	
	public static boolean matchesType(java.lang.String name) {
		return (name.equals(StartKeyword));
	}

	@Override
	public void setMode(String mode) throws BugTrap {
		throw new BugTrap(this, "cannot set mode to " + mode);
	}
	
	public void open() throws BugTrap {
		String defaultPathName = pathName;
		File defaultDirectory = new File(getDefaultDataFileDirectory());
		if (defaultDirectory.exists() && defaultDirectory.isDirectory())
			defaultPathName = getDefaultDataFileDirectory() + pathName;
		try {
			value = new Scanner(new File(defaultPathName));
		} catch (FileNotFoundException e) {
			throw new BugTrap(this, "File " + defaultPathName + " not found.");
		}
		value.useDelimiter(Delimiter);
	}
	

	public void open(compiler.data.String pathName) throws BugTrap {
		this.pathName = pathName.getValue();
		this.open();
	}
	
	public void close() throws BugTrap {
		if (value!=null) value.close();
	}

	public compiler.data.Boolean readBoolean() throws BugTrap {
		String pattern = Token.BooleanPattern;
		if (value.hasNext()) {
			java.lang.String dataValue = value.next(pattern);
			if (dataValue.equals(compiler.data.Boolean.True)) return new compiler.data.Boolean(true);
			else if (dataValue.equals(compiler.data.Boolean.False)) return new compiler.data.Boolean(false);
		}
		throw new BugTrap(this, "Cannot read Boolean data type.");
	}
	
	public compiler.data.Integer readInteger() throws BugTrap {
		if (value.hasNext()) {
			return new compiler.data.Integer(value.nextInt());
		}
		throw new BugTrap(this, "Cannot read Integer data type.");
	}
	public compiler.data.Real readReal() throws BugTrap {
		if (value.hasNext()) {
			return new compiler.data.Real(value.nextFloat());
		}
		throw new BugTrap(this, "Cannot read Real data type.");
	}
	
	public compiler.data.Character readCharacter() throws BugTrap {
		String pattern = Token.CharacterPattern;
		if (value.hasNext()) {
			java.lang.String dataValue = value.next(pattern);
			return new compiler.data.Character(dataValue.charAt(1));
		}		
		throw new BugTrap(this, "Cannot read Character data type.");
	}
	
	public compiler.data.String readString() throws BugTrap {
		String pattern = Token.StringPattern;
		if (value.hasNext()) {
			java.lang.String dataValue = value.next(pattern);
			return new compiler.data.String(dataValue.substring(1, dataValue.length()-1));
		}		
		throw new BugTrap(this, "Cannot read String data type.");
	}
	
	public DataType read(java.lang.String type) throws BugTrap {
		if (compiler.data.Boolean.matchesType(type)) return readBoolean();
		else if (compiler.data.Integer.matchesType(type)) return readInteger();
		else if (compiler.data.Real.matchesType(type)) return readReal();
		else if (compiler.data.Character.matchesType(type)) return readCharacter();
		else if (compiler.data.String.matchesType(type)) return readString();
		else throw new BugTrap(this, type + " cannot be written to file.");
	}

	
	public boolean hasNext() {
		return value.hasNext();
	}

	@Override
	public void print(int indent) {
		// TODO Auto-generated method stub
	}



	@Override
	public DataType copy() throws BugTrap {
		//Note: File handle is not duplicated when copied.
		InputFile file = new InputFile(this.pathName);
		file.value = this.value;
		return file;
	}

	@Override
	public String valueToString() {
		return pathName;
	}
	

}
