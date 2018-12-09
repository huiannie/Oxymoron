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

import compiler.binder.Context;
import compiler.blocks.Function;
import compiler.blocks.Method;
import compiler.blocks.Module;
import compiler.data.DataType;
import compiler.declarations.ParameterDeclaration;
import compiler.expression.Expression;
import compiler.literal.MethodCall;
import compiler.literal.ValueType;
import compiler.main.Settings;
import compiler.util.BugTrap;
import compiler.util.IOUtils;

public class ClassType extends ValueType {
	public static final boolean Debug = Settings.Debug;

	public static final java.lang.String StartKeyword = ClassType.class.getName();

	Context rootContext = null;
	ClassBlock classBlock;

	public ClassType(ClassBlock classBlock) {
		this.classBlock = classBlock;
	}
	
	public String getType() {
		return classBlock.getName();
	}
	
	public void setRootContext(Context subContext) {
		this.rootContext = subContext;
	}

	public Context getRootContext() {
		return rootContext;
	}
	
	public void bindConstructor(Context callerContext, ArrayList<Expression> argumentsList) throws BugTrap {
		// Find the appropriate constructor
		ConstructorsMap constructors = this.classBlock.getConstructors();
		Context classTypeContext = this.getRootContext();

		// Execute the default class field declarations.
		this.classBlock.execute(classTypeContext);

		// Try to find a suitable constructor
		for (Constructor signature : constructors.keyset()) {
			if (signature.isSameSize(argumentsList)) {
				try {
					Method method = constructors.get(signature);
					// Bind a Constructor
					Context constructorContext = new Context(method, classTypeContext);
					
					// Any local variables used by the constructor will stay in the constructor's context.
					for (int index=0; index<argumentsList.size(); index++) {
						ParameterDeclaration p = signature.getParametersList().get(index);
						Expression argument = argumentsList.get(index);
						p.bind(constructorContext, callerContext, argument);
					}
					
					if (Debug) IOUtils.println("Binded to constructor " + signature.toString());
					
					// execute the constructor.
					method.execute(constructorContext);
					
					if (Debug) {
						this.classBlock.print(0);
						classTypeContext.print(0);
					}
					
					return;
				} catch (BugTrap e) {
					if (Debug) e.printStackTrace();
				}
			}
		}
		throw new BugTrap(this, "Binding failed.");
	}

	
	public DataType evaluate(MethodCall methodCall, Context callerContext) throws BugTrap {
		Method method = classBlock.getMethod(methodCall.getName());
		if (method==null) throw new BugTrap(callerContext, "Method " + methodCall.getName() + " is not defined.");
		
		// Since the method is called from some explicit class object call,
		// the context of the method should now be rooted at the class object.
		Context ownerContext = getRootContext().findOwnerClassContext(method);
		
		if (!ownerContext.allowsAccessTo(method, callerContext)) 
			throw new BugTrap(this, "Context " + callerContext.getBlock().getClass().getSimpleName() 
								+ " may not access " + method.getName() + " defined in " + ownerContext.getBlock().getClass().getSimpleName());

		
		Context subcontext = new Context(method, ownerContext);
		method.bindParameters(subcontext, callerContext, methodCall.getArguments());
		method.execute(subcontext);
		
		if (method instanceof Module) return null;
		else if (method instanceof Function) return ((Function)method).getReturnValue(subcontext);
		else throw new BugTrap(this, "method " + method.getName() + " is undefined");
	}




	@Override
	public java.lang.String valueToString() {
		return "Not done yet";
	}

	@Override
	public ValueType evaluate(Context context) throws BugTrap {
		throw new BugTrap(context, "Not supposed to execute a class object value type without binding any method");
	}

	public ClassType copy() {
		return this.classBlock.getType();
	}

}
