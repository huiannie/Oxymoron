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

import java.util.ArrayList;

import compiler.binder.Context;
import compiler.classes.ClassBlock;
import compiler.declarations.ParameterDeclaration;
import compiler.expression.Expression;
import compiler.main.Settings;
import compiler.tokenizers.LineMatcher;
import compiler.util.BugTrap;
import compiler.util.IOUtils;

public class Method extends Block {
	public static final boolean Debug = Settings.Debug;

	public static final String AccessPublicKeyword = ClassBlock.AccessPublicKeyword;
	public static final String AccessProtectedKeyword = ClassBlock.AccessProtectedKeyword;
	public static final String AccessPrivateKeyword = ClassBlock.AccessPrivateKeyword;
	protected static final String AccessToken = ClassBlock.AccessToken;
	
	protected String name = null;
	protected String access = null; // default
	
	public ArrayList<ParameterDeclaration> parametersList = new ArrayList<ParameterDeclaration>();
		
	public Method(int startLineNumber, int endLineNumber) {
		super(startLineNumber, endLineNumber);
		selfContained = true;
	}

	public String getName() {
		return name;
	}
	
	
	public static boolean startsWithAKeyword(String line) {
		if (LineMatcher.matchStart(line, AccessProtectedKeyword)) return true;
		else if (LineMatcher.matchStart(line, AccessPrivateKeyword)) return true;
		else if (LineMatcher.matchStart(line, AccessPublicKeyword)) return true;
		else if (Function.startsWithAKeyword(line)) return true;
		else if (Module.startsWithAKeyword(line)) return true;
		return false;
	}

	public boolean isPrivate() {
		return access.equals(AccessPrivateKeyword);
	}
	public boolean isProtected() {
		return access.equals(AccessProtectedKeyword);
	}
	public boolean isPublic() {
		return access.equals(AccessPublicKeyword);
	}
	
	public void print(int indent, String extras) {
		IOUtils.printIndented(indent, this.getClass().getSimpleName() 
								+ " name: " + (name==null?"unknown":name) 
								+ " range: " + getStartLineNumber() + "-" + getEndLineNumber());
		if (parametersList.size()>0) {
			IOUtils.printIndented(indent+1, "Parameter list:");
			for (ParameterDeclaration p : parametersList) {
				p.print(indent+2);
			}
			IOUtils.printIndented(indent+1, "End parameter list.");
		}
		else {
			IOUtils.printIndented(indent+1, "No parameter list.");
		}
		if (extras!=null) IOUtils.printIndented(indent, extras);
		// Now list the subblocks in this method.
		if (!subblocks.isEmpty()) {
			IOUtils.printIndented(indent+1, "Start of subblocks:");
			for (Block block : subblocks) {
				block.print(indent+2);
			}
			IOUtils.printIndented(indent+1, "End of subblocks");
		}		
	}
	

	
	public void bindParameters(Context methodContext, Context callerContext, ArrayList<Expression> argumentsList) throws BugTrap {
		
		for (int index=0; index<argumentsList.size(); index++) {
			ParameterDeclaration p = parametersList.get(index);
			Expression argument = argumentsList.get(index);
			p.bind(methodContext, callerContext, argument);
		}
	}


}
