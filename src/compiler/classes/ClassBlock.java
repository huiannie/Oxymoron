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

import java.util.ArrayList;

import compiler.binder.Constant;
import compiler.binder.Context;
import compiler.binder.Variable;
import compiler.blocks.Block;
import compiler.blocks.Method;
import compiler.declarations.Declaration;
import compiler.literal.Literal;
import compiler.main.Settings;
import compiler.parser.Cmds;
import compiler.parser.MethodsMap;
import compiler.parser.Scope;
import compiler.tokenizers.LineMatcher;
import compiler.tokenizers.LineTokenizer;
import compiler.util.BugTrap;
import compiler.util.IOUtils;

public class ClassBlock extends Block {
	public static final boolean Debug = Settings.Debug;
	
	public static final String StartKeyword = "Class";
	public static final String EndKeyword = "End Class";
	public static final String ExtendKeyword = "Extends";

	
	public static final String AccessPublicKeyword = "Public";
	public static final String AccessProtectedKeyword = "Protected";
	public static final String AccessPrivateKeyword = "Private";

	public static final String AccessToken = "(" + AccessPublicKeyword + "|" + AccessProtectedKeyword + "|" + AccessPrivateKeyword + ")";


	private String name = null;
	private String access = null;  // default
	private ClassBlock superclass = null;
	private ClassBlocksMap subclasses = new ClassBlocksMap();
	private ConstructorsMap constructors = new ConstructorsMap();
	private MethodsMap methodsMap = new MethodsMap();
	
	
	public ClassBlock(int startLineNumber, int endLineNumber) {
		super(startLineNumber, endLineNumber);
		selfContained = true;
		hasHeaderAndFooter = true;
	}
	
