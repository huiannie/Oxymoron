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

import compiler.main.Settings;
import compiler.util.IOUtils;

public class LineMatcher {
	public static final boolean Debug = Settings.Debug;
	
	
	public static boolean matchStart(String line, String keyword) {
		String regexp = "^" + keyword + "\\b";
		Matcher matcher = Pattern.compile(regexp).matcher(line);
		boolean result = matcher.find();  // Do not call find() more than once since we only want 1 match.
		if (Debug && result) IOUtils.println("\"" + line + "\" matched: \"" + matcher.group(0) +"\"");
		return result;
	}

	public static boolean matchStart(String line, String keyword1, String keyword2) {
		String regexp = "^" + keyword1 + "\\b\\s*" + keyword2 + "\\b";
		Matcher matcher = Pattern.compile(regexp).matcher(line);
		boolean result = matcher.find();  // Do not call find() more than once since we only want 1 match.
		if (Debug && result) IOUtils.println("\"" + line + "\" matched: \"" + matcher.group(0) +"\"");
		return result;
	}
	
	public static boolean matchStart(String line, String keyword1, String keyword2, String keyword3) {
		String regexp = "^" + keyword1 + "\\b\\s*" + keyword2 + "\\b\\s*" + keyword3 + "\\b";
		Matcher matcher = Pattern.compile(regexp).matcher(line);
		boolean result = matcher.find();  // Do not call find() more than once since we only want 1 match.
		if (Debug && result) IOUtils.println("\"" + line + "\" matched: \"" + matcher.group(0) +"\"");
		return result;
	}
	

	public static boolean matchExact(String line, String keyword) {
		return line.matches(keyword);
	}

	public static boolean matchExact(String line, String keywords[]) {
		for (String keyword : keywords) {
			if (line.matches(keyword)) return true;
		}
		return false;
	}


	
}
