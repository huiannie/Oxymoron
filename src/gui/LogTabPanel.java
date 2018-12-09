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

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.text.DefaultStyledDocument;

public class LogTabPanel extends JPanel implements FontSizeEventListener {

	private static final long serialVersionUID = 9079862533706124826L;

	private static final int ExtraWindowWidth = 100;

	public static final String name = "Log";
	
	JTextPane textpane;
	JScrollPane scrollPane;
	GuiOutputPipe errpipe;
	
	public LogTabPanel() {
		super();
		create();
	}
	
	void create() {
		// Use border layout. 
		// Put a scollable text area on top.
		// Set the scrollPane at the center
		// and let the Border layout fill it
		// with the whole space dynamically.
		setLayout(new BorderLayout());

		// The text area is not editable.
		// It only displays output.
		DefaultStyledDocument document = new DefaultStyledDocument();
		textpane = new JTextPane(document);
		textpane.setEditable(false);

		errpipe = new GuiOutputPipe(textpane);

		scrollPane = new JScrollPane(textpane);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		
		
		JPanel inputPane = new JPanel();
		inputPane.setLayout(new BorderLayout());
		
		add(scrollPane, BorderLayout.CENTER);
		add(inputPane, BorderLayout.SOUTH);


		setFontSize();
	}
	
	void setFontSize() {
		FontSettings f = FontSettings.get();
		f.setFontSize(textpane);
	}

	
	public void clear() {
		textpane.setText("");
	}
	public GuiOutputPipe getOutPipe() {
		return errpipe;
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
