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

import javax.swing.JTextField;

import compiler.util.BugTrap;

public class GuiInputPipe extends Interruptable {

	private static final long Interval = 100;
	private JTextField inputField;
	private String buffer;
	private boolean echo;

	public GuiInputPipe(JTextField inputField, boolean echo) {
		this.inputField = inputField;
		this.buffer = null;
		this.echo = echo;
	}
	
	public void close() {
		inputField = null;
		buffer = null;
	}
	public void setLine(String line) {
		buffer = line;
	}
	
	public String getLine() throws BugTrap {
		if (inputField!=null) {
			// Enable editing on the GUI end.
			inputField.setEnabled(true);
			inputField.requestFocus();

			// As long as data is not yet available, keep polling.
			while (buffer==null) {
				// Sleep for a while before polling again.
				try {
					Thread.sleep(Interval);
					
				} catch (InterruptedException e) {
					throw new BugTrap(e);
				}
				if (isInterrupted()) {
					// Before handling the interrupt, cleanup locally.
					// Reset the buffer and disable editing on the GUI end.
					buffer = null;
					inputField.setEnabled(false);
					handleInterrupt();
				}
				
				// If at any time the GUI end is broken, terminate the loop.
				if (inputField==null) break;
			}
			// Data has been received from the GUI end.
			String inputString = buffer;
			
			// Reset the buffer and disable editing on the GUI end.
			buffer = null;
			inputField.setEnabled(false);
			
			// If the GUI has requested for echoing, echo data back to stdout.
			if (echo) 
				GUIOUtils.printlnGreen(inputString);
			
			// Return the input string to caller.
			return inputString;
		}
		throw new BugTrap("Input TextField not available.");
	}
}
