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
import javax.swing.filechooser.FileSystemView;

public class ResourceSystemView extends FileSystemView {
	private static final boolean Debug = ResSettings.Debug;
	
	private static final String DirectoryIcon = ResSettings.DirectoryIcon;
	private static final String TxtIcon = ResSettings.TxtIcon;
	
	Node resourceTree;

	
	public ResourceSystemView(Node resourceTree) {
		super();
		this.resourceTree = resourceTree;
	}
	
	
	@Override
	public File createFileObject(File dir, String filename) {
		if (Debug) ResIOUtils.println("Calling createFileObject on " + filename + " of dir " + (dir==null?"null":dir.getName()));
		return super.createFileObject(dir, filename);
	}

	@Override
	public File createFileObject(String path) {
		if (Debug) ResIOUtils.println("Calling createFileObject on " + path);
		
		// If the path is part of the tree and is unmounted, then mount the path and return the Node.
		if (path.startsWith(resourceTree.getName())) {
			String mountedpath = PathUtils.join(resourceTree.getRootMountpoint(), path);
			return resourceTree.getNode(mountedpath);
		}
		// If the path is part of the tree and is mounted, then return the Node
		else if (path.startsWith(resourceTree.getPath())) {
			return resourceTree.getNode(path);
		}
		else
			return super.createFileObject(path);
	}

	@Override
	protected File createFileSystemRoot(File f) {
		if (Debug) ResIOUtils.println("Calling createFileSystemRoot on " + (f==null?"null":f.getName()));
		if (f instanceof Node) {
			return super.createFileSystemRoot(f);
		}
		else {
			return super.createFileSystemRoot(f);
		}
	}
	
	@Override
	public File createNewFolder(File file) { return null; }

	@Override
	public File getChild(File dir, String filename) {
		if (Debug) ResIOUtils.println("Calling getChild on " + (dir==null?"null":dir.getName()) + " with " + filename);
		
		// If part of the tree, then search the tree
		if (dir instanceof Node) {
			Node n = (Node) dir;
			for (Node c : n.getChildren()) {
				if (c.getName().equals(filename)) return c;
			}
			return null;
		}
		// If the file is the root of the tree and the dir is the mount point, then return the root.
		// Note that in the tree is virtually mounted, not actually there.
		else if (dir.getPath().equals(resourceTree.getRootMountpoint()) && filename.equals(resourceTree.getName())) {
			return resourceTree;
		}
		// In all other cases, use the super method.
		else {
			return super.getChild(dir, filename);
		}
	}

	@Override
	public File getDefaultDirectory() {
		if (Debug) ResIOUtils.println("Calling getDefaultDirectory " + super.getDefaultDirectory().getAbsolutePath());
		return super.getDefaultDirectory();
	}
	
	@Override
	public File getParentDirectory(File dir) {
		if (Debug) ResIOUtils.println("Calling getParentDirectory() on " + (dir==null?"null-class":dir.getClass().getSimpleName()) + ": " + (dir==null?"null":dir.getName()));
		if (dir instanceof Node) {
			// If root, then return mount point.
			if (resourceTree.getPath().equals(dir.getPath())) {
				return resourceTree.getParentFile();
			}
			// If part of the resource tree but not the resource tree root, return the node's parent.
			else {
				return dir.getParentFile();
			}
		}
		// If not part of the tree, return the super method.
		else {
			return super.getParentDirectory(dir);
		}
	}
	
	@Override
	public File getHomeDirectory() {
		if (Debug) ResIOUtils.println("Calling getHomeDirectory() to get " + super.getHomeDirectory());
		return super.getHomeDirectory();
	}
	
	@Override
	public boolean isHiddenFile(final File f) {
		if (f instanceof Node) {
			Node n = (Node) f;
			if (n.isNameDataFile()) return false;
			if (n.isNamePossiblyDirectory()) return false;
			return true;
		}
		else {
			return super.isHiddenFile(f);
		}
	}
	
