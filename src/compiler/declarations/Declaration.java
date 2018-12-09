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
package compiler.declarations;

import java.util.ArrayList;

import compiler.binder.Context;
import compiler.blocks.Block;
import compiler.classes.ClassFieldDeclaration;
import compiler.classes.ClassObjectDeclaration;
import compiler.classes.ConstructorCall;
import compiler.literal.Literal;
import compiler.main.Settings;
import compiler.parser.Scope;
import compiler.tokenizers.LineMatcher;
import compiler.util.BugTrap;
import compiler.util.IOUtils;

public class Declaration extends Block {
	public static final boolean Debug = Settings.Debug;

	
	ArrayList<Literal> literals = new ArrayList<Literal>();
	
	public Declaration(int lineNumber) {
		super(lineNumber, lineNumber+1);
	}
	
	
	public static boolean startsWithAKeyword(String line) {
		if (LineMatcher.matchStart(line, ConstructorCall.StartKeyword)) return true;
		else if (LineMatcher.matchStart(line, ConstantDeclaration.StartKeyword)) return true;
		else if (LineMatcher.matchStart(line, VariableDeclaration.StartKeyword)) return true;
		return false;
	}

	
	public static boolean isADeclaration(String line) {
		if (ConstantDeclaration.isAConstant(line)) return true;
		if (VariableDeclaration.isADeclare(line)) return true;
		if (ClassObjectDeclaration.isAClassObject(line)) return true;
		if (ClassFieldDeclaration.isAClassField(line)) return true;
		return false;
	}
	
	public void print(int indent) {
		IOUtils.printIndented(indent, this.getClass().getSimpleName() 
								+ " range: " + startLineNumber + "-" + endLineNumber);
		if (!literals.isEmpty()) {
			IOUtils.printIndented(indent, "List of declared literals:");
			for (Literal l : literals) {
				l.print(indent+2);
			}
			IOUtils.printIndented(indent, "End of list of declared literals");
		}		
	}
	
	public ArrayList<Literal> getDeclarations() {
		return literals;
	}


	public void parse(ArrayList<String> code, Scope scope, Block parent) throws BugTrap {
		if (this.parent==null) this.parent = parent;

		int lineNumber = getStartLineNumber();
		String line = code.get(lineNumber);
		
		if (ConstantDeclaration.isAConstant(line)) {
			literals.add(ConstantDeclaration.parse(line, scope, parent));
		}
		else if (VariableDeclaration.isADeclare(line)) {
			literals = VariableDeclaration.parse(line, scope, parent);
		}
		else if (ClassObjectDeclaration.isAClassObject(line)) {
			literals = ClassObjectDeclaration.parse(line, scope, parent);
		}
		else if (ClassFieldDeclaration.isAClassField(line)) {
			literals = ClassFieldDeclaration.parse(line, scope, parent);
		}
		else 
			throw new BugTrap(this, line + " is not a declaration.");

	}
	
	
	@Override
	public int execute(Context context) throws BugTrap {
		for (Literal literal : literals) {
			literal.evaluate(context);
		}
		return ExitCode_OK;
	}
}
