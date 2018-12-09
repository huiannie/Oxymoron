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
package compiler.commands;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;

import compiler.binder.Context;
import compiler.blocks.Block;
import compiler.data.DataType;
import compiler.expression.Expression;
import compiler.expressionparser.ExpressionParser;
import compiler.literal.ArrayCall;
import compiler.literal.Literal;
import compiler.literal.SymbolCall;
import compiler.main.Settings;
import compiler.parser.FileReader;
import compiler.parser.Scope;
import compiler.tokenizers.LineMatcher;
import compiler.tokenizers.LineTokenizer;
import compiler.util.BugTrap;
import compiler.util.IOUtils;
import gui.GuiInputPipe;

public class Input extends Command {
	public static final boolean Debug = Settings.Debug;
	
	public static final String StartKeyword = "Input";
	
	private static InputStream instream = Settings.instream;
	private static GuiInputPipe pipe = null;   // For Gui textfield input.
	private static FileReader fileReader = null;   // For FileInputStream (emulate System.in)
	
	ArrayList<Literal> variableNames = new ArrayList<Literal>();

	// For configuring the input stream.
	// InputStream could be:
	// 1. A pipe from Gui. In this case, each call obtains a new line. MUST create a new scanner each time.
	// 2. System.in from commandline. In this case, each call obtains a new line. May create a new scanner each time.
	// 3. A file. In this case, the scanner must remain the same.
	public static void SetInputPipe(GuiInputPipe pipe) {
		Input.pipe = pipe;
	}
	public static GuiInputPipe getInputPipe() {
		return Input.pipe;
	}
	
	public static void SetInputStream(InputStream instream) {
		Input.instream = instream;
		if (fileReader!=null) {
			fileReader.close();
			fileReader=null;
		}
		if (Input.instream!=null && Input.instream!=Settings.instream) {
			fileReader = new FileReader(Input.instream);
		}
	}
	
	private Scanner getScanner() throws BugTrap {
		Scanner scanner;
		if (pipe!=null) {
			String inputString = pipe.getLine();
			if (!inputString.endsWith("\n")) inputString = inputString + "\n";
			scanner = new Scanner(inputString);
			return scanner;
		}
		else if (instream==System.in) {
			scanner = new Scanner(instream);
			return scanner;
		}
		else if (instream!=null) {
			if (fileReader==null) throw new BugTrap("Input not available.");
			String nextline = fileReader.nextLine();
			if (Debug) IOUtils.println("Read from file line: " + nextline);

			// Somehow, Scanner cannot be created on a zero-length line.
			// In order for automated testing to be able to handle empty lines,
			// replace a zero-length line by a space.
			if (nextline.length()==0) nextline = " ";

			scanner = new Scanner(nextline);
			return scanner;
		}
		else {
			throw new BugTrap("Input not available.");
		}
	}
		
	
	public Input(int lineNumber) {
		super(lineNumber, StartKeyword);
	}

	public static boolean isAnInput(String line) {
		if (LineMatcher.matchStart(line, StartKeyword)) return true;
		else return false;
	}
	
	
	@Override
	public void print(int indent) {
		IOUtils.printIndented(indent, getClass().getSimpleName() 
								+ " range: " + startLineNumber + "-" + endLineNumber);
		for (Literal v : variableNames) {
			IOUtils.printIndented(indent+1, "variable = " + v.valueToString());
		}
	}

	
	public void parse(ArrayList<String> code, Scope scope, Block parent) throws BugTrap {
		if (this.parent==null) this.parent = parent;

		int lineNumber = getStartLineNumber();
		String line = code.get(lineNumber);
		
		
		// Syntax 1: Input
		// Syntax 2: Input A, B, C[0][2]
		
		LineTokenizer tokenizer = new LineTokenizer(line);
		
		if (!tokenizer.isKeyword(StartKeyword)) throw new BugTrap(this, StartKeyword + ": Keyword missing");
		tokenizer.parseKeyword(StartKeyword);

		String remainder = tokenizer.getRemainder();

		if (remainder.length()==0) {
			// Without argument. 
		}
		else {
			// With argument.
			String args[] = remainder.split(",");
			for (String arg : args) {
				LineTokenizer tokenizer2 = new LineTokenizer(arg);
				
				if (tokenizer2.isArrayName()) {
					String arrayname = tokenizer2.parseArrayName();
					Expression expr = new ExpressionParser(arrayname, scope.getStringsMap(), parent).parse();
					if (!expr.isLiteral()) throw new BugTrap(this, "Bad variable " + arrayname);
					variableNames.add(expr.getLiteral());
				}
				else if (tokenizer2.isName()) {
					String name = tokenizer2.parseName();
					Expression expr = new ExpressionParser(name, scope.getStringsMap(), parent).parse();
					if (!expr.isLiteral()) throw new BugTrap(this, "Bad variable " + name);
					variableNames.add(expr.getLiteral());
				}
				else {
					throw new BugTrap(this, ": unrecognized name");
				}
			}
		}
	}
	
	
	
	DataType read(Scanner scanner, String type) throws BugTrap {
		try {
			if (compiler.data.Integer.matchesType(type)) {
				int value = scanner.nextInt();
				if (Debug) IOUtils.println(this.getName() + " Read integer " + value);
				return new compiler.data.Integer(value);
			}
			else if (compiler.data.Real.matchesType(type)) {
				float value = scanner.nextFloat();
				if (Debug) IOUtils.println(this.getName() + " Read float " + value);
				return new compiler.data.Real(value);
			}
			else if (compiler.data.Character.matchesType(type)) {
				char value = scanner.next().charAt(0);
				if (Debug) IOUtils.println(this.getName() + " Read char " + value);
				return new compiler.data.Character(value);
			}
			else if (compiler.data.String.matchesType(type)) {
				String value = scanner.nextLine();
				if (Debug) IOUtils.println(this.getName() + " Read string " + value);
				return new compiler.data.String(value);
			}
			else if (compiler.data.Boolean.matchesType(type)) {
				String value = scanner.nextLine();
				if (Debug) IOUtils.println(this.getName() + " Read boolean " + value);
				return compiler.data.Boolean.parseValue(value);
			}
			else
				throw new BugTrap(this, "Bad input for type " + type);
		}
		catch (Exception e) {
			if (e instanceof BugTrap) throw e;
			else throw new BugTrap(e);
		}

	}



	
	@Override
	public int execute(Context context) throws BugTrap {
		Scanner scanner = getScanner();
		
		// If no variable is provided, then just read until the newline character. Throw out the content read.
		if (variableNames.size()==0) {
			if (Debug) IOUtils.println("Read some dummy line");
			scanner.nextLine();
			if (Debug) IOUtils.println("Read some dummy line and skipped");
		}
		else {
			for (Literal literal : variableNames) {
				if (literal instanceof SymbolCall) {
					SymbolCall variableCall = (SymbolCall) literal;
					String type = variableCall.getDataType(context);
					if (Debug) {
						IOUtils.println(getClass().getSimpleName() + ": going to save input to variable " + variableCall.getName());
					}
					DataType newValue = read(scanner, type);
					variableCall.setValue(context, newValue);
				}
				else if (literal instanceof ArrayCall) {
					ArrayCall arrayCall = (ArrayCall) literal;
					String type = arrayCall.getDataType(context);
					DataType newValue = read(scanner, type);
					arrayCall.setValue(context, newValue);
				}
			}
		}
		return ExitCode_OK;
	}

}
