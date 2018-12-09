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

import java.util.HashMap;
import java.util.Set;

import compiler.binder.Context;
import compiler.binder.Variable;
import compiler.blocks.Block;
import compiler.main.Settings;
import compiler.util.IOUtils;

public class ClassFieldsMap {
	public static final boolean Debug = Settings.Debug;

	HashMap<Variable, ClassField> map = new HashMap<Variable, ClassField>();
	Context classContext = null;
	
	public ClassFieldsMap(Context classContext) {
		this.classContext = classContext;
	}
	
	
	public void print(int indent) {
		IOUtils.printIndented(indent, this.getClass().getSimpleName() + " entries:");
		for (Variable v : map.keySet()) {
			map.get(v).print(indent+1);
		}
		IOUtils.printIndented(indent, "End of " + this.getClass().getSimpleName());
	}


	public boolean containsKey(Variable key) {
		return map.containsKey(key);
	}


	public ClassField put(ClassField field) {
		return map.put(field.getVariable(), field);
	}


	public ClassField get(Variable variable) {
		return map.get(variable);
	}
	
	public Set<Variable> keyset() {
		return map.keySet();
	}
	

	

	public String getAccess(Variable variable) {
		return get(variable).getAccess();
	}
	public boolean isPrivate(Variable variable) {
		return get(variable).isPrivate();
	}
	public boolean isProtected(Variable field) {
		return get(field).isProtected();
	}
	public boolean isPublic(Variable field) {
		return get(field).isPublic();
	}


	public boolean isAccessibleTo(Variable variable, Block block) {
		// If not part of any class, then there is no access by inheritance.
		if (!map.containsKey(variable)) return false;

		// Look for the enclosing class. If no such class exists, then
		// the caller block may not belong to any class. So it cannot
		// access the variable which belongs to some class.
		Block callerBlock = block;
		while (callerBlock!=null && !(callerBlock instanceof ClassBlock))
			callerBlock = callerBlock.parent;
		if (callerBlock==null) return false;

		// If public, then anyone can use
		ClassField classField = map.get(variable);
		if (classField.isPublic()) return true;
		
		// If not public, then check if callerClass is the owner
		ClassBlock owner = classField.getOwner();
		if (owner==callerBlock) return true;
		
		if (classField.isProtected()) {
			ClassBlock superclass = ((ClassBlock) callerBlock).getSuperClass();
			while (superclass!=null) {
				if (superclass==owner && classField.isProtected())
					return true;
				superclass = superclass.getSuperClass();
			}
		}
		return false;
	}


}
