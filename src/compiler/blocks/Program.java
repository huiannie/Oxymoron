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
package compiler.blocks;

import java.io.InputStream;
import java.util.ArrayList;

import compiler.binder.Constant;
import compiler.binder.Context;
import compiler.classes.ClassBlock;
import compiler.classes.ClassBlocksMap;
import compiler.commands.Input;
import compiler.declarations.Declaration;
import compiler.lib.LibrariesBuilder;
import compiler.main.Settings;
import compiler.parser.MethodsMap;
import compiler.parser.Scope;
import compiler.util.BugTrap;
import compiler.util.IOUtils;
import gui.GuiInputPipe;

public class Program extends Block {

	public static final boolean Debug = Settings.Debug;

	private static final String Main = "main";

	String filename;
	
	MethodsMap methodsMap = new MethodsMap();
	ClassBlocksMap classesMap = new ClassBlocksMap();

	public Program(int startLineNumber, int endLineNumber, String filename, ClassBlocksMap classesMap) {
		super(startLineNumber, endLineNumber);
		selfContained = true;
		
		this.filename = filename;
		
		// If there are classes associated to this program, 
		// then make sure to attach this program as the parent of each of them.
		if (classesMap!=null) {
			this.classesMap = classesMap;
			for (String className : this.classesMap.keyset()) {
				ClassBlock classBlock = this.classesMap.get(className);
				classBlock.parent = this;
			}
		}
	}
	
	public String getFilename() {
		return filename;
	}
	
	public Method getMethod(String name) {
		return methodsMap.get(name);
	}
	
	public ClassBlock getClassBlock(String name) {
		return classesMap.get(name);
	}
	public boolean hasClassBlock(String name) {
		return classesMap.containsKey(name);
	}
	public boolean hasClassType(String name) {
		return classesMap.containsKey(name);
	}
	
	public void parse(ArrayList<String> code, Scope scope, Block parent) throws BugTrap {
		super.parse(code, scope, parent);

		for (Block subblock : subblocks) {
			if (subblock instanceof Method) {
				Method method = (Method) subblock;
				if (methodsMap.containsKey(method.getName())) 
					throw new BugTrap(this, "Method " + method.getName() + " already exists.");
				methodsMap.put(method.getName(), method);
			}
		}

	}
	
	public void run(GuiInputPipe pipe) throws BugTrap {
		Input.SetInputPipe(pipe);
		run();
		Input.SetInputPipe(null);
	}
	public void run(InputStream instream) throws BugTrap {
		Input.SetInputStream(instream);
		run();
		Input.SetInputStream(null);
	}
	
	private void run() throws BugTrap {
		try {
			Context programContext = new Context(this, null);

			if (Debug) IOUtils.println("****************** Binding globals *************************");
			// Add global constants
			for (String key : Constant.globals.keyset()) {
				programContext.bind(Constant.globals.get(key));
			}

			if (Debug) IOUtils.println("****************** Loading libraries *************************");
			LibrariesBuilder.load(methodsMap, this);
			
			if (Debug) methodsMap.print(4);
			if (Debug) IOUtils.println("****************** Starting running *************************");
			
			if (methodsMap.containsKey(Main)) {
				// Check for globals first.
				for (Block subblock : subblocks) {
					if (subblock instanceof Declaration) {
						subblock.execute(programContext);
					}
				}
				
				Module module = (Module) methodsMap.get(Main);
				Context subContext = new Context(module, programContext);
				module.execute(subContext);
			}
			else {
				super.execute(programContext);
			}
		}
		// If some exceptions are not caught while running the program,
		// then catch them here and wrap them up as bug trap.
		catch (Exception e) {
			if (e instanceof BugTrap) throw e; 
			else throw new BugTrap(e);
		}	
	}
}
