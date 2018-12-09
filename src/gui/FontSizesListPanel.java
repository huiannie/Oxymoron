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

import javax.swing.BorderFactory;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.EventListenerList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class FontSizesListPanel extends JPanel {
	private static final long serialVersionUID = -2424853413829384890L;

	protected EventListenerList listenerList = new EventListenerList();
	
	JList<String> fontSizesList;
	
	public FontSizesListPanel() {
		super();
		
		setLayout(new BorderLayout());

		FontSettings f = FontSettings.get();
		fontSizesList = new JList<String>(f.getLabels());
		fontSizesList.setSelectedIndex(f.getChoice());
		fontSizesList.setVisible(true);
		add(fontSizesList);
		
		Dimension d = fontSizesList.getPreferredSize();
		d.width = (int) (d.width * 1.5);
		fontSizesList.setPreferredSize(d);
		
		Border loweredbevel = BorderFactory.createLoweredBevelBorder();
		TitledBorder title = BorderFactory.createTitledBorder(loweredbevel, "Size");
		title.setTitlePosition(TitledBorder.ABOVE_TOP);
		setBorder(title);
		

		// Add listener for font selection change.
		fontSizesList.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent event) {
				if (event != null && event.getValueIsAdjusting()) {
					return;
				}
				if (event == null) {
					return;
				}
				FontSettings f = FontSettings.get();
				int newChoice = fontSizesList.getSelectedIndex();
				f.setChoice(newChoice);
								
				// Selection has been made. Close the menu.
				javax.swing.MenuSelectionManager.defaultManager().clearSelectedPath();
				
				fireEvent(new FontSizeEvent(this));
			};
		});
	}


	void fireEvent(FontSizeEvent evt) {
		Object[] listeners = listenerList.getListenerList();
		for (int i = 0; i < listeners.length; i = i+2) {
			if (listeners[i] == FontSizeEventListener.class) {
				((FontSizeEventListener) listeners[i+1]).FontSizeEventOccurred(evt);
			}
		}
	}
	
	public void addFontSizeEventListener(FontSizeEventListener listener) {
		listenerList.add(FontSizeEventListener.class, listener);
	}
	public void removeFontSizeEventListener(FontSizeEventListener listener) {
		listenerList.remove(FontSizeEventListener.class, listener);
	}
	
	
}