	// Use this as a temporary place holder for superclass and subclass that are not yet fully parsed.
	private ClassBlock(String name) {
		super(Cmds.UnknownLineNumber, Cmds.UnknownLineNumber);
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public static boolean startsWithAKeyword(String line) {
		if (LineMatcher.matchStart(line, AccessProtectedKeyword)) return true;
		else if (LineMatcher.matchStart(line, AccessPrivateKeyword)) return true;
		else if (LineMatcher.matchStart(line, AccessPublicKeyword)) return true;
		else if (LineMatcher.matchStart(line, EndKeyword)) return true;
		else if (LineMatcher.matchStart(line, StartKeyword)) return true;
		// Note: the keyword Extends should not appear at the start of a line.
		return false;
	}

	
	public static boolean isAStart(String line) {

		if (LineMatcher.matchStart(line, StartKeyword)) return true;
		else if (LineMatcher.matchStart(line, AccessToken, StartKeyword)) return true;
		else return false;
	}

	public static boolean isAnEnd(String line) {
		return (LineMatcher.matchExact(line, EndKeyword));
	}

	
	public void parseHeader(String line) throws BugTrap {
		// Syntax: [access] Class name
		
		LineTokenizer tokenizer = new LineTokenizer(line);
		
		if (tokenizer.isAccessModifier())
			access = tokenizer.parseAccessModifier();

		if (!tokenizer.isKeyword(StartKeyword)) throw new BugTrap(this, StartKeyword + ": Keyword missing");
		tokenizer.parseKeyword(StartKeyword);

		if (!tokenizer.isName()) throw new BugTrap(this, "name missing");
		name = tokenizer.parseName();

		if (tokenizer.isKeyword(ExtendKeyword)) {
			// Remove the keyword
			tokenizer.parseKeyword(ExtendKeyword);
			
			if (!tokenizer.isName()) throw new BugTrap(this, "super class name missing.");
			String superclassName = tokenizer.parseName();
			// For now, put a dummy block that records the name.
			superclass = new ClassBlock(superclassName);
		}
	}

	public ClassBlock getSuperClass() {
		return superclass;
	}
	public void setSuperClass(ClassBlock superclass) {
		this.superclass = superclass;
	}
	public boolean isSubClass(String name) {
		return subclasses.containsKey(name);
	}
	public void putSubClass(ClassBlock subclass) {
		subclasses.put(subclass.getName(), subclass);
	}
	public boolean isExtendedFrom(ClassBlock classBlock) {
		if (superclass==null) return false;
		else if (superclass==classBlock) return true;
		else return superclass.isExtendedFrom(classBlock);
	}

	public Method getMethod(String name) {
		// Search immediate class
		if (methodsMap.containsKey(name)) {
			Method method = methodsMap.get(name);
			return method;
		}
		// If not found, then search superclass
		else if (superclass!=null)
			return superclass.getMethod(name);
		else
			return null;
	}
	public Method getMethodNaive(String name) {
		// Search immediate class
		if (methodsMap.containsKey(name)) {
			return methodsMap.get(name);
		}
		else
			return null;
	}
	
	
	
	public ClassType getType() {
		return new ClassType(this);
	}

	public ConstructorsMap getConstructors() {
		// TODO: inherit constructors
		return constructors;
	}
	
	public void print(int indent) {
		IOUtils.printIndented(indent, getClass().getSimpleName() + ": " + name);
		methodsMap.print(indent+1);
		IOUtils.printIndented(indent, "End of " + getClass().getSimpleName() + ": " + name);
	}
	
	@Override
	public void parse(ArrayList<String> code, Scope scope, Block parent) throws BugTrap {
		String line = code.get(getStartLineNumber());
		parseHeader(line);
		
		super.parse(code, scope, parent);

		for (Block subblock : subblocks) {
			if (Debug) IOUtils.println(getClass().getSimpleName() + " " + name + " subblock " + subblock.getClass().getSimpleName());
			
			if (subblock instanceof Method) {
				Method method = (Method) subblock;
				if (method.getName().equals(name)) {
					// TODO: Need to check if one already exists
					constructors.put(new Constructor(name, method.parametersList), method);
				}
				else {
					if (methodsMap.containsKey(method.getName()))
						throw new BugTrap(this, "Method " + method.getName() + " already exists.");
					methodsMap.put(method.getName(), method);
				}
			}
		}
		
		// Check if the default constructor is defined. If not, add it.
		boolean found = false;
		for (Constructor constructor : constructors.keyset()) {
			if (constructor.isDefault()) found = true;
		}
		if (!found) {
			Method method = new Method(-1, -1);
			constructors.put(new Constructor(name), method);
			if (Debug) IOUtils.println(getClass().getSimpleName() + " : adding default constructor.");
		}
	}
	



	@Override
	public int execute(Context context) throws BugTrap {
		if (Debug) IOUtils.println(getClass().getSimpleName() + " " + name + ": executing to initialize class fields.");
		
		ClassFieldsMap fieldsMap = new ClassFieldsMap(context);

		for (Block subblock : subblocks) {
			if (subblock instanceof Declaration) {
				ArrayList<Literal> literals = ((Declaration) subblock).getDeclarations();
				for (Literal d : literals) {
					// Only load all the class field declarations.
					if (d instanceof ClassFieldDeclaration) {
						ClassFieldDeclaration declaration = (ClassFieldDeclaration) d;
						Variable variable = new Variable(declaration, context);
						String access = declaration.getAccess();
						ClassBlock owner = this;
						ClassField classField = new ClassField(variable, access, owner);
						fieldsMap.put(classField);
					}
				}
			}
		}
		
		// Next, call superclass 
		if (superclass!=null) {
			Context superclassContext = new Context(superclass, null);
			superclass.execute(superclassContext);
			// only transfer to current context the fields and constants that not already there
			
			
			// Handle the class fields within the superclass
			for (String symbol : superclassContext.getVariablesMap().keyset()) {
				Variable variable = superclassContext.getVariablesMap().get(symbol);
				if (!context.isDeclared(symbol) && !superclassContext.getClassFieldsMap().isPrivate(variable)) {
					// Transfer the variable to this context.
					context.bind(variable);
					// Transfer the class field to this classFieldsMap.
					fieldsMap.put(superclassContext.getClassFieldsMap().get(variable));
				}
			}
			
			// Handle the constants
			for (String symbol : superclassContext.getConstantsMap().keyset()) {
				Constant constant = superclassContext.getConstantsMap().get(symbol);
				if (!context.isDeclared(symbol)) {
					context.bind(constant);
				}
			}
			// After processing the class fields and constants, the superclass context is preserved
			context.setSuper(superclassContext);
		}

		context.setClassFieldsMap(fieldsMap);
		
		return ExitCode_OK;
	}

}
