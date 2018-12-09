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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

import compiler.parser.Cmds;



public class FileTabPanel extends JPanel implements FontSizeEventListener {

	private static final long serialVersionUID = 1457146944506143015L;
	
	private final static int ExtraWindowWidth = 100;
	
	JTextArea textarea;
	JTextArea linenumbersArea;
	JScrollPane verticalScrollPane;
	JRadioButton rawButton;
	JRadioButton parsedButton;
	Cmds cmds;
	
	public FileTabPanel(Cmds cmds) {
		super();
		this.cmds = cmds;
		create();
	}
	
	void create() {
		// Use border layout. 
		// Set the scrollPane at the center
		// and let the Border layout fill it
		// with the whole space dynamically.
		setLayout(new BorderLayout());

		Border border = new LineBorder(Color.LIGHT_GRAY, 1);
		// The text area is not editable.
		// It only displays output.
		linenumbersArea = new JTextArea();
		linenumbersArea.setBorder(border);
		linenumbersArea.setLineWrap(false);
		linenumbersArea.setWrapStyleWord(false);
		linenumbersArea.setEditable(false);
		
		textarea = new JTextArea();
		textarea.setBorder(border);
		textarea.setLineWrap(false);
		textarea.setWrapStyleWord(false);
		textarea.setEditable(false);

		
		JPanel parallelPanel = new JPanel();
		parallelPanel.setLayout(new BorderLayout());
		parallelPanel.add(linenumbersArea, BorderLayout.WEST);

		parallelPanel.add(textarea, BorderLayout.CENTER);
		
		verticalScrollPane = new JScrollPane(parallelPanel);
		verticalScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		
		
		// Radiobuttons to switch among different views of the file
		
		// Raw shows the original file
		String raw = "Raw";
		rawButton = new JRadioButton(raw);
		rawButton.setActionCommand(raw);
		rawButton.setSelected(true);
		
		// Parsed shows the file after parsing.
		String parsed = "Parsed";
		parsedButton = new JRadioButton(parsed);
		parsedButton.setActionCommand(parsed);
		parsedButton.setSelected(false);

		ButtonGroup group = new ButtonGroup();
		group.add(rawButton);
		group.add(parsedButton);
		
		JPanel inputPane = new JPanel();
		inputPane.setLayout(new BorderLayout());
		inputPane.add(rawButton, BorderLayout.WEST);
		inputPane.add(parsedButton);

		// Put the text area at the center. 
		// Put the buttons below it.
		add(verticalScrollPane, BorderLayout.CENTER);
		add(inputPane, BorderLayout.SOUTH);
		
		
		// Set the panel to show raw text
		setRawText();
		
		
		
		// Set up callbacks for button.
		rawButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setRawText();
			};
		});
		parsedButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setParsedText();
			};
		});
		
		

		
		setFontSize();
	}
	
	void setFontSize() {
		FontSettings f = FontSettings.get();
		f.setFontSize(linenumbersArea);
		f.setFontSize(textarea);
		f.setFontSize(rawButton);
		f.setFontSize(parsedButton);
	}
	
	
	

	private void setRawText() {
		linenumbersArea.setText(getCmdsRawLineNumbers());
		textarea.setText(getCmdsRawText());
	}
	private void setParsedText() {
		linenumbersArea.setText(getCmdsParsedLineNumbers());
		textarea.setText(getCmdsParsedText());
	}
	
	private String getCmdsRawLineNumbers() {
		String s = "";
		// Remember that line numbers stored start at zero.
		// But when they are displayed, they are offset by 1.
		for (int index=0; index<cmds.getRawLines().length; index++) {
			s += (index+1) + "\n";
		}
		return s;
	}
	private String getCmdsParsedLineNumbers() {
		// Remember that line numbers stored start at zero.
		// But when they are displayed, they are offset by 1.
		boolean startAt1 = true;
		
		String s = "";
		for (int index=0; index<cmds.getNumberOfLines(); index++) {
			s += cmds.matchRawlineNumbersAsString(index, startAt1) + "\n";
		}
		return s;
	}
	
	private String getCmdsRawText() {
		String s = "";
		for (String rawline : cmds.getRawLines()) {
			s += rawline + "\n";
		}
		return s;
	}
	private String getCmdsParsedText() {
		String s = "";
		for (int index=0; index<cmds.getNumberOfLines(); index++)
			s += cmds.getCmdLine(index) + "\n";
		return s;
	}
	
	
	@Override
	public void FontSizeEventOccurred(FontSizeEvent evt) {
		setFontSize();
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
}
