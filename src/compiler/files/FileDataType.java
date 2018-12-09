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

import compiler.data.Character;
import compiler.data.DataType;
import compiler.data.Integer;
import compiler.data.Real;
import compiler.main.Settings;
import compiler.tokenizers.Token;
import compiler.util.BugTrap;

public abstract class FileDataType extends DataType {
	
	protected static final String SingleQuote = Token.SingleQuote;
	protected static final String DoubleQuote = Token.DoubleQuote;
	protected static final String Delimiter = "\n";
	
	
	protected String getDefaultDataFileDirectory() {
		return Settings.getDataFileDirectory();
	}

	// Once an object is declared of this type, it must NOT be primitive.
	public boolean isPrimitive() {
		return false;
	}
	// Once an object is declared of this type, it must be file.
	public boolean isFile() {
		return true;
	}
	// Once an object is declared of this type, it must NOT be aggregate
	public boolean isAggregate() {
		return false;
	}

	public abstract void setMode(java.lang.String mode) throws BugTrap;
	public abstract void open(compiler.data.String filename) throws BugTrap;
	public abstract void close() throws BugTrap;


	public static FileDataType parse(java.lang.String type) throws BugTrap {
		if (compiler.files.InputFile.matchesType(type)) {
			return new compiler.files.InputFile();
		}
		else if (compiler.files.OutputFile.matchesType(type)) {
			return new compiler.files.OutputFile();
		}
		throw new BugTrap(type + ": type not recognized!");
	}
	public static DataType parse(java.lang.String type, java.lang.String value) throws BugTrap {
		if (compiler.files.InputFile.matchesType(type)) {
			return new compiler.files.InputFile(value);
		}
		else if (compiler.files.OutputFile.matchesType(type)) {
			return new compiler.files.OutputFile(value);
		}
		throw new BugTrap(type + ": type not recognized!");
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
	public compiler.data.String toDString() throws BugTrap {
		throw new BugTrap(this, "not convertible to String");
	}

	@Override
	public Character toCharacter() throws BugTrap {
		throw new BugTrap(this, "not convertible to Character");
	}
}
