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
package compiler.parser;

import java.io.File;

import compiler.blocks.Program;

public class ProgramCompilation extends Compilation {
	private File programFile;
	private Program program;
	private Scope scope;
	public ProgramCompilation(File programFile, Program program, Scope scope) {
		this.programFile = programFile;
		this.program = program;
		this.scope = scope;
	}
	@Override
	public Cmds getCmds() {
		return scope.getCmds();
	}
	@Override
	public File getSourceFile() {
		return programFile;
	}
	@Override
	public Program getProgram() {
		return program;
	}
	@Override
	public boolean isClass() {
		return false;
	}
	@Override
	public boolean isProgram() {
		return true;
	}
}
