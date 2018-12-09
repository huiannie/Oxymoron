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
package compiler.classes;

import java.io.File;

import compiler.blocks.Program;
import compiler.parser.Cmds;
import compiler.parser.Compilation;

public class ClassCompilation extends Compilation {
	private File classFile;
	private ClassBlock classBlock;
	private ClassScope scope;
	public ClassCompilation(File classFile, ClassBlock classBlock, ClassScope scope) {
		this.classFile = classFile;
		this.classBlock = classBlock;
		this.scope = scope;
	}
	@Override
	public Cmds getCmds() {
		return scope.getCmds();
	}
	@Override
	public Program getProgram() {
		return (Program) classBlock.parent;
	}
	@Override
	public File getSourceFile() {
		return classFile;
	}
	@Override
	public boolean isClass() {
		return true;
	}
	@Override
	public boolean isProgram() {
		return false;
	}
}