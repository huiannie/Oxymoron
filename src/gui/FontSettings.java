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
import javax.swing.JComponent;

public class FontSettings {
	private static int choice = 3;
	private static final float[] fontSizes = {8, 10, 11, 12, 14, 16, 18, 20, 22, 24, 28, 30, 32, 36, 40};

	private String[] fontSizeLabels;
	
	private static FontSettings f = new FontSettings();
	
	private FontSettings() {
		fontSizeLabels = new String[fontSizes.length];
		for (int index=0; index<fontSizes.length; index++)
			fontSizeLabels[index] = String.format("%.0f", fontSizes[index]);
	}
	
	public static FontSettings get() {
		return f;
	}
	
	public void setFontSize(JComponent c) {
		c.setFont(c.getFont().deriveFont(fontSizes[choice]));
	}
	
	public void setChoice(int choice) {
		FontSettings.choice = choice;
	}
	
	public int getChoice() {
		return FontSettings.choice;
	}
	
	public String[] getLabels() {
		return fontSizeLabels;
	}
}
