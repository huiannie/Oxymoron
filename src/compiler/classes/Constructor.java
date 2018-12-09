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

import compiler.declarations.ParameterDeclaration;
import compiler.expression.Expression;
import compiler.main.Settings;
import compiler.tokenizers.Token;

public class Constructor {
	public static final boolean Debug = Settings.Debug;
	
	String name;
	ArrayList<ParameterDeclaration> parametersList = null;
	
	// Default constructor
	public Constructor(String name) {
		this.name = name;
		this.parametersList = new ArrayList<ParameterDeclaration>();
	}


	public Constructor(String name, ArrayList<ParameterDeclaration> parametersList) {
		this.name = name;
		this.parametersList = parametersList;
	}

	public boolean isSameSize(ArrayList<Expression> argumentsList) {
		return parametersList.size()==argumentsList.size();
	}
	
	public ArrayList<ParameterDeclaration> getParametersList() {
		return this.parametersList;
	}
	
	public String toString() {
		String s = "";
		for (ParameterDeclaration p : parametersList) {
			s += (s==""?"":";") + p.getDatatypeString() + " " + (p.isReference()?"Ref ":"") + p.getName() ;
		}
		return name + Token.RoundOpen + s + Token.RoundClose;
	}
	
	public boolean isDefault() {
		return (parametersList.size()==0);
	}
}
