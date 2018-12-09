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

import examples.objectoriented.ObjectOrientedExamples;
import examples.standalone.StandaloneExamples;

public class ResSettings {
	public static final boolean Debug = false;
	
	public static final String ResourceType = ".txt";
	public static final String LogType = ".log";
	public static final String ClassType = ".class";
	public static final String ImageType = ".png";
	public static final String HiddenType = ".";
	public static final String ChapterLabel = "ch";
	
	// The following are the settings for the examples embedded with the source.
	public static final boolean ReconstructExamples = true;

	static final String ResourceRootPath = PathUtils.packageNameToPath(ResSettings.class.getPackageName());
	public static final String StandAloneProgramDirectory = PathUtils.packageNameToPath(StandaloneExamples.getPackage());
	public static final String ObjectOrientedProgramDirectory = PathUtils.packageNameToPath(ObjectOrientedExamples.getPackage());

	// Depending on whether the program is run during testing or after delivery,
	// the logfile may be found in different location.
	static final String ResourceLogName = "resourcelist" + LogType;	
	static final String JarResourceLogPath = PathUtils.join(ResourceRootPath, ResourceLogName);
	static final String SrcResourceLogPath = PathUtils.join(PathUtils.join("src", ResourceRootPath), ResourceLogName);

	static final String DirectoryIcon = "icon-directory-16.png";
	static final String TxtIcon = "icon-txt-16.png";
	static final String GenericIcon = "icon-generic-16.png";
	
	



}
