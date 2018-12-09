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

import compiler.util.BugTrap;

public abstract class Interruptable {
	private Interrupt flag = null;

	public boolean isInterrupted() {
		return flag!=null;
	}
	public void setInterrupt(Interrupt flag) {
		this.flag = flag;
	}

	public void handleInterrupt() throws InterruptTrap {
		// An interrupt will be acknowledged 
		if (isInterrupted()) {
			flag.setAck();
			throw new InterruptTrap(flag);
		}
	}
	
	public static class InterruptTrap extends BugTrap {
		private static final long serialVersionUID = 1143248191669996500L;
		public InterruptTrap(Interrupt flag) {
			super(flag.getMessage());
		}
	}
}
