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

import java.io.OutputStream;
import java.io.PrintStream;

import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyledDocument;

public class GuiOutputPipe extends PrintStream {
	private static final int Limit = 5000;
	private JTextPane outputPane;
	
	private static final OutputStream outstream = System.out;
	private GuiOutputPipe() {
		super(outstream);
	}

	public GuiOutputPipe(JTextPane outputField) {
		this();
		this.outputPane = outputField;
	}
	
	public void close() {
		outputPane = null;
	}
	
	

	public void print(String line, Style style) {
		if (outputPane!=null) {
			if (outputPane.getText().length()>Limit) outputPane.setText("");
			
			StyledDocument doc = outputPane.getStyledDocument();
			try {
				doc.insertString(doc.getLength(), line, style);
			} catch (BadLocationException e) {
				e.printStackTrace();
				outputPane.setText("");  // reset the output panel.
			}
			outputPane.setCaretPosition( outputPane.getDocument().getLength());
		}
	}
	public void println(String line, Style style) {
		print(line + "\n", style);
	}
	
	public void print(String line) {
		print(line, null);
	}


	public void println(String line) {
		print(line + "\n");
	}

	public void println() {
		print("\n");
	}
	

}
