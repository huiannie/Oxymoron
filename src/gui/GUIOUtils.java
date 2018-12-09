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
package gui;

import java.awt.Color;

import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

import compiler.main.Settings;
import compiler.util.BugTrap;

public class GUIOUtils extends Interruptable {
	static Interrupt flag = null;
	
	private static GuiOutputPipe outstream = (GuiOutputPipe) Settings.GetOutstream();
	private Style StyleBlue;
	private Style StyleRed;
	private Style StyleGreen;

	private static GUIOUtils g = new GUIOUtils();
	
	private GUIOUtils() {
		StyleContext context = new StyleContext();
		StyleBlue = context.addStyle("Blue", null);
		StyleRed = context.addStyle("Red", null);
		StyleGreen = context.addStyle("Green", null);
		StyleConstants.setForeground(StyleBlue, Color.BLUE);
		StyleConstants.setForeground(StyleRed, Color.RED);
		StyleConstants.setForeground(StyleGreen, Color.GREEN);
	}
	
	public static GUIOUtils Get() {
		return g;
	}
	

	public static void printlnBlue(String string) throws BugTrap {
		outstream.println(string, g.StyleBlue);
		g.handleInterrupt();
	}
	public static void printlnRed(String string) throws BugTrap {
		outstream.println(string, g.StyleRed);
		g.handleInterrupt();
	}
	public static void printlnGreen(String string) throws BugTrap {
		outstream.println(string, g.StyleGreen);
		g.handleInterrupt();
	}

	public static void println(String string) throws BugTrap {
		outstream.println(string);
		g.handleInterrupt();
	}
	public static void print(String string) throws BugTrap {
		outstream.print(string);
		g.handleInterrupt();
	}
	public static void println() throws BugTrap {
		outstream.println();
		g.handleInterrupt();
	}

	public static void printIndented(int indent, String string) throws BugTrap {
		String indentation = "";
		for (int i=0; i<indent; i++) indentation += "  ";
		outstream.println(indentation + string);
		g.handleInterrupt();
	}
	
	public static void printIndented(int indent, Class<?> c, String string) throws BugTrap {
		String indentation = "";
		for (int i=0; i<indent; i++) indentation += "  ";
		outstream.println("Class: " + c.getSimpleName() + indentation + string);
		g.handleInterrupt();
	}

	
}
