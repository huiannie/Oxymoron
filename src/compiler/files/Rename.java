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
package compiler.files;

import java.util.ArrayList;

import compiler.binder.Context;
import compiler.blocks.Block;
import compiler.expression.Expression;
import compiler.expressionparser.ExpressionParser;
import compiler.literal.Literal;
import compiler.main.Settings;
import compiler.parser.Scope;
import compiler.tokenizers.LineMatcher;
import compiler.tokenizers.LineTokenizer;
import compiler.util.BugTrap;
import compiler.util.IOUtils;

public class Rename extends FileCommand {
	public static final boolean Debug = Settings.Debug;
	
	public static final String StartKeyword = "Rename";
	
	Literal sourceName;
	Literal destName;

	

	public Rename(int lineNumber) {
		super(lineNumber, StartKeyword);
	}

	public static boolean isARename(String line) {
		if (LineMatcher.matchStart(line, StartKeyword)) return true;
		else return false;
	}
	
	@Override
	public void print(int indent) {
		IOUtils.printIndented(indent, getClass().getSimpleName() 
								+ " range: " + startLineNumber + "-" + endLineNumber);
		// TODO
	}



	@Override
	public void parse(ArrayList<String> code, Scope scope, Block parent) throws BugTrap {
		if (this.parent==null) this.parent = parent;

		int lineNumber = getStartLineNumber();
		String line = code.get(lineNumber);
		
		// Syntax: Rename string string

		LineTokenizer tokenizer = new LineTokenizer(line);
		
		if (!tokenizer.isKeyword(StartKeyword)) throw new BugTrap(this, StartKeyword + ": Keyword missing");
		tokenizer.parseKeyword(StartKeyword);

		String remainder = tokenizer.getRemainder();
		ArrayList<Expression> filenames = new ExpressionParser(remainder, scope.getStringsMap(), parent).parseToList();
		
		if (filenames.size()!=2) throw new BugTrap(this, " syntax error.");
		Expression expr1 = filenames.get(0);
		Expression expr2 = filenames.get(1);

		// Get the source file name. It is expected to be a string.
		if (!expr1.isLiteral()) throw new BugTrap(this, "Bad variable " + expr1.toStringInBracket());
		sourceName = expr1.getLiteral();

		// Get the destination file name. It is expected to be a string.
		if (!expr2.isLiteral()) throw new BugTrap(this, "Bad variable " + expr2.toStringInBracket());
		destName = expr2.getLiteral();
	}
	
	@Override
	public int execute(Context context) throws BugTrap {

		compiler.data.String sourceFilename = (compiler.data.String) sourceName.evaluate(context);
		compiler.data.String destFilename = (compiler.data.String) destName.evaluate(context);
		
		if (Debug) {
			IOUtils.println(getClass().getSimpleName() + ": " + sourceFilename.getValue() + " to " + destFilename.getValue());
		}
		
		DataFile sourceFile = new DataFile(sourceFilename);
		DataFile destFile = new DataFile(destFilename);
		
		sourceFile.rename(destFile);
		
		return ExitCode_OK;
	}
}
