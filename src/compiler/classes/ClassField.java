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

import compiler.binder.Variable;
import compiler.main.Settings;
import compiler.util.IOUtils;

public class ClassField {
	public static final boolean Debug = Settings.Debug;
	
	private static final String AccessPublicKeyword = ClassBlock.AccessPublicKeyword;
	private static final String AccessProtectedKeyword = ClassBlock.AccessProtectedKeyword;
	private static final String AccessPrivateKeyword = ClassBlock.AccessPrivateKeyword;
	
	Variable variable;
	String access;
	ClassBlock owner;
	
	public ClassField(Variable variable, String access, ClassBlock owner) {
		this.variable = variable;
		setAccess(access);
		this.owner = owner;
	}
	
	public void setAccess(String access) {
		if (access.equals(AccessPrivateKeyword))
			this.access = AccessPrivateKeyword;
		else if (access.equals(AccessProtectedKeyword))
			this.access = AccessProtectedKeyword;
		else if (access.equals(AccessPublicKeyword))
			this.access = AccessPublicKeyword;
		// Else, there is no access mode.
	}

	public String getAccess() {
		return access;
	}
	public boolean isPublic() {
		return access==AccessPublicKeyword;
	}
	public boolean isProtected() {
		return access==AccessProtectedKeyword;
	}
	public boolean isPrivate() {
		return access==AccessPrivateKeyword;
	}
	
	public Variable getVariable() {
		return variable;
	}
	public ClassBlock getOwner() {
		return owner;
	}
	
	public void print(int indent) {
		IOUtils.printIndented(indent, "Field:" + variable.getName() + " is declared in ClassBlock:" + owner.getName() + " with access:" + access);
	}
}
