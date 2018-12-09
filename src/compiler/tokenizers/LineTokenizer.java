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
package compiler.tokenizers;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import compiler.binder.VariableRef;
import compiler.classes.ClassBlock;
import compiler.declarations.ParameterDeclaration;
import compiler.expression.Equal;
import compiler.main.Settings;
import compiler.util.BugTrap;
import compiler.util.IOUtils;


/* This tokenizer inputs a string and save it into header. 
 * Then it allows the caller to peek and check if the front of the header contains 
 * a substring that matches what is needed.
 * Upon finished checking, the caller may parse that substring. When that substring
 * is parsed, it is removed from header.
 *
 * Attention: This tokenizer modifies header after each call to parse. 
 */

public class LineTokenizer {
	public static final boolean Debug = Settings.Debug;
	
	private static final String AccessPublicKeyword = ClassBlock.AccessPublicKeyword;
	private static final String AccessProtectedKeyword = ClassBlock.AccessProtectedKeyword;
	private static final String AccessPrivateKeyword = ClassBlock.AccessPrivateKeyword;
	
	private static final String ValidSimpleName = "\\b" + Token.ValidNameTagPattern + "\\b";
	// Example: ABC1, __abc

	private static final String PrimitiveDataTypeName = Token.PrimitiveDataTypeName;

	private static final String ArrayDataTypeName = Token.ArrayDataTypeName;
	private static final String ArrayEmptyIndex = "\\[\\]";
	// Example: [A]
	private static final String ArrayIndex1 = "\\[" + Token.ValidNameTagPattern + "\\]";
	// Example: [12]
	private static final String ArrayIndex2 = "\\[" + Token.PositiveIntegerPattern + "\\]";

	private static final String ParameterList = "\\(.*\\)";
	private static final String ValidPositiveInteger = Token.PositiveIntegerPattern;
	
	private static final String FileDataTypeName = Token.FileDataTypeName;
	private static final String FileModeName = Token.FileModePattern;

	
	String header;
	public LineTokenizer(String line) {
		header = line.trim();
	}
	
	public String getRemainder() {
		return header;
	}
	
	public boolean isAccessModifier() throws BugTrap {
		if (LineMatcher.matchStart(header, AccessPublicKeyword)) {
			return true;
		}
		else if (LineMatcher.matchStart(header, AccessProtectedKeyword)) {
			return true;
		}
		else if (LineMatcher.matchStart(header, AccessPrivateKeyword)) {
			return true;
		}
		return false;
	}
	
	
	public String parseAccessModifier() throws BugTrap {
		String access = null;
		
		if (LineMatcher.matchStart(header, AccessPublicKeyword)) {
			access = AccessPublicKeyword;
			header = header.substring(access.length()).trim();
		}
		else if (LineMatcher.matchStart(header, AccessProtectedKeyword)) {
			access = AccessProtectedKeyword;
			header = header.substring(access.length()).trim();
		}
		else if (LineMatcher.matchStart(header, AccessPrivateKeyword)) {
			access = AccessPrivateKeyword;
			header = header.substring(access.length()).trim();
		}
		return access;
	}

	public boolean isKeyword(String keyword) {
		return LineMatcher.matchStart(header, keyword);
	}
	
	public String parseKeyword(String keyword) throws BugTrap {
		// Look for the keyword. 
		if (LineMatcher.matchStart(header, keyword)) {
			header = header.substring(keyword.length()).trim();
			return keyword;
		}
		return null;
	}

	public boolean isName() {
		return (LineMatcher.matchStart(header, ValidSimpleName));
	}	
	
	public String parseName() throws BugTrap {
		String name = null;
		Matcher matcher = Pattern.compile(ValidSimpleName).matcher(header);
		if (matcher.find()) {
			name = header.substring(matcher.start(), matcher.end());
			header = header.substring(name.length()).trim();
		}
		return name;
	}
	
	// Match A[10] etc
	public boolean isArrayName() throws BugTrap {
		Matcher matcher;
		int dims = 0;
		boolean found;

		String copy = header;
		
		// Check for name.
		matcher = Pattern.compile(ValidSimpleName).matcher(copy);
		if (!matcher.find()) return false;
				
		// Remove name. Then check for indices.
		copy = copy.substring(matcher.end()).trim();

		do {
			found = false;
			matcher = Pattern.compile("^" + ArrayIndex1).matcher(copy);
			if (matcher.find()) {
				copy = copy.substring(matcher.end()).trim();
				dims++;
				found = true;
			}
			matcher = Pattern.compile("^" + ArrayIndex2).matcher(copy);
			if (matcher.find()) {
				copy = copy.substring(matcher.end()).trim();
				dims++;
				found = true;
			}
		} while (found);	
		return dims>0;
	}
	
