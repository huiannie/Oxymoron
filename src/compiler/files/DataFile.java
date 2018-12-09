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

import compiler.data.DataType;
import compiler.main.Settings;
import compiler.util.BugTrap;

public class DataFile extends FileDataType {
	public static final boolean Debug = Settings.Debug;

	public static final java.lang.String StartKeyword = "DataFile";


	java.lang.String pathName = null;
	File value;

	public DataFile(java.lang.String pathName) throws BugTrap {
		if (pathName==null || pathName.length()==0 || pathName.startsWith("."))
			throw new BugTrap(this, "Bad file name.");
		this.pathName = pathName;
		value = new File(addDefaultDirectory(pathName));
	}
	public DataFile(compiler.data.String pathName) throws BugTrap {
		this(pathName.getValue());
	}
	
	public java.lang.String addDefaultDirectory(java.lang.String pathName) {
		String defaultPathName = pathName;
		File defaultDirectory = new File(getDefaultDataFileDirectory());
		if (defaultDirectory.exists() && defaultDirectory.isDirectory())
			defaultPathName = getDefaultDataFileDirectory() + pathName;		
		return defaultPathName;
	}
	
	public File getValue() {
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

	@Override
	public void print(int indent) {
		// TODO Auto-generated method stub
	}

	@Override
	public DataType copy() throws BugTrap {
		return new DataFile(this.pathName);
	}


	@Override
	public String valueToString() {
		return pathName;
	}
	@Override
	public void open(compiler.data.String filename) throws BugTrap {
		throw new BugTrap(this, "Open not supported.");
	}
	@Override
	public void close() throws BugTrap {
		throw new BugTrap(this, "Close not supported.");
	}

	public void delete() {
		if (value.exists()) {
			value.delete();
		}
	}

	public void rename(DataFile destFile) {
		if (value.exists()) {
			if (destFile.getValue().exists()) {
				destFile.getValue().delete();
			}
			value.renameTo(destFile.getValue());
		}
	}
}
