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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import compiler.data.DataType;
import compiler.main.Settings;
import compiler.util.BugTrap;

public class OutputFile extends FileDataType {
	public static final boolean Debug = Settings.Debug;

	public static final java.lang.String StartKeyword = "OutputFile";
	public static final java.lang.String ModeKeyword = "AppendMode";

	java.lang.String pathName = null;
	java.lang.String mode = null;

	BufferedWriter value = null;
	public OutputFile() {
	}
	public OutputFile(java.lang.String pathName) {
		this.pathName = pathName;
	}
	public OutputFile(compiler.data.String pathName) {
		this.pathName = pathName.getValue();
	}
	public BufferedWriter getValue() {
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
		if (mode.equals(ModeKeyword)) {
			this.mode = ModeKeyword;
		}
		else 
			new BugTrap(this, "cannot set mode to " + mode);
	}
	
	public void open() throws BugTrap {
		try {
			String defaultPathName = pathName;
			File defaultDirectory = new File(getDefaultDataFileDirectory());
			if (defaultDirectory.exists() && defaultDirectory.isDirectory())
				defaultPathName = getDefaultDataFileDirectory() + pathName;
			
			if (mode==ModeKeyword) {
				// append mode 
				value = new BufferedWriter( new FileWriter(defaultPathName, true));
			}
			else  // write mode only
				value = new BufferedWriter( new FileWriter(defaultPathName, false));
		} catch (IOException e) {
			throw new BugTrap(e);
		}
	}
	public void open(compiler.data.String pathName) throws BugTrap {
		this.pathName = pathName.getValue();
		this.open();
	}
	public void close() throws BugTrap {
		try {
			if (value!=null)
				value.close();
		} catch (IOException e) {
			throw new BugTrap(e);
		}
	}


	public void write(compiler.data.Boolean dataValue) throws BugTrap {
		try {
			value.write(dataValue.valueToString());
			value.write(Delimiter);
		} catch (IOException e) {
			throw new BugTrap(e);
		}
	}
	public void write(compiler.data.Integer dataValue) throws BugTrap {
		try {
			value.write(dataValue.valueToString());
			value.write(Delimiter);
		} catch (IOException e) {
			throw new BugTrap(e);
		}
	}
	public void write(compiler.data.Real dataValue) throws BugTrap {
		try {
			value.write(dataValue.valueToString());
			value.write(Delimiter);
		} catch (IOException e) {
			throw new BugTrap(e);
		}
	}
	public void write(compiler.data.Character dataValue) throws BugTrap {
		try {
			value.write(SingleQuote + dataValue.valueToString() + SingleQuote);
			value.write(Delimiter);
		} catch (IOException e) {
			throw new BugTrap(e);
		}
	}
	public void write(compiler.data.String dataValue) throws BugTrap {
		try {
			value.write(DoubleQuote + dataValue.valueToString() + DoubleQuote);
			value.write(Delimiter);
		} catch (IOException e) {
			throw new BugTrap(e);
		}
	}
	public void write(DataType dataValue) throws BugTrap {
		if (compiler.data.Boolean.matchesType(dataValue.getType())) write((compiler.data.Boolean) dataValue);
		else if (compiler.data.Integer.matchesType(dataValue.getType())) write((compiler.data.Integer) dataValue);
		else if (compiler.data.Real.matchesType(dataValue.getType())) write((compiler.data.Real) dataValue);
		else if (compiler.data.Character.matchesType(dataValue.getType())) write((compiler.data.Character) dataValue);
		else if (compiler.data.String.matchesType(dataValue.getType())) write((compiler.data.String) dataValue);
		else throw new BugTrap(this, dataValue.getType() + " cannot be written to file.");
	}


	@Override
	public void print(int indent) {
		// TODO Auto-generated method stub
	}



	@Override
	public DataType copy() throws BugTrap {
		//Note: File handle is not duplicated when copied.
		OutputFile file = new OutputFile(this.pathName);
		file.value = this.value;
		return file;
	}

	@Override
	public String valueToString() {
		return pathName;
	}
}