	// Match A[10] etc
	public String parseArrayName() throws BugTrap {
		String name = null;
		boolean found;
		
		Matcher matcher = Pattern.compile(ValidSimpleName).matcher(header);
		if (matcher.find()) {
			name = header.substring(matcher.start(), matcher.end());
			header = header.substring(matcher.end()).trim();
		}
		do {
			found = false;
			matcher = Pattern.compile("^" + ArrayIndex1).matcher(header);
			if (matcher.find()) {
				name += header.substring(matcher.start(), matcher.end());
				header = header.substring(matcher.end()).trim();
				found = true;
			}
			matcher = Pattern.compile("^" + ArrayIndex2).matcher(header);
			if (matcher.find()) {
				name += header.substring(matcher.start(), matcher.end());
				header = header.substring(matcher.end()).trim();
				found = true;
			}
		} while (found);	
		
		return name;
	}
	
	
	// Match A[] etc
	public boolean isArrayNameWithEmptyIndexBracket() throws BugTrap {
		Matcher matcher;
		int dims = 0;
		boolean found;

		String copy = header;
		
		// Check for name.
		matcher = Pattern.compile(ValidSimpleName).matcher(copy);
		if (!matcher.find()) return false;
				
		// Remove name. Then check for indices.
		copy = copy.substring(matcher.end()).trim();

		do {
			found = false;
			matcher = Pattern.compile("^" + ArrayEmptyIndex).matcher(copy);
			if (matcher.find()) {
				copy = copy.substring(matcher.end()).trim();
				dims++;
				found = true;
			}
		} while (found);	
		return dims>0;
	}
	
	// Match A[] etc
	public String parseArrayNameWithEmptyIndexBracket() throws BugTrap {
		String name = null;
		boolean found;
		
		Matcher matcher = Pattern.compile(ValidSimpleName).matcher(header);
		if (matcher.find()) {
			name = header.substring(matcher.start(), matcher.end());
			header = header.substring(matcher.end()).trim();
		}
		do {
			found = false;
			matcher = Pattern.compile("^" + ArrayEmptyIndex).matcher(header);
			if (matcher.find()) {
				name += header.substring(matcher.start(), matcher.end());
				header = header.substring(matcher.end()).trim();
				found = true;
			}
		} while (found);	
		
		return name;
	}

	
	
	
	// Match [], [][] etc
	public boolean isArrayEmptyIndex() throws BugTrap {
		Matcher matcher;
		matcher = Pattern.compile("^" + ArrayEmptyIndex).matcher(header);
		if (matcher.find()) return true;
		matcher = Pattern.compile("^" + ArrayEmptyIndex).matcher(header);
		if (matcher.find()) return true;
		return false;
	}

	
	// Match [10], [A][B] etc
	public boolean isArrayIndex() throws BugTrap {
		Matcher matcher;
		matcher = Pattern.compile("^" + ArrayIndex1).matcher(header);
		if (matcher.find()) return true;
		matcher = Pattern.compile("^" + ArrayIndex2).matcher(header);
		if (matcher.find()) return true;
		return false;
	}

	// Match [10], [A][B] etc
	public String parseArrayIndexWithBracket() throws BugTrap {
		String bracketedIndex = null;
		Matcher matcher = Pattern.compile("^" + ArrayIndex1).matcher(header);
		if (matcher.find()) {
			bracketedIndex = header.substring(matcher.start(), matcher.end());
			header = header.substring(matcher.end()).trim();
			return bracketedIndex;
		}
		matcher = Pattern.compile("^" + ArrayIndex2).matcher(header);
		if (matcher.find()) {
			bracketedIndex = header.substring(matcher.start(), matcher.end());
			header = header.substring(matcher.end()).trim();
			return bracketedIndex;
		}		
		return bracketedIndex;
	}
	public String parseArrayIndexWithoutBracket() throws BugTrap {
		String bracketedIndex = parseArrayIndexWithBracket();
		return bracketedIndex.substring(1, bracketedIndex.length()-1);
	}

	public boolean isPositiveInteger() {
		Matcher matcher = Pattern.compile(ValidPositiveInteger).matcher(header);
		return matcher.find();
	}
	
	public String parsePositiveInteger() {
		String integerString = null;
		Matcher matcher = Pattern.compile(ValidPositiveInteger).matcher(header);
		if (matcher.find()) {
			integerString = header.substring(matcher.start(), matcher.end());
			header = header.substring(matcher.end()).trim();
		}
		return integerString;		
	}
	

