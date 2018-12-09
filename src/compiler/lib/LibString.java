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
package compiler.lib;

import compiler.lib.string.Append;
import compiler.lib.string.Contains;
import compiler.lib.string.CurrencyFormat;
import compiler.lib.string.Delete;
import compiler.lib.string.Insert;
import compiler.lib.string.IsInteger;
import compiler.lib.string.IsReal;
import compiler.lib.string.Length;
import compiler.lib.string.StringToInteger;
import compiler.lib.string.StringToReal;
import compiler.lib.string.Substring;
import compiler.lib.string.ToLower;
import compiler.lib.string.ToUpper;
import compiler.main.Settings;
import compiler.util.BugTrap;
import compiler.util.IOUtils;

public class LibString extends Library {
	public static final boolean Debug = Settings.Debug;
	
	public static final String names[] = {Length.name, Append.name, Contains.name, CurrencyFormat.name,
										IsInteger.name, IsReal.name,
										StringToInteger.name, StringToReal.name, Substring.name,
										ToLower.name, ToUpper.name,
										Delete.name, Insert.name};
	
	public static LibString library;
	
	static {
		library = new LibString();
		try {
			library.create();
		} catch (BugTrap e) {
			IOUtils.println(LibString.class.getName() + "Failed to create library.");
		}
	}

	public void create() throws BugTrap {
		create(new Append());
		create(new CurrencyFormat());
		create(new Contains());
		create(new IsInteger());
		create(new IsReal());
		create(new Length());
		create(new StringToInteger());
		create(new StringToReal());
		create(new Substring());
		create(new ToLower());
		create(new ToUpper());
		
		// from chapter 12
		create(new Delete());
		create(new Insert());
	}
}
