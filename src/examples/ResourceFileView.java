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

import java.io.File;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.filechooser.FileView;


public class ResourceFileView extends FileView {
	public static final boolean Debug = ResSettings.Debug;

	private static final String DirectoryIcon = ResSettings.DirectoryIcon;
	private static final String TxtIcon = ResSettings.TxtIcon;
	private static final String GenericIcon = ResSettings.GenericIcon;

	
	@Override
	public Icon getIcon(File f) {
		if (Debug) ResIOUtils.println(this.getClass().getSimpleName() + " Called getIcon() for " + f.getName());
		try {
			if (Debug) {
				ResIOUtils.print("Calling getIcon(" + f.getName() + ").  ");
				if (f instanceof Node) ResIOUtils.println(f.getName() + " is  node. ");
				else ResIOUtils.println(f.getName() + " is a generic file.");
			}

			String iconName;
			if (f instanceof Node) {
				iconName = (((Node)f).isDirectory()) ? DirectoryIcon : TxtIcon;
			}
			else {  // Some generic file from somewhere outside
				if (Node.isNamePossiblyDirectory(f.getName())) iconName = DirectoryIcon;
				else if (Node.isNameDataFile(f.getName())) iconName = TxtIcon;
				else iconName = GenericIcon;
			}
			
			java.net.URL url = getClass().getResource(iconName);
			return new ImageIcon(url);
		} catch (Exception e) {
			// Return an empty icon.
			return new ImageIcon();
		}
	}
	@Override
	public String getName(File f) {
		if (Debug) ResIOUtils.println(this.getClass().getSimpleName() + " Called getName() for " + f.getName());
		return f.getName();
	}
	@Override
	public Boolean isTraversable(File f) {
		if (Debug) ResIOUtils.println(this.getClass().getSimpleName() + " Called isTraverable() for " + f.getName());
		return f.isDirectory();
	}
}
