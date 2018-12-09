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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;


public class Node extends File {
	private static final boolean Debug = ResSettings.Debug;

	private static final long serialVersionUID = 6850029830107632194L;

	public static final String FileType = ResSettings.ResourceType;
	public static final String LogType = ResSettings.LogType;
	public static final String ImageType = ResSettings.ImageType;
	public static final String ClassType = ResSettings.ClassType;
	public static final String HiddenType = ResSettings.HiddenType;
	
	public static final String delimiter = PathUtils.delimiter;

	public static final String StandAlone = ResSettings.StandAloneProgramDirectory;
	public static final String ObjectOriented = ResSettings.ObjectOrientedProgramDirectory;
	
	private String name;
	private Node parent;
	private ArrayList<Node> children;
	private String mountpoint = null;
	
	public Node(String name, Node parent) {
		super(name);
		this.name = name;
		this.parent = parent;
		this.children = new ArrayList<Node>();
	}
	public void addChild(Node child) {
		children.add(child);
	}
	@Override
	public String getName() {
		return name;
	}
	@Override
	public File getParentFile() {
		if (Debug) ResIOUtils.println(this.getClass().getSimpleName() + ": calling getparentFile() on " + getName());
		if (mountpoint!=null) {
			// This is root. Its parent is a normal File, not a node.
			return new File(mountpoint);
		}
		else {
			// This is not root. Its parent is a Node.
			return parent;
		}
	}
	public ArrayList<Node> getChildren() {
		return children;
	}
	@Override
	public String getPath() {
		String s = name;
		Node p = this;
		while (p.parent!=null) {
			if (p.parent.getName()==delimiter)
				s = p.parent.getName() + s;
			else
				s = p.parent.getName() + delimiter + s;
			p = p.parent;
		}
		
		// When p.parent==null, we have hit the root.
		// If root is mounted, add the path of the mountpoint.
		if (p.mountpoint!=null) s = PathUtils.join(p.mountpoint, s);
		
		if (Debug) ResIOUtils.println(Node.class.getSimpleName()+ " " + getName() + " getPath() as " + s);
		return s;
	}
	
	public void print(int indent) {
		ResIOUtils.printIndented(indent, getPath());
	}
	
	
	
	@Override
	public boolean exists() { 
		if (Debug) ResIOUtils.println("Checking if " + this.name + " exists. Yes, it does.");
		return true;
	}

	@Override
	public boolean isAbsolute() {
		if (Debug) ResIOUtils.println("Checking if " + this.name + " is absolute. Yes, it is.");
		return true;
	}
	
	@Override
	public File[] listFiles() {
		if (Debug) ResIOUtils.println(this.getClass().getSimpleName() + ": calling listFiles() for " + getName());
		if (getChildren().size()==0) return new Node[0];
		
		Node[] array = getChildren().toArray(new Node[1]);
		if (Debug) ResIOUtils.println("Number of children found: " + array.length);
		if (Debug) {
			for (Node n : array) {
				ResIOUtils.printIndented(1, n.getName());
			}
			ResIOUtils.println("End of children list of " + getName());
		}
		return array;
	}
	
	@Override
	public String getParent() {
		if (Debug) ResIOUtils.println(this.getClass().getSimpleName() + ": calling getparent() on " + getName());
		if (getParentFile()==null) return null;
		return getParentFile().getPath();
	}
	
	
	@Override
	public boolean isDirectory() {
		boolean result = isNamePossiblyDirectory(getName());
		if (Debug) ResIOUtils.println(this.getClass().getSimpleName() + ": calling isDirectory() on " + getName() + "? " + result);
		return result;
	}
	
	@Override
	public String getAbsolutePath() {
		if (Debug) ResIOUtils.println(this.getClass().getSimpleName() + ": calling getAbsolutePath() on " + getName());
		String path = getPath();
		if (Debug) ResIOUtils.println("   Returning node=" + getName() + " path=" + path);
		return path;
	}
	
	public File getFile() {
		return this;
	}
	
	private Node getRoot() {
		if (parent==null) return this;
		else return parent.getRoot();
	}
	public String getRootMountpoint() {
		return getRoot().mountpoint;
	}

