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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Token {
	public static final String OneCharacterOrMore = ".+";
	public static final String ZeroCharacterOrMore = ".*";
	public static final String OneDigitLetterOrUnderscore = "[_0-9A-Z-a-z]";
	public static final String ValidNameTagPattern = "[_A-Za-z][_0-9A-Z-a-z]*";
	public static final String PositiveIntegerPattern = "\\d+";

	public static final String RoundOpen = "(";
	public static final String RoundClose = ")";
	public static final String SquareOpen = "[";
	public static final String SquareClose = "]";
	public static final String Tab = "\t";
	public static final String space = " ";
	public static final String assignSign = "=";
	public static final String Comma = ",";
	public static final String Period = ".";
	public static final String Colon = ":";
	public static final String TabLabel = "Tab";
	public static final String SingleQuote = "'";
	public static final String DoubleQuote = "\"";

	
	public static final String PrimitiveDataTypeName = "(" + compiler.data.Integer.StartKeyword 
														+ "|" + compiler.data.Real.StartKeyword 
														+ "|" + compiler.data.String.StartKeyword 
														+ "|" + compiler.data.Character.StartKeyword 
														+ "|" + compiler.data.Boolean.StartKeyword 
														+ ")";

	public static final String ArrayDataTypeName = "(" + compiler.data.Integer.StartKeyword 
													+ "|" + compiler.data.Real.StartKeyword 
													+ "|" + compiler.data.String.StartKeyword 
													+ "|" + compiler.data.Character.StartKeyword 
													+ "|" + compiler.data.Boolean.StartKeyword 
													+ "|" + compiler.files.InputFile.StartKeyword 
													+ "|" + compiler.files.OutputFile.StartKeyword 
													+ ")" + "(\\[\\])";

	public static final String FileDataTypeName = "(" + compiler.files.InputFile.StartKeyword 
													+ "|" + compiler.files.OutputFile.StartKeyword 
													+ ")";
	public static final String FileModePattern = compiler.files.OutputFile.ModeKeyword;
	

	public static final String BooleanPattern = "(" + compiler.data.Boolean.True
												+ "|" + compiler.data.Boolean.False
												+ ")";
	public static final String CharacterPattern = SingleQuote + "." + SingleQuote;
	public static final String StringPattern = DoubleQuote + OneCharacterOrMore + DoubleQuote;

	
	public static boolean isLetterOrDigit(char c) {
		return (c >= 'a' && c <= 'z') ||
			   (c >= 'A' && c <= 'Z') ||
			   (c >= '0' && c <= '9') ;
	}
	
	public static boolean isLetter(char c) {
		return (c >= 'a' && c <= 'z') ||
				   (c >= 'A' && c <= 'Z');
	}
	
	public static boolean isUnderscore(char c) {
		return c=='_';
	}
	
	public static boolean isANumber(String s) {
		Matcher matcher = Pattern.compile("-*[0-9]*\\.*[0-9]+\\b").matcher(s);
		return matcher.find();
	}
}
