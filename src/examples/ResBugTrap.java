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
import java.io.PrintWriter;
import java.io.StringWriter;

public class ResBugTrap extends Exception {
	private static final long serialVersionUID = -7226625439309415739L;

	private PrintStream errStream = System.err;
	
	private String details;

	public String getDetails() {
		return details;
	}
	
	public ResBugTrap(java.lang.String string) {
		details = string;
	}

	public ResBugTrap(java.lang.Exception e) {
		details = e.getMessage();
		setStackTrace(e.getStackTrace());
	}
	

	public void printStackTrace() {
		StringWriter sw = new StringWriter();
		super.printStackTrace(new PrintWriter(sw));
		errStream.println(sw.toString());
	}

	public static void printStackTrace(Exception e) {
		new ResBugTrap(e).printStackTrace();
	}
}
