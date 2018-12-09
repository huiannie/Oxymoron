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
package compiler.binder;

import compiler.blocks.Block;
import compiler.blocks.Function;
import compiler.blocks.Method;
import compiler.blocks.Module;
import compiler.blocks.Program;
import compiler.classes.ClassBlock;
import compiler.classes.ClassFieldsMap;
import compiler.classes.ClassObject;
import compiler.classes.ClassObjectsMap;
import compiler.classes.ClassType;
import compiler.data.DataType;
import compiler.literal.Literal;
import compiler.main.Settings;
import compiler.util.BugTrap;
import compiler.util.IOUtils;

public class Context {
	public static final boolean Debug = Settings.Debug;

	VariablesMap variablesMap = new VariablesMap();
	ConstantsMap constantsMap = new ConstantsMap();
	ClassObjectsMap classObjectsMap = new ClassObjectsMap();
	ClassFieldsMap classFieldsMap = null;

	Module startModule = null;
	private DataType functionReturnValue = null;

	
	Block block;
	Context parent;
	
	Context superclassContext = null;
	
	public Context(Block block, Context parent) {
		this.block = block;
		this.parent = parent;
		if (Debug) IOUtils.println("Context of " + block.getClass().getSimpleName());
	}


	
	public Module getStartModule() {
		return startModule;
	}
	
	public boolean isDeclaredGlobal(String name) {
		if (this.block instanceof Program || this.block instanceof ClassBlock) {
			if (constantsMap.containsKey(name)) return true;
			if (variablesMap.containsKey(name)) return true;			
			return false;
		}
		else if (parent!=null) return parent.isDeclaredGlobal(name);
		else return false;
	}
	
	public Literal getDeclaredGlobal(String name) {
		if (Debug) IOUtils.println("  Looking for global at " + block.getClass().getSimpleName());
		
		if (this.block instanceof Program || this.block instanceof ClassBlock) {
			if (Debug) { print(2); }
			if (constantsMap.containsKey(name)) return constantsMap.get(name);
			if (variablesMap.containsKey(name)) return variablesMap.get(name);			
			if (Debug) IOUtils.printIndented(2, "Symbol " + name + " is not a global in context of program ");
			return null;
		}
		else if (parent!=null) return parent.getDeclaredGlobal(name);
		return null;
	}
	
	public boolean isDeclared(String name) {
		// Check local variables and constants first. 
		if (constantsMap.containsKey(name)) return true;
		if (variablesMap.containsKey(name)) return true;
		// Recur upward to check parents
		if (parent!=null && !this.block.isSelfContained()) {
			return parent.isDeclared(name);
		}
		// If not found within local scope, then check globals.
		return isDeclaredGlobal(name);
	}
	
	public Literal getDeclared(String name) throws BugTrap {
		
		if (Debug) {
			IOUtils.printIndented(2, "Searching for symbol " + name + " in context of block " 
									+ block.getClass().getSimpleName() + " "
									+ ((block instanceof Method) ? ((Method) block).getName() : ""));
			print(2);
			if (constantsMap.containsKey(name) || variablesMap.containsKey(name)) 
				IOUtils.printIndented(3, name + " is found.");
			else
				IOUtils.printIndented(3, name + " is NOT found.");
		}
		
		// Check local variables and constants first. 
		if (constantsMap.containsKey(name)) return constantsMap.get(name);
		if (variablesMap.containsKey(name)) return variablesMap.get(name);
		// Recur upward to check parents
		if (parent!=null && !this.block.isSelfContained()) {
			if (Debug) { IOUtils.printIndented(2, "Search for parent context now."); }
			return parent.getDeclared(name);
		}
		// If not found within local scope, then check globals.
		Literal global = getDeclaredGlobal(name);
		return global;
	}


	// When a constant is created, bind it to the immediate context
	public void bind(Constant constant) throws BugTrap {
		String name = constant.getName();
		if (Debug) IOUtils.println("Context binding constant: " + name);
		if (constantsMap.containsKey(constant.getName())) throw new BugTrap(this, "Constant " + name + " already exists.");
		constantsMap.put(constant.getName(), constant);
	}

	// When a variable is created, bind it to the immediate context
	public void bind(Variable variable) throws BugTrap {
		String name = variable.getName();
		if (Debug) IOUtils.println("Context binding variable: " + name);
		if (variablesMap.containsKey(variable.getName())) throw new BugTrap(this, "Variable " + name + " already exists.");
		variablesMap.put(variable.getName(), variable);
	}
	

	public VariablesMap getVariablesMap() {
		return variablesMap;
	}
	public ConstantsMap getConstantsMap() {
		return constantsMap;
	}
	

	// Search for the closest context in which a variable has been declared under the given name.
	public Variable getVariable(String name) throws BugTrap {
		Literal literal = getDeclared(name);
		if (literal instanceof Variable) return (Variable) literal;
		throw new BugTrap(this, name + " not declared.");
	}

	// Search for the closest context in which a constant has been declared under the given name.
	public Constant getConstant(String name) throws BugTrap {
		Literal literal = getDeclared(name);
		if (literal instanceof Constant) return (Constant) literal;
		throw new BugTrap(this, name + " not declared.");
	}



	public ClassFieldsMap getClassFieldsMap() {
		return classFieldsMap;
	}
	public void setClassFieldsMap(ClassFieldsMap map) {
		this.classFieldsMap = map;
	}
	public Context getSuper() {
		return superclassContext;
	}
	public void setSuper(Context superclassContext) {
		this.superclassContext = superclassContext;
	}


	public Context getParent() {
		return parent;
	}