	public static boolean isNamePossiblyDirectory(String name) {
		if (name.endsWith(FileType)) return false;
		if (name.endsWith(LogType)) return false;
		if (name.endsWith(ClassType)) return false;
		if (name.endsWith(ImageType)) return false;
		if (name.startsWith(".")) return false;
		return true;
	}
	public static boolean isNameDataFile(String name) {
		if (name.endsWith(FileType)) return true;
		return false;
	}
	public boolean isNamePossiblyDirectory() {
		return isNamePossiblyDirectory(getName());
	}
	public boolean isNameDataFile() {
		return isNameDataFile(getName());
	}

	
	// https://stackoverflow.com/questions/3923129/get-a-list-of-resources-from-classpath-directory
	ArrayList<String> listResourceDirectory() throws ResBugTrap {
		if (Debug) ResIOUtils.println(ResourceTreeBuilder.class.getSimpleName() + ": Opening resource path: " + getPath());
		if (!isNamePossiblyDirectory(getName())) return null;

		String filepath = getPath();
		filepath = purgeMountPoint(filepath);

		// Resource is a directory file located in bin/
		// It may include hidden files and class files.
		// Exclude hidden files and class files.
		ArrayList<String> filenames = new ArrayList<String>();
		try {
			InputStream in = getResourceAsStream(filepath);
			BufferedReader br = new BufferedReader( new InputStreamReader(in) );
			String resource;
			while( (resource = br.readLine()) != null ) {
				if (Debug) ResIOUtils.println("   Read in line : " + resource);
				if (isNamePossiblyDirectory(resource) 
					|| isNameDataFile(resource))
					filenames.add(resource);
			}
		}
		catch(IOException e) {
			throw new ResBugTrap(e);
		}
		return filenames;
	}
	
	public String[] readlines() {
		String filepath = getPath();
		filepath = purgeMountPoint(filepath);

		// Read all lines.
		ArrayList<String> lines = new ArrayList<String>();
		InputStream in = null;
		try {
			in = getResourceAsStream(filepath);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			
			String resource;
			while( (resource = br.readLine()) != null ) {
				lines.add( resource );
			}
			in.close();
		}
		catch(IOException e) {
			try { if (in!=null) in.close(); } catch (IOException e1) {}
			// If fail to read a resource file, return an empty array of strings.
			return new String[0];
		}
		return lines.toArray(new String[1]);
	}

	private InputStream getResourceAsStream( String resource ) {
		final InputStream in = getContextClassLoader().getResourceAsStream( resource );
		return in == null ? getClass().getResourceAsStream( resource ) : in;
	}

	private ClassLoader getContextClassLoader() {
		return Thread.currentThread().getContextClassLoader();
	}
	public void mount(String mountpoint) {
		// Make sure the mount point is set at the root and not anywhere else in the tree.
		// The root is identified as the node without parent.
		if (this.parent==null) {
			this.mountpoint = mountpoint;
			if (Debug) ResIOUtils.println("Mounting root at " + mountpoint);
		}
		else
			parent.mount(mountpoint);
	}
	
	private String purgeMountPoint(String filepath) {
		String rootmountpoint = getRootMountpoint();
		if (rootmountpoint!=null) {
			if (filepath.startsWith(rootmountpoint)) {
				filepath = filepath.substring(rootmountpoint.length());
				if (filepath.startsWith(delimiter))
					filepath = filepath.substring(1);
			}
		}
		return filepath;
	}
	
	
	Node getNode(String path) {
		String rootmountpoint = getRootMountpoint();
		if (!path.startsWith(rootmountpoint)) path = PathUtils.join(rootmountpoint, path);
		
		if (getPath().equals(path)) return this;

		// If any existing children matches exactly, then return the one that matches.
		for (Node child : getChildren()) {
			if (child.getPath().equals(path)) return child;
		}
		// None of the existing children matches exactly, search the one that is on the path.
		for (Node child : getChildren()) {
			if (path.startsWith(child.getPath())) return child.getNode(path);
		}
		
		if (Debug) ResIOUtils.println("Path " + path + " cannot be found in the tree.");
		return null;
	}

	public Node getStandAloneResourceNode() {
		return getNode(StandAlone);
	}
	public Node getObjectOrientedResourceNode() {
		return getNode(ObjectOriented);
	}
	public String getStandAloneResourcePath() {
		return getNode(StandAlone).getPath();
	}
	public String getObjectOrientedResourcPath() {
		return getNode(ObjectOriented).getPath();
	}

}
