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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;


public class ResourceTreeBuilder {
	public static final boolean Debug = ResSettings.Debug;

	public static final String delimiter = PathUtils.delimiter;

	private static final boolean ReconstructExamples = ResSettings.ReconstructExamples;
	public static final String RootPath = ResSettings.ResourceRootPath;
	public static final String StandAlone = ResSettings.StandAloneProgramDirectory;
	public static final String ObjectOriented = ResSettings.ObjectOrientedProgramDirectory;
	public static final String JarResourceLogPath = ResSettings.JarResourceLogPath;
	public static final String ResourceLogPath = ResSettings.SrcResourceLogPath;

	private static final String DefaultDirectory = ".";

	
	// The rootpath could potentially contain multiple levels. For example: "examples/text/"
	// The root is the head of the rootpath. That would be "examples".
	// The anchor is the tail of the rootpath. That would be "text".
	// Every level of the rootpath needs to be translated to a node in the tree so that the resource can be retrieved.
	// The anchor is the virtual root to appear on the file-chooser when user searches for files..
	private String rootpath;
	private Node root;
	private Node anchor;
	
	public ResourceTreeBuilder() {
		rootpath = RootPath;
		String names[] = rootpath.split(delimiter);		
		if (names.length>=1) {
			root = new Node(names[0], null);
			anchor = root;
		}
		for (int index=1; index<names.length; index++) {
			anchor.addChild(new Node(names[index], anchor));
			anchor = anchor.getChildren().get(0);
		}
	}

	
	public Node buildTree() {
		try {
			if (Debug) ResIOUtils.println("Building resource tree rooted at " + rootpath);
			build(anchor);
			if (Debug) ResIOUtils.println(ResourceTreeBuilder.class.getSimpleName() 
					+ ": Finished building tree at " + root.getPath() + "\n");
	

			if (anchor.getChildren().size()==0) {
				// Need to load it from jar.
				if (Debug) ResIOUtils.println("Building resource tree from log.");
				String logpath = JarResourceLogPath;
				buildFromLog(logpath);
				// No need to save the tree in any log.
			}
			else {
				// It is know that we are running the program at the development stage.
				// There is no jar file. All the examples are available in the source directory.
				// The default settings is to leave the logfile as such.
				// If we have updated the examples, then adjust the settings to reconstruct the examples.
				if (ReconstructExamples) {
					// Update the log file in directory. This file will be used for constructing
					// the same directory from jar.
					String logpath = ResourceLogPath;
					saveFileLog(logpath);
				}
			}
			
			File currentDirectory = new File(new File(DefaultDirectory).getCanonicalPath());
			String mountpoint = currentDirectory.getAbsolutePath();
			root.mount(mountpoint);
			if (Debug) ResIOUtils.println("Mounting root at " + mountpoint);
			return root;
			
		} catch (IOException e1) {
			if (Debug) e1.printStackTrace();
			return null;
		} catch (Exception e2) {
			if (Debug) e2.printStackTrace();
			return null;
		}
	}
	
	

	private void buildFromLog(String logpath) throws ResBugTrap {
		Node node = new Node(logpath, null);
		String[] list = node.readlines();
		
		String rootpathtokens[] = rootpath.split(delimiter);
		
		for (String path : list) {
			String tokens[] = path.split(delimiter);
			
			boolean onPath = true;
			for (int index=0; index<rootpathtokens.length; index++) {
				if (!tokens[index].equals(rootpathtokens[index])) onPath = false;
			}
			if (onPath) {
				Node p = anchor;
				for (int index=rootpathtokens.length; index<tokens.length; index++) {
					String name = tokens[index];
					Node q = null;
					for (Node c : p.getChildren()) {
						if (c.getName().equals(name)) {
							// the name already exists in the tree. 
							q = c;
						}
					}
					// If the name is not already in the tree, create a node for it.
					if (q==null) {
						q = new Node(name, p);
						p.addChild(q);
					}
					// Now move on to the node.
					p = q;
				}
			}
		}
	}
	
	private void build(Node node) throws ResBugTrap {
		if (node.isDirectory()) {
			List<String> resourceNames = node.listResourceDirectory();
			for (String name : resourceNames) {
				Node child = new Node(name, node);
				node.addChild(child);
				build(child);
			}			
		}
	}
	
	private void saveFileLog(String pathname) throws ResBugTrap {
		try {
			PrintStream p = new PrintStream(new File(pathname));
			saveFileLog(p);
			p.close();
		} catch (FileNotFoundException e) {
			throw new ResBugTrap(e);
		}
	}
	private void saveFileLog(PrintStream outstream) {
		Queue<Node> q = new LinkedList<Node>();
		q.add(root);
		while (!q.isEmpty()) {
			Node n = q.poll();
			outstream.println(n.getPath());
			for (Node c : n.getChildren())
				q.add(c);
		}
	}
	

	public void print(int indent) {
		Queue<Node> queue = new LinkedList<Node>();
		queue.add(root);
		while (!queue.isEmpty()) {
			Node p = queue.poll();
			if (p.getChildren().size()==0) {
				p.print(indent);
			}
			else {
				for (Node child : p.getChildren()) {
					queue.add(child);
				}
			}
		}
	}

	  
	public static void main(String[] args) throws Exception {
		new ResourceTreeBuilder().buildTree().print(0);
	}

}
