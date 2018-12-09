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
package compiler.expressionparser;

import java.util.LinkedList;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import compiler.expression.Expression;
import compiler.expression.GreaterEqual;
import compiler.expression.GreaterThan;
import compiler.expression.LessEqual;
import compiler.expression.LessThan;
import compiler.main.Settings;
import compiler.tokenizers.Token;
import compiler.util.BugTrap;
import compiler.util.IOUtils;

class ExpressionTokenizer {
	private static final boolean Debug = Settings.Debug;	

	public static final String keywords[] = Expression.keywords;

	public static final String Comma = Token.Comma;
	public static final String Period = Token.Period;
	public static final String SingleQuote = Token.SingleQuote;

	
	static public boolean keywordIsSymbol(String keyword) {
		if (!Token.isLetter(keyword.charAt(0))) return true;
		return false;
	}
	
	static public int findKeywordTokenEnd(String s, String keyword) {
		// Assume that the keyword must appear at the start of s 
		if (s.startsWith(keyword)) {

			if (keywordIsSymbol(keyword)) {
				// Two special cases where one keyword is a substring of another. Dismiss these false positives.
				if (keyword==LessThan.Keyword && s.startsWith(LessEqual.Keyword)) return -1;
				if (keyword==GreaterThan.Keyword && s.startsWith(GreaterEqual.Keyword)) return -1;
				return keyword.length();
			}
			else {
				int end = keyword.length();
				if (Token.isLetterOrDigit(s.charAt(end))) {
					return -1;
				}
				else {
					return end;
				}
			}
		}
		return -1;
	}
	
	
	static public Queue<String> tokenize(String line) throws BugTrap {
		Queue<String> tokens = new LinkedList<String>();
		int p=0;
		while (p<line.length()) {
			String substring = line.substring(p);
			boolean isKeyword = false;
			
			// First, check if the string starts with a keyword
			for (String key : keywords) {
				int end = findKeywordTokenEnd(substring, key);
				if (end!=-1) {
					tokens.add(key);
					p+=key.length();
					isKeyword = true;
					break;
				}
			}
			// If no keyword has been found at the start of the string
			if (!isKeyword) {
				// Check if there is a white space, skip it.
				if (substring.startsWith(" ")) {
					p++;
				}
				// Check if there is a number
				else if (substring.startsWith("-") || Character.isDigit(substring.charAt(0))) {
					Matcher matcher = Pattern.compile("-*[0-9]*\\.*[0-9]+\\b").matcher(substring);
					if (matcher.find()) {
						String token = substring.substring(matcher.start(), matcher.end());
						tokens.add(token);
						p+=token.length();
					}
				} 
				// Check if there is a name or a string
				else if (Token.isLetter(substring.charAt(0)) || Token.isUnderscore(substring.charAt(0))) {
					Matcher matcher2 = Pattern.compile("[0-9A-Za-z_]+\\b").matcher(substring);
					if (matcher2.find()) {
						String token = substring.substring(matcher2.start(), matcher2.end());
						tokens.add(token);
						p+=token.length();
					}
				}
				// Check if there is a character enclosed in single quote
				else if (substring.substring(0, 1).equals(SingleQuote) && substring.substring(2, 3).equals(SingleQuote)) {
					// Accept a character in the form of a string of length 3: 'x'
					String token = substring.substring(0, 3);
					tokens.add(token);
					p+=3;
				}
				// To handle functions, use comma as a token used to separate function parameters.
				else if (substring.startsWith(Comma)) {
					tokens.add(Comma);
					p++;
				}
				// To handle class objects, use period as a token used to separate class fields.
				else if (substring.startsWith(Period)) {
					tokens.add(Period);
					p++;
				}
				
				else {
					throw new BugTrap("In line " + line + ": Substring " + substring + " cannot be matched.");
				}
			}
		}
		
		if (Debug) {
			IOUtils.println("In Expression Tokenizer, Original string: " + line);
			for (String s : tokens) {
				IOUtils.print(s + " ");
			}
			IOUtils.println();
		}
		
		return tokens;
	}

}