	@Override
	public boolean isFileSystem(File f) {
		if (Debug) ResIOUtils.println("Calling isFileSystem on " + (f==null?"null":f.getName()));
		if (f instanceof Node) {
			return !isFileSystemRoot(f);
		}
		else {
			return super.isFileSystem(f);
		}
	}
	@Override
	public boolean isFileSystemRoot(File f) {
		if (Debug) ResIOUtils.println("Calling isFileSystemRoot on " + (f==null?"null":f.getPath()));
		
		if (f instanceof Node) {
			// A node is not to be a file system root.
			return false;
		}
		else {
			return super.isFileSystemRoot(f);
		}
	}

	
	@Override
	public String getSystemDisplayName(File f) {
		if (Debug) ResIOUtils.println("Calling getSystemDisplayName() on " + (f==null?"null":f.getPath()));
		if (f instanceof Node) {
			Node node = (Node) f;
			return node.getName();
		}
		else {
			return super.getSystemDisplayName(f);
		}
	}
	
	@Override
	public boolean isParent(File folder, File file) {
		if (Debug) ResIOUtils.println("Calling isParent() on " + folder.getPath() + " for file " + (file==null?"null":file.getPath()));
		// 4 cases:
		if ((folder instanceof Node) && (file instanceof Node)) {
			// Both folder and file are part of the tree.
			if (file==null || file.getParent()==null) return false;
			return folder.getPath().equals(file.getParentFile().getPath());
		}
		else if ((folder instanceof Node) && !(file instanceof Node)) {
			// folder is part of the tree, file is not.
			if (file==null || file.getParent()==null) return false;
			return folder.getPath().equals(file.getParentFile().getPath());
		}
		else if (!(folder instanceof Node) && (file instanceof Node)) {
			// folder is not part of the tree but file is. (Root is in this case)
			if (file==null || file.getParent()==null) return false;
			return folder.getPath().equals(file.getParentFile().getPath());
		}
		else {
			return super.isParent(folder, file);
		}
	}
	
	
	@Override
	public File[] getFiles(File dir, boolean useFileHiding) {
		if (Debug) ResIOUtils.println("Calling getFiles() on " + (dir==null?"null":dir.getPath()));
		if (dir instanceof Node) {
			// Return children as an array of files.
			return dir.listFiles();
		}
		else if (dir.getAbsolutePath().equals(resourceTree.getRootMountpoint())) {
			// Get all existing children of mount point. Add the resource tree root to it.
			File[] real = dir.listFiles();
			File[] pseudo = new File[real.length+1];
			pseudo[0] = resourceTree;
			for (int index=0; index<real.length; index++)
				pseudo[index+1] = real[index];
			return pseudo;
		}
		else {
			return super.getFiles(dir, useFileHiding);
		}
	}
	
	@Override
	public Boolean isTraversable(File f) {
		if (Debug) ResIOUtils.println("Calling isTraversable??"); 

		if (f instanceof Node) {
			// Directories are traversable. Others are not.
			return ((Node)f).isDirectory();
		}
		else {
			return super.isTraversable(f);
		}
	}
	
	@Override
	public File[] getRoots() {
		if (Debug) ResIOUtils.println("Calling getRoots()");
		return super.getRoots();
	}
	
	@Override
	public Icon getSystemIcon(File f) {
		if (Debug) ResIOUtils.println("Calling getSystemIcon() returns null");
		if (f instanceof Node) {
			String iconName;
			iconName = (((Node)f).isDirectory()) ? DirectoryIcon : TxtIcon;
			java.net.URL url = getClass().getResource(iconName);
			return new ImageIcon(url);

		}
		else {
			return super.getSystemIcon(f);
		}
	}
	
	@Override
	public boolean isRoot(File f) {
		if (Debug) ResIOUtils.println("Calling isRoot(" + (f==null?"null":f.getName()) + ")");
		if (f instanceof Node) {
			// Nodes cannot be a system root.
			return false;
		}
		else {
			return super.isRoot(f);
		}
	}
	
	@Override
	public boolean isComputerNode(File dir) {
		return super.isComputerNode(dir);
	}

	@Override
	public boolean isFloppyDrive(File dir) {
		return super.isFloppyDrive(dir);
	}

	@Override
	public boolean isDrive(File dir) {
		return super.isDrive(dir);
	}
	@Override
	public String getSystemTypeDescription(File f) {
		if (f instanceof Node) {
			Node n = (Node) f;
			return n.getName();
		}
		else {
			return super.getSystemTypeDescription(f);
		}
	}

}