	public boolean isPrimitiveDataType() throws BugTrap {
		Matcher matcher = Pattern.compile(PrimitiveDataTypeName).matcher(header);
		return matcher.find();
	}
	
	public String parsePrimitiveDataType() throws BugTrap {
		String datatypeString = null;
		Matcher matcher = Pattern.compile(PrimitiveDataTypeName).matcher(header);
		if (matcher.find()) {
			datatypeString = header.substring(matcher.start(), matcher.end());
			header = header.substring(matcher.end()).trim();
		}
		return datatypeString;
	}
	
	public boolean isArrayDataType() throws BugTrap {
		Matcher matcher = Pattern.compile(ArrayDataTypeName).matcher(header);
		return matcher.find();
	}
	
	public String parseArrayDataType() throws BugTrap {
		String datatypeString = null;
		Matcher matcher = Pattern.compile(ArrayDataTypeName).matcher(header);
		if (matcher.find()) {
			datatypeString = header.substring(matcher.start(), matcher.end());
			header = header.substring(matcher.end()).trim();
		}
		return datatypeString;
	}

	public boolean isFileDataType() throws BugTrap {
		Matcher matcher = Pattern.compile(FileDataTypeName).matcher(header);
		return matcher.find();
	}
	
	public String parseFileDataType() throws BugTrap {
		String datatypeString = null;
		Matcher matcher = Pattern.compile(FileDataTypeName).matcher(header);
		if (matcher.find()) {
			datatypeString = header.substring(matcher.start(), matcher.end());
			header = header.substring(matcher.end()).trim();
		}
		return datatypeString;
	}

	public boolean isFileMode() throws BugTrap {
		Matcher matcher = Pattern.compile(FileModeName).matcher(header);
		return matcher.find();
	}
	
	public String parseFileMode() throws BugTrap {
		String datatypeString = null;
		Matcher matcher = Pattern.compile(FileModeName).matcher(header);
		if (matcher.find()) {
			datatypeString = header.substring(matcher.start(), matcher.end());
			header = header.substring(matcher.end()).trim();
		}
		return datatypeString;
	}

	
	// For method declaration only
	public boolean isParameters() {
		Matcher matcher = Pattern.compile("^" + ParameterList).matcher(header);
		return matcher.find();
	}
	
	// For method declaration only
	public ArrayList<ParameterDeclaration> parseParameters() throws BugTrap {
		// Check arguments
		Matcher matcher = Pattern.compile("^" + ParameterList).matcher(header);

		if (!matcher.find()) return null;

		ArrayList<ParameterDeclaration> parametersList = new ArrayList<ParameterDeclaration>();
		// Extract out the arguments.
		String args = header.substring(matcher.start()+1, matcher.end()-1).trim();
		
		String arguments[];
		if (args.length()==0) arguments = new String[0];
		else arguments = args.split(",");
		for (String arg : arguments) {
			if (Debug) IOUtils.printIndented(2, "parsing argument: " + arg);

			
			// Break each argument into tokens of two or three fields
			String token[] = arg.split(" ");
			if (token.length==2) {
				// pass by value
				ParameterDeclaration parameter = new ParameterDeclaration(token[0], false, token[1]);
				parametersList.add(parameter);
			}
			else if (token.length==3 && token[1].equals(VariableRef.Keyword)) {
				// pass by reference
				ParameterDeclaration parameter = new ParameterDeclaration(token[0], true, token[2]);
				parametersList.add(parameter);
			}
			else {
				throw new BugTrap("Cannot recognize argument: " + arg);
			}
			
		}
		
		if (Debug) IOUtils.printIndented(2, "Parameter list " + this.getClass().getSimpleName() + " completely parsed. ");

		header = header.substring(header.indexOf(args) + args.length()).trim();
		return parametersList;
	}
	
	
	public boolean isAssignSign() {
		if (header.startsWith(Equal.Keyword)) return false;
		Matcher matcher = Pattern.compile("^" + Token.assignSign + "(\\b)*").matcher(header);
		return matcher.find();
	}

	public String parseAssignSign() {
		if (header.startsWith(Equal.Keyword)) return null;
		Matcher matcher = Pattern.compile("^" + Token.assignSign + "(\\b)*").matcher(header);
		if (matcher.find()) {
			header = header.substring(header.indexOf(Token.assignSign) + Token.assignSign.length()).trim();
			return Token.assignSign;
		}
		return null;
	}
	

	
	
	// For now, just let a class data type be any valid simple name.
	public boolean isClassType() {
		return isName();
	}	
	
	public String parseClassType() throws BugTrap {
		return parseName();
	}

	
}
