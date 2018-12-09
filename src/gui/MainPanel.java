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
import java.awt.Image;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import compiler.parser.Cmds;


public class MainPanel extends JPanel {

	private static final long serialVersionUID = 188642703540159183L;
	
	private static final String Logo = GuiSettings.Logo;
	
	private static final int ConsolePosition = 0;
	private static final int LogPosition = 1;
	private static final int ProgramPosition = 2;
	
	private MainMenuBar menuBar;
	private JTabbedPane tabbedPanel;
	private ConsoleTabPanel consolePanel;
	private LogTabPanel logPanel;
	private ArrayList<FileTabPanel> filePanels = new ArrayList<FileTabPanel>();
	
	private Image logoImage;
	
	public MainPanel(String title, Engine engine) {
		java.net.URL url = getClass().getResource(Logo);
		logoImage = new ImageIcon(url).getImage();
		
		setPreferredSize(new Dimension(640, 480));
		setLayout(new BorderLayout());

		menuBar = new MainMenuBar(engine);
		add(menuBar, BorderLayout.NORTH);

		disableTestMenu();

		tabbedPanel = new JTabbedPane();
	
		
		consolePanel = new ConsoleTabPanel();
		tabbedPanel.insertTab(ConsoleTabPanel.name, null, consolePanel, null, ConsolePosition);
		
		logPanel = new LogTabPanel();
		tabbedPanel.insertTab(LogTabPanel.name, null, logPanel, null, LogPosition);
		
		add(tabbedPanel, BorderLayout.CENTER);
		
		
		/* Hook up this GUI with the source of data */
		engine.mainPanel = this;
		engine.callbackRedirectStdout(consolePanel.getOutPipe());
		engine.callbackRedirectStderr(logPanel.getOutPipe());
		engine.callbackRedirectStdin(consolePanel.getInPipe());
		
		/* hook up the two default panels for font size update */
		menuBar.getFontSizesListPanel().addFontSizeEventListener(consolePanel);
		menuBar.getFontSizesListPanel().addFontSizeEventListener(logPanel);
		
	}

	public void addFilePane(Cmds cmds, boolean isProgram) {
		FileTabPanel filePanel = new FileTabPanel(cmds);
		menuBar.getFontSizesListPanel().addFontSizeEventListener(filePanel);
		filePanels.add(filePanel);
		if (isProgram) {
			tabbedPanel.insertTab(cmds.getFilename(), null, filePanel, null, ProgramPosition);
			tabbedPanel.setSelectedComponent(filePanel);
		}
		else {
			tabbedPanel.addTab(cmds.getFilename(), filePanel);
			tabbedPanel.setSelectedIndex(ProgramPosition);
		}
	}
	public void removeAllFilePanels() {
		for (FileTabPanel p : filePanels) {
			menuBar.getFontSizesListPanel().removeFontSizeEventListener(p);
			tabbedPanel.remove(p);
		}
	}
	
	public void resetContents() {
		removeAllFilePanels();
		consolePanel.clear();
		// logPanel.clear();
	}
	public void setConsoleSelected() {
		tabbedPanel.setSelectedIndex(ConsolePosition);
	}
	public void enableTestMenu() {
		menuBar.enableTestMenu();
	}
	public void enableTestMenuDuringTest() {
		menuBar.enableTestMenuDuringTest();
	}
	public void disableTestMenu() {
		menuBar.disableTestMenu();
	}
	public Image getLogo() {
		return logoImage;
	}
	
	public static void createAndShowGui(String title, Engine engine) {
		try {
			JFrame frame = new JFrame(title);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

			MainPanel mainPanel = new MainPanel(title, engine);
			frame.add(mainPanel);
			frame.setIconImage(mainPanel.getLogo());
			frame.pack();
			frame.setLocationRelativeTo(null);
			frame.setVisible(true);
		}
		catch(Exception e) {
			// The Gui seems to only throw exceptions, not BugTraps.
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}

}