	public Method findMethodInEnclosingClass(String name) {
		if (block instanceof ClassBlock) 
			return ((ClassBlock) block).getMethod(name);
		else if (parent!=null)
			return parent.findMethodInEnclosingClass(name);
		else
			return null;
	}
	public Method findMethodInEnclosingProgram(String name) {
		if (block instanceof Program) 
			return ((Program) block).getMethod(name);
		else if (parent!=null)
			return parent.findMethodInEnclosingProgram(name);
		else
			return null;
	}
	
	
	public Context findOwnerProgramContext(Method method) {
		if (method.parent instanceof Program) {
			Program owner = (Program) method.parent;
			Context ownerContext = this;
			while (ownerContext!=null && ownerContext.getBlock()!=owner) {
				ownerContext = ownerContext.getParent();
			}
			return ownerContext;
		}
		return null;
	}


	public Context findOwnerClassContext(Method method) {
		if (method.parent instanceof ClassBlock) {
			ClassBlock owner = (ClassBlock) method.parent;
			Context ownerContext = this;
			while (ownerContext!=null && ownerContext.getBlock()!=owner) {
				if (ownerContext.getBlock() instanceof ClassBlock)
					ownerContext = ownerContext.getSuper();
				else
					ownerContext = ownerContext.getParent();
			}
			return ownerContext;
		}
		return null;
	}
	
	public Context findCallerClassContext(Method method) {
		Context callerContext = this;
		while (callerContext!=null && !(callerContext.getBlock() instanceof ClassBlock))
			callerContext = callerContext.getParent();
		return callerContext;
	}
	public boolean allowsAccessTo(Method method, Context callerContext) {
		// Assume that this context is the ownerContext of the method
		if (method.parent != this.getBlock()) {
			// Method is not defined under this block. So this context is not the owner.
			return false;
		}
		else if (!(this.getBlock() instanceof ClassBlock)) {
			// owner is not a class. So if the method can be found, it can be accessed.
			return true;
		}
		else { 
			// This is the owner. Now determine if the callerContext is allowed to access.
			
			// If the method is public, then yes.
			if (method.isPublic()) return true;
			else if (method.isProtected()) {
				if (Debug) IOUtils.println("Accessibility check: method " + method.getName() + " is protected.");
				Context callerClassContext = callerContext.findCallerClassContext(method);
				
				// Check if the caller is within a class extended from owner class.
				// If caller does not belong to any class, then not accessible.
				if (callerClassContext==null) return false; 
				else {
					ClassBlock ownerClass = (ClassBlock) this.getBlock();
					ClassBlock callerClass = (ClassBlock) callerClassContext.getBlock();
					if (Debug) IOUtils.println("Accessibility check: method " + method.getName() + " is called from " + callerClass.getName());
					return (callerClass==ownerClass || callerClass.isExtendedFrom(ownerClass));
				}
				
			}
			else if (method.isPrivate()) { // method is private.
				if (Debug) IOUtils.println("Accessibility check: method " + method.getName() + " is private.");
				Context callerClassContext = callerContext.findCallerClassContext(method);

				// Check if the caller is within the same class.
				// If caller does not belong to any class, then not accessible.
				if (callerClassContext==null) return false; 
				else return (this.getBlock()==callerClassContext.getBlock());
			}
			else {
				return true;
			}
		}
	}
	

	// Find the enclosing class and retrieve the method by the given name from that class.
	public Method getMethod(String name) throws BugTrap {
		Method method = findMethodInEnclosingClass(name);
		if (method!=null) return method;
		else {
			method = findMethodInEnclosingProgram(name);
			if (method!=null) return method;
		}
		throw new BugTrap(this, name + ": method not found");
	}




	public Context findEnclosingFunctionContext() throws BugTrap {
		if (block instanceof Function) return this;
		if (parent==null) throw new BugTrap(this, "Cannot find enclosing function");
		return parent.findEnclosingFunctionContext();
	}

	public Block getBlock() {
		return block;
	}


	public void print(int indent) {
		IOUtils.printIndented(indent, "Context of block " + block.getClass().getSimpleName() + ":");
		if (!constantsMap.keyset().isEmpty()) constantsMap.print(indent+1);
		if (!variablesMap.keyset().isEmpty()) variablesMap.print(indent+1);
		if (classFieldsMap!=null) classFieldsMap.print(indent+1);
		IOUtils.printIndented(indent, "End of context of block " + block.getClass().getSimpleName() + ".");
	}

	
	
	public void setReturnValue(DataType returnValue) {
		functionReturnValue = returnValue;
	}

	public DataType getReturnValue() {
		return functionReturnValue;
	}
	
	public ClassBlock findEnclosingClass() {
		if (block instanceof ClassBlock) 
			return (ClassBlock) block;
		else if (parent!=null)
			return parent.findEnclosingClass();
		else
			return null;
	}
	public Program findEnclosingProgram() {
		if (block instanceof Program) 
			return (Program) block;
		else if (parent!=null)
			return parent.findEnclosingProgram();
		else
			return null;
	}

	public ClassObject getClassObject(String name) {
		// Check local objects
		if (classObjectsMap.containsKey(name)) return classObjectsMap.get(name);
		// Recur upward to check parents
		if (parent!=null && !this.block.isSelfContained()) {
			if (Debug) { IOUtils.printIndented(2, "Search for parent context now."); }
			return parent.getClassObject(name);
		}
		return null;
	}

	public void bind(ClassObject classObject) throws BugTrap {
		String name = classObject.getName();
		if (Debug) IOUtils.println("Context binding class object: " + name);
		if (classObjectsMap.containsKey(name)) throw new BugTrap(this, "Class object " + name + " already exists.");
		classObjectsMap.put(name, classObject);

	}

	public ClassType getClassType(String classTypeName) {
		Program program = findEnclosingProgram();
		// Note: Class objects may have contexts that root at their own class instead of the program.
		return program.getClassBlock(classTypeName).getType();
	}




}
