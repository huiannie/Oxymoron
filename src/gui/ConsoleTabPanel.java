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
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.text.DefaultStyledDocument;


public class ConsoleTabPanel extends JPanel implements FontSizeEventListener {
	public static final boolean Debug = GuiSettings.Debug;

	private static final long serialVersionUID = 1457146944506143015L;
	
	private static final int ExtraWindowWidth = 100;

	public static final String name = "Console";

	private static final boolean echo = true;
	
	JTextPane textpane;
	JTextField textfield;
	JLabel label;
	JScrollPane scrollPane;
	GuiInputPipe inpipe;
	GuiOutputPipe outpipe;
	
	public ConsoleTabPanel() {
		super();
		create();
	}
	
	void create() {
		// Use border layout. 
		// Put a scollable text area on top.
		// Add a textfield after it.
		// Set the scrollPane at the center
		// and let the Border layout fill it
		// with the whole space dynamically.
		setLayout(new BorderLayout());

		// The text area is not editable.
		// It only displays output.
		DefaultStyledDocument document = new DefaultStyledDocument();
		textpane = new JTextPane(document);
		textpane.setEditable(false);
		
		outpipe = new GuiOutputPipe(textpane);

		scrollPane = new JScrollPane(textpane);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		
		
		// The textfield is for user to enter input.
		label = new JLabel("Input:");
		textfield = new JTextField();
		textfield.setEditable(true);
		textfield.setEnabled(false);
		
		inpipe = new GuiInputPipe(textfield, echo);

		textfield.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				if(e.getKeyChar()=='\n'){
					inpipe.setLine(textfield.getText());
					textfield.setText("");
				}
				super.keyReleased(e);
			}
		});

		
		JPanel inputPane = new JPanel();
		inputPane.setLayout(new BorderLayout());
		inputPane.add(label, BorderLayout.WEST);
		inputPane.add(textfield, BorderLayout.CENTER);
		
		add(scrollPane, BorderLayout.CENTER);
		add(inputPane, BorderLayout.SOUTH);


		setFontSize();
	}
	
	void setFontSize() {
		FontSettings f = FontSettings.get();
		f.setFontSize(textpane);
		f.setFontSize(label);
		f.setFontSize(textfield);
	}

	

	public GuiInputPipe getInPipe() {
		return inpipe;
	}
	public GuiOutputPipe getOutPipe() {
		return outpipe;
	}

	
	

	public void clear() {
		textpane.setText("");
	}
	
	@Override
	//Make the panel wider than it really needs, so
	//the window's wide enough for the tabs to stay
	//in one row.
	public Dimension getPreferredSize() {
		Dimension size = super.getPreferredSize();
		size.width += ExtraWindowWidth;
		return size;
	}

	@Override
	public void FontSizeEventOccurred(FontSizeEvent evt) {
		setFontSize();
	}


}
