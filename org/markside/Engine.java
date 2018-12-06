package org.markside;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.text.ParseException;
import java.util.Scanner;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class Engine {
	
	private class Result{
		private int index, state;
		private String answer;
		public Result(int index, int state, String ans) {
			super();
			this.index = index;
			this.state = state;
			this.answer = ans;
		}
		public int getIndex() {
			return index;
		}
		public int getState() {
			return state;
		}
		public String getAnswer() {
			return answer;
		}
		public void setIndex(int index) {
			this.index = index;
		}
		public void setState(int state) {
			this.state = state;
		}
		public void setAnswer(String answer) {
			this.answer = answer;
		}
		@Override
		public String toString() {
			return "Result [index=" + index + ", state=" + state + ", answer=" + answer + "]";
		}
		
	}
	
	private static final int 	CMD = 0,
								PARS = 1,
								ARGS = 2,
								INIT_STATE = 0,
								RECURSIVE_STATE = 1,
								START_ARGUMENTS_STATE = 3,
								FINAL_STATE = 4,
								START_PARAMETERS_STATE = 5,
								END_PARAMETERS_STATE = 9,
								ERROR_STATE = 10;
	private static final int[][] GRAPH = {
			//	 0	1	2	3	4	5	6	7	8	9
			//	 *	\	L	#	{	[	=	,	]	}
				{0,	1,	0,	0,	0,	0,	0,	0,	0,	0}, // 0 (Start)
				{10,10,	2,	10,	10,	10,	10,	10,	10,	10}, // 1 (Recursive call)
				{10,10,	2,	2,	3,	5,	10,	10,	10,	10}, // 2
				{3,	1,	3,	3,	3,	3,	3,	3,	3,	4}, // 3
				{0,	1,	0,	0,	0,	0,	0,	0,	0,	0}, // 4 (Final)
				{10,10,	6,	10,	10,	10,	10,	10,	9,	10}, // 5
				{10,10,	6,	6,	10,	10,	7,	10,	10,	10}, // 6
				{7,	7,	7,	7,	7,	7,	7,	8,	9,	7}, // 7
				{10,10,	6,	10,	10,	10,	10,	10,	10,	10}, // 8
				{10,10,	10,	10,	3,	10,	10,	10,	10,	10}, // 9
				{10,10,	10,	10,	10,	10,	10,	10,	10,	10}, // 10 (Sink / Error)
		};
	private static final String INIT_CONFIG = "config.js";
	private ScriptEngine jsEng;
	
	public Engine() {
		this.jsEng = getJSEngine();
	}
	
	private ScriptEngine getJSEngine() {
		// create a script engine manager
		ScriptEngineManager factory = new ScriptEngineManager();
		// create a JavaScript engine
		return factory.getEngineByName("JavaScript");
	}
	
	private String eval(String cmd) throws ScriptException {
//		System.out.println("Executing ... "+cmd);
		return this.jsEng.eval(cmd).toString();
	}
	private int evalInt(String cmd) throws ScriptException {
		return Integer.parseInt(this.jsEng.eval(cmd).toString());
	}
	
	public void init() throws ScriptException, FileNotFoundException {
		loadScript(INIT_CONFIG);
		for (int i = 0; i < evalInt("extensions.length"); i++) {
			String script = eval("extensions["+i+"]");
			try {
				loadScript(script);
			} catch (FileNotFoundException | ScriptException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void loadScript(String script) throws FileNotFoundException, ScriptException {
		this.jsEng.eval(new FileReader(script));
		System.out.println(this.jsEng.eval("loadScript(script)"));
	}
	
	private String runFunction(String[]args) throws ScriptException {
		return eval("runScript(\""+args[CMD]+"\",["+args[ARGS]+"],["+args[PARS]+"])");
	}
	
	private boolean isLetter(char c) {
		return Character.isLetter(c);
	}
	
	private boolean isDigit(char c) {
		return Character.isDigit(c);
	}
	
	private boolean isBackSlash(char c) {
		return c == '\\';
	}
	
	private boolean isOpenCurlyBacket(char c) {
		return c == '{';
	}
	
	private boolean isCloseCurlyBacket(char c) {
		return c == '}';
	}
	
	private boolean isOpenSquareBacket(char c) {
		return c == '[';
	}
	
	private boolean isCloseSquareBacket(char c) {
		return c == ']';
	}
	
	private boolean isEquals(char c) {
		return c == '=';
	}
	
	private boolean isComma(char c) {
		return c == ',';
	}
	
	private Result execScript(String input,int state,int index) throws ParseException, ScriptException {
		/*
		 * This finite state automaton recognizes commands of this type:
		 * \command[par1=value1,par2=value2,...]{arguments...} 
		 * or
		 * \command{arguments...} 
		 * */
//		System.out.println("execScript(input ("+input.length()+"): "+input+",state: "+state+",index: "+index+")");
		Result ans = new Result(index, state, "");
		int previousState;
		String 	cmdName = "", 
				parameters = "",
				arguments = "";
		boolean recordCmdName = state == RECURSIVE_STATE,
				recordParameters = false,
				recordArguments = false;
		for (int i = index; i < input.length(); i++) {
			char x = input.charAt(i);
			previousState = state;
			state = GRAPH[state][getTypeOfSymbol(x)];
			String c = Character.toString(x);
//			System.out.println("Char: "+c+"; State: "+state+"; index: "+i);
			switch (state) {
			case ERROR_STATE:
				throw new ParseException(input,i);
			case START_ARGUMENTS_STATE:
				recordArguments = true;
				recordCmdName = false;
				break;
			case FINAL_STATE:
				recordArguments = false;
//				System.out.println("cmdName: "+cmdName);
//				System.out.println("parameters: "+parameters);
//				System.out.println("arguments: "+arguments);
				ans.setState(state);
				ans.setIndex(i);
				ans.setAnswer(ans.getAnswer()+runFunction(parseCmd(cmdName+" "+parameters+" "+arguments)));
				return ans;
			case RECURSIVE_STATE:
				Result tmp = execScript(input,state,i+1);
//				System.out.println("Result: "+tmp);
				c = tmp.getAnswer();
				i = tmp.getIndex();
				state = previousState;
				break;
			case START_PARAMETERS_STATE:
				recordParameters = true;
				recordCmdName = false;
				break;
			case END_PARAMETERS_STATE:
				recordParameters = false;
				break;
			}
			if(recordCmdName) {
				cmdName += c;
			}else if(recordParameters && !isOpenSquareBacket(c.charAt(0))) {
				parameters += c;
			}else if(recordArguments && !isOpenCurlyBacket(c.charAt(0))) {
				arguments += c;
			}else if(!isBackSlash(c.charAt(0)) && 
					!isOpenSquareBacket(c.charAt(0)) && !isCloseSquareBacket(c.charAt(0))
					&& !isOpenCurlyBacket(c.charAt(0)) && !isCloseCurlyBacket(c.charAt(0))) {
				ans.setAnswer(ans.getAnswer()+c);
			}
		}
		return ans;
	}
	
	private int getTypeOfSymbol(char c) {
		if(isBackSlash(c)) {
			return 1;
		}else if(isLetter(c)) {
			return 2;
		}else if(isDigit(c)) {
			return 3;
		}else if(isOpenCurlyBacket(c)) {
			return 4;
		}else if(isCloseCurlyBacket(c)) {
			return 9;
		}else if(isOpenSquareBacket(c)) {
			return 5;
		}else if(isCloseSquareBacket(c)) {
			return 8;
		}else if(isEquals(c)) {
			return 6;
		}else if(isComma(c)) {
			return 7;
		}else {
			return 0;
		}
	}
	
	
	private String[] parseCmd(String input) {
//		System.out.println("parseCmd("+input+")");
		String[] ans = new String[3];
		ans[CMD] = ans[PARS] = ans[ARGS] = ""; 
		Scanner str = new Scanner(input);
		int i = 0;
		boolean isString = false;
		while(str.hasNext()) {
			String s = str.next();
			if(s.startsWith("\"") || s.endsWith("\"")) {
				isString = !isString;
			}
			ans[i] +=  s + ((i==2 && !isString && str.hasNext())?",":((i==1)?"":" "));
			i = (i<2)?i+1:2;
		}
		ans[CMD] = ans[CMD].trim();
		ans[ARGS] = ans[ARGS].trim();
		str.close();
		return ans;
	}
	
	public void repl() throws FileNotFoundException, ParseException {
		Scanner in = new Scanner(System.in);
		boolean isRunning = true;
		do {
			System.out.print("> ");
			String cmd = in.nextLine();
			String[] parsedCmd = parseCmd(cmd);
			try {
				switch (Operator.valueOf(parsedCmd[CMD])) {
				case list:
					System.out.println("------- Basic:");
					for (Operator op : Operator.values()) {
						System.out.println(op);
					}
					System.out.println("------- Loaded:");
					System.out.println(eval("getListOfScripts()"));
					break;
				case reset:
					this.init();
					break;
				case exit:
					isRunning = false;
					break;
				case exec:
					Scanner fd = new Scanner(new File(parsedCmd[PARS]));
					StringBuffer file = new StringBuffer();
					while(fd.hasNextLine()) {
						file.append(fd.nextLine());
						if(fd.hasNextLine()) {
							file.append("\n");
						}
					}
					fd.close();
					System.out.println(execScript(file.toString(), INIT_STATE, 0).getAnswer());
					break;
				default:
					break;
				}
			}catch(IllegalArgumentException e) {
				try {
					System.out.println(runFunction(parsedCmd));
				} catch (ScriptException e1) {
					System.err.println("Syntax error in script: "+cmd);
				}
			} catch (ScriptException e) {
				System.err.println("Syntax error in script: "+cmd);
			}
		}while(isRunning);
		in.close();
	}
	
	public static void main(String[] args) throws FileNotFoundException, ScriptException, ParseException {
		Engine engine = new Engine();
//		String script = "a b c \\add[a=1,b=2]{ 1 \\mul[a=1,b=2]{ 2 \\mul[a=1,b=2]{ 2  2   } } 3 \\mul[a=1,b=2]{ 2  2   }} \\wc[a=1,b=2]{ \"a b c\" } hello \\mul[a=1,b=2]{ 2  2   } world!";
		engine.init();
		engine.repl();
//		System.out.println(engine.execScript(script, INIT_STATE, 0).getAnswer());
//		System.out.println("Script: "+script);
	}

}
