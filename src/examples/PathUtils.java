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
package examples;

public class PathUtils {
	public static final String delimiter = "/";
	
	public static String joinAll(String... parts) {
		String result = parts[0];
		if (parts.length==1) return result;
		
		for (int i=1; i<parts.length; i++) {
			result = join(result, parts[i]);
		}
		return result;
	}

	public static String join(String head, String tail) {

		if (head.endsWith(delimiter) && tail.startsWith(delimiter)) return head + tail.substring(1);
		if (head.endsWith(delimiter) && tail.startsWith("." + delimiter)) return head + tail.substring(2);
		if (head.endsWith(delimiter) && !tail.startsWith(delimiter)) return head + tail;
		if (!head.endsWith(delimiter) && tail.startsWith(delimiter)) return head + tail;
		if (!head.endsWith(delimiter) && tail.equals(".")) return head;
		if (!head.endsWith(delimiter) && tail.startsWith("." + delimiter)) return head + delimiter + tail.substring(2);
		if (!head.endsWith(delimiter) && !tail.startsWith(delimiter)) return head + delimiter + tail;
		return head + delimiter + tail;
	}
	
	public static String packageNameToPath(String packageName) {
		return packageName.replace('.', delimiter.charAt(0));
	}
}
