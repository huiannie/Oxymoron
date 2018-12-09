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
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import examples.ResourceFileView;
import examples.ResourceSystemView;



public class MainMenuBar extends JMenuBar {

	private static final long serialVersionUID = 1387600013524006385L;

	public static final int NoKeyStroke = -1;
	
	private static final Dimension FileChooserDim = new Dimension(800,600);
	private static final String DefaultDirectory = "user.dir";
	
	private static final long Pause = 500;

	private Engine engine;
	
	private JMenu fileMenu;
	private JMenuItem menuItemLoad;
	private JMenuItem menuItemBatchLoad;
	private JMenuItem menuItemQuit;
	
	private JMenu testMenu;
	private JMenuItem menuItemRun;
	private JMenuItem menuItemStop;
	
	private JMenu appearanceMenu;
	private JMenu fontSubmenu;
	private FontSizesListPanel fontSizesListPanel;
	
	private JMenu examplesMenu;
	private JMenuItem menuItemExamples1;
	private JMenuItem menuItemExamples2;

	public MainMenuBar(Engine engine) {
		super();
		this.engine = engine;
		create();
	}
	
	
	public void create() {
		
		//Build the FILE menu.
		fileMenu = new CustomMenu("File", KeyEvent.VK_F, "File");
		this.add(fileMenu);
		menuItemLoad = new CustomMenuItem("Load Standalone Program", KeyEvent.VK_O, "Load Standalone Program");
		menuItemBatchLoad = new CustomMenuItem("Load Object-oriented Program", KeyEvent.VK_B, "Load Object-oriented Program");
		menuItemQuit = new CustomMenuItem("Quit", KeyEvent.VK_Q, "Quit");
		fileMenu.add(menuItemLoad);
		fileMenu.add(menuItemBatchLoad);
		fileMenu.add(menuItemQuit);
		
		
		
		//Build the TEST menu.
		testMenu = new CustomMenu("Test", KeyEvent.VK_T, "Test");
		this.add(testMenu);
		menuItemRun = new CustomMenuItem("Run", KeyEvent.VK_R, "Run");
		menuItemStop = new CustomMenuItem("Stop", KeyEvent.VK_S, "Stop");
		testMenu.add(menuItemRun);
		testMenu.add(menuItemStop);

		
		
		//Build the Appearance menu.
		appearanceMenu = new CustomMenu("Appearance", KeyEvent.VK_A, "Appearance");
		this.add(appearanceMenu);
		fontSubmenu = new CustomMenu("Font", NoKeyStroke, "Font");
		// Attach a list of font sizes to the font menu.
		fontSizesListPanel = new FontSizesListPanel();
		fontSubmenu.add(fontSizesListPanel);
		appearanceMenu.add(fontSubmenu);
		
		
		
		// Build the EXAMPLES menu.
		examplesMenu = new CustomMenu("Examples", KeyEvent.VK_E, "Browse examples");
		// The example menu is added only when resource is available.
		if (engine.isResourceAvailable())
			this.add(examplesMenu);
		menuItemExamples1 = new CustomMenuItem("Load Standalone Example", KeyEvent.VK_X, "Load Standalone Example");
		menuItemExamples2 = new CustomMenuItem("Load Object-oriented Example", NoKeyStroke, "Load Object-oriented Example");
		examplesMenu.add(menuItemExamples1);
		examplesMenu.add(menuItemExamples2);

		
		
		
		
		// Add listeners
		
		// Listener to LOAD a Standalone program file
		menuItemLoad.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				File file = openFile();
				if (file!=null) {
					stopAnyRunningProgram();
					engine.callbackOpenFile(file);
				}
			};
		});
		// Listener to LOAD a batch of Object-Oriented program files
		menuItemBatchLoad.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				File[] files = openFiles();
				if (files!=null) {
					stopAnyRunningProgram();
					engine.callbackOpenFiles(files);
				}
			};
		});
		// Listener to RUN
		menuItemRun.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				engine.callbackRun();
			};
		});
		// Listener to STOP
		menuItemStop.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				engine.callbackStop();
			};
		});
		// Listener to QUIT
		menuItemQuit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			};
		});
		// Listener to LOAD a standalone Resource file
		menuItemExamples1.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				File file = openResource();
				if (file!=null) {
					stopAnyRunningProgram();
					engine.callbackOpenFile(file);
				}
			};
		});
		// Listener to LOAD a batch of Object-Oriented Resource files
		menuItemExamples2.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				File[] files = openResources();
				if (files!=null) {
					stopAnyRunningProgram();
					engine.callbackOpenFiles(files);
				}
			};
		});
	}
	
	public FontSizesListPanel getFontSizesListPanel() {
		return fontSizesListPanel;
	}

	
	private File openFile() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setPreferredSize(FileChooserDim);
		fileChooser.setCurrentDirectory(new File(System.getProperty(DefaultDirectory)));
		fileChooser.setMultiSelectionEnabled(false);
		int result = fileChooser.showOpenDialog(this);
		if (result == JFileChooser.APPROVE_OPTION) {
			File selectedFile = fileChooser.getSelectedFile();
			return selectedFile;
		}
		return null;
	}
	
	private File[] openFiles() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setPreferredSize(FileChooserDim);
		fileChooser.setCurrentDirectory(new File(System.getProperty(DefaultDirectory)));
		fileChooser.setMultiSelectionEnabled(true);
		int result = fileChooser.showOpenDialog(this);
		if (result == JFileChooser.APPROVE_OPTION) {
			File[] selectedFiles = fileChooser.getSelectedFiles();
			return selectedFiles;
		}
		return null;
	}

	private File openResource() {
		// Check if resource is available
		if (!engine.isResourceAvailable()) return null;

		// Create a file chooser that is rooted at the anchor of v.
		ResourceSystemView v = new ResourceSystemView(engine.getResourceTree());
		JFileChooser fileChooser = new JFileChooser(v);
		fileChooser.setFileView(new ResourceFileView());
		// Now set a new current directory.
		fileChooser.setCurrentDirectory(engine.getResourceTree().getStandAloneResourceNode());

		fileChooser.setPreferredSize(FileChooserDim);
		fileChooser.setMultiSelectionEnabled(false);
		
		int result = fileChooser.showOpenDialog(this);		
		if (result == JFileChooser.APPROVE_OPTION) {
			File selectedFile = fileChooser.getSelectedFile();
			return selectedFile;
		}
		return null;
	}

	private File[] openResources() {
		// Check if resource is available
		if (!engine.isResourceAvailable()) return null;

		// Create a file chooser that is rooted at the anchor of v.
		ResourceSystemView v = new ResourceSystemView(engine.getResourceTree());
		JFileChooser fileChooser = new JFileChooser(v);
		fileChooser.setFileView(new ResourceFileView());
		// Now set a new current directory.
		fileChooser.setCurrentDirectory(engine.getResourceTree().getObjectOrientedResourceNode());
		fileChooser.setPreferredSize(FileChooserDim);
		fileChooser.setMultiSelectionEnabled(true);
		int result = fileChooser.showOpenDialog(this);
		if (result == JFileChooser.APPROVE_OPTION) {
			File[] selectedFiles = fileChooser.getSelectedFiles();
			return selectedFiles;
		}
		return null;
	}


	static class CustomMenu extends JMenu {
		private static final long serialVersionUID = 1582635501568572338L;

		public CustomMenu(String name, int keystroke, String description) {
			super(name);
			setMnemonic(keystroke);
			getAccessibleContext().setAccessibleDescription(description);
		}
	}
	
	static class CustomMenuItem extends JMenuItem {
		private static final long serialVersionUID = 8835708927484111073L;

		public CustomMenuItem(String label, int keystroke, String description) {
			super(label, keystroke);
			getAccessibleContext().setAccessibleDescription(description);
		}
	}
	
	private boolean isRunning() {
		return menuItemStop.isEnabled();
	}

	private void stopAnyRunningProgram() {
		if (isRunning()) {
			engine.callbackStop();
			try { Thread.sleep(Pause); } catch (InterruptedException ignore) {}
		}
	}
	
	public void enableTestMenu() {
		menuItemRun.setEnabled(true);
		menuItemStop.setEnabled(false);
	}
	public void enableTestMenuDuringTest() {
		menuItemRun.setEnabled(false);
		menuItemStop.setEnabled(true);
	}
	public void disableTestMenu() {
		menuItemRun.setEnabled(false);
		menuItemStop.setEnabled(false);
	}

}
