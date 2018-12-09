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

import java.io.PrintStream;

public class ResIOUtils {
	private static PrintStream outstream = System.out;
	

	public static void println(String string) {
		outstream.println(string);
	}
	public static void print(String string) {
		outstream.print(string);
	}
	public static void println() {
		outstream.println();
	}

	public static void printIndented(int indent, String string) {
		String indentation = "";
		for (int i=0; i<indent; i++) indentation += "  ";

		outstream.println(indentation + string);
	}
	
	public static void printIndented(int indent, Class<?> c, String string) {
		String indentation = "";
		for (int i=0; i<indent; i++) indentation += "  ";

		outstream.println("Class: " + c.getSimpleName() + indentation + string);
	}


}
