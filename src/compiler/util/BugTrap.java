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
package compiler.util;

import java.io.PrintWriter;
import java.io.StringWriter;

import compiler.binder.Constant;
import compiler.binder.Context;
import compiler.binder.Variable;
import compiler.blocks.Block;
import compiler.blocks.Function;
import compiler.blocks.Method;
import compiler.blocks.Module;
import compiler.classes.ClassBlock;
import compiler.classes.ClassObject;
import compiler.commands.Command;
import compiler.data.DataType;
import compiler.expression.Operator;
import compiler.literal.ValueType;
import compiler.main.Settings;
import compiler.parser.Cmds;

public class BugTrap extends Exception {

	private static final long serialVersionUID = -3485920708282277088L;
	
	private static Cmds cmds = null;
	
	String details;

	public static void setCmds(Cmds cmds) {
		BugTrap.cmds = cmds;
	}
	
	public String getDetails() {
		return details;
	}

	public String translate(int lineNumber) {
		if (cmds==null) return "" + lineNumber;
		else return cmds.matchRawlineNumbersAsString(lineNumber);
	}

	public String getLines(Block block) {
		int startLineNumber = block.getStartLineNumber();
		int endLineNumber = block.getEndLineNumber()-1;  // exclusive
		
		String start = translate(startLineNumber);
		String end = translate(endLineNumber);
				
		if (startLineNumber==endLineNumber-1) 
			return "\nLine " + start;
		else
			return "\nLines " + start + " to " + end;
	}
	
	
	
	
	public BugTrap(java.lang.String string) {
		details = string;
	}

	public BugTrap(Block block, java.lang.String string) {
		details = "In block :" + getLines(block) + " " + string;
	}
	
	public BugTrap(Block block, String line, java.lang.String string) {
		details = "In block :" + getLines(block) + " {" + line + "} " + string;
	}


	public BugTrap(Function block, java.lang.String string) {
		details = "In " + block.getClass().getSimpleName() + " " + block.getName() + ": " + getLines(block) + " " + string;
	}

	public BugTrap(Module block, java.lang.String string) {
		details = "In " + block.getClass().getSimpleName() + " " + block.getName() + ": " + getLines(block) + " " + string;
	}
	
	public BugTrap(Command block, java.lang.String string) {
		details = "In " + block.getClass().getSimpleName() + ": " + getLines(block) + " " + string;
	}

	public BugTrap(int startLineNumber, int endLineNumber, java.lang.String string) {
		details = "In " + startLineNumber + " to " + endLineNumber + " " + string;
	}
	
	public BugTrap(DataType type, java.lang.String string) {
		details = "In " + type.getClass().getSimpleName()  + " " + type.getType() + ": " + string;
	}
	
	public BugTrap(ValueType type, java.lang.String string) {
		details = "In " + type.getClass().getSimpleName()  + " " + type.getType() + ": " + string;
	}

	public BugTrap(Context context, java.lang.String string) {
		Block block = context.getBlock();
		String blockType = block.getClass().getSimpleName();
		String name = "";
		if (block instanceof ClassBlock) name = ((ClassBlock)block).getName();
		else if (block instanceof Method) name = ((Method)block).getName();
		
		details = "In the context of " + blockType + " " + name + ": " + getLines(block) + " " + string;
	}

	public BugTrap(Context context, Object obj, java.lang.String string) {
		Block block = context.getBlock();
		String blockType = block.getClass().getSimpleName();
		String name = "";
		if (block instanceof ClassBlock) name = ((ClassBlock)block).getName();
		else if (block instanceof Method) name = ((Method)block).getName();
		
		details = obj.getClass().getSimpleName() + " in the context of " + blockType + " " + name + ": " + getLines(block) + " " + string;
	}


	
	public BugTrap(Operator operator, java.lang.String string) {
		details = "At operator " + operator.getKeyword() + ":" + string;
	}
	
	public BugTrap(Constant literal, java.lang.String string) {
		details = "At " + literal.getClass().getSimpleName() + " " + literal.getName() + ":" + string;
	}

	public BugTrap(Variable literal, java.lang.String string) {
		details = "At " + literal.getClass().getSimpleName() + " " + literal.getName() + ":" + string;
	}
	public BugTrap(ClassObject literal, java.lang.String string) {
		details = "At " + literal.getClass().getSimpleName() + " " + literal.getName() + ":" + string;
	}

	public BugTrap(java.lang.Exception e) {
		details = e.getMessage();
		setStackTrace(e.getStackTrace());
	}
	
	

	public void printStackTrace() {
		StringWriter sw = new StringWriter();
		super.printStackTrace(new PrintWriter(sw));
		Settings.GetErrstream().println(sw.toString());
	}

	public static void printStackTrace(Exception e) {
		new BugTrap(e).printStackTrace();
	}
}
