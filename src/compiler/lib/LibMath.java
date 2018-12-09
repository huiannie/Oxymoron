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

import compiler.lib.math.Cos;
import compiler.lib.math.IntegerToString;
import compiler.lib.math.Pow;
import compiler.lib.math.Random;
import compiler.lib.math.RealToString;
import compiler.lib.math.Round;
import compiler.lib.math.Sin;
import compiler.lib.math.Sqrt;
import compiler.lib.math.Tan;
import compiler.lib.math.ToInteger;
import compiler.lib.math.ToReal;
import compiler.util.BugTrap;
import compiler.util.IOUtils;

public class LibMath extends Library {
	
	public static final String names[] = {Cos.name, Pow.name, Random.name, 
										Round.name, Sin.name, Sqrt.name, 
										Tan.name, ToInteger.name, ToReal.name,
										IntegerToString.name, RealToString.name
										};
	
	public static LibMath library;
	
	static {
		library = new LibMath();
		try {
			library.create();
		} catch (BugTrap e) {
			IOUtils.println(LibMath.class.getName() + "Failed to create library.");
		}
	}

	public void create() throws BugTrap {
		create(new Cos());
		create(new Pow());
		create(new Random());
		create(new Round());
		create(new Sin());
		create(new Sqrt());
		create(new Tan());
		create(new ToInteger());
		create(new ToReal());
		create(new IntegerToString());
		create(new RealToString());
	}
}
