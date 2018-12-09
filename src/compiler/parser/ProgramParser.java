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

import java.util.ArrayList;

import compiler.blocks.Program;
import compiler.util.BugTrap;

public abstract class ProgramParser {
	abstract public void preprocess() throws BugTrap;
	abstract public void compile() throws BugTrap;
	abstract public Cmds getProgramCmds();
	abstract public Program getProgram() throws BugTrap;
	abstract public String getProgramName();
	abstract public void preprocessAndCompile() throws BugTrap;
	abstract public ArrayList<Cmds> getClassCmds();
}
