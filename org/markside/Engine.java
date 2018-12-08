package org.markside;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.text.ParseException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
								INIT_INDEX = 0,
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
//		System.out.println(this.jsEng.eval("loadScript(script)"));
		this.jsEng.eval("loadScript(script)");
	}
	
	private String runFunction(String[]args) throws ScriptException {
		return eval("runScript(\""+args[CMD]+"\","+args[ARGS]+","+args[PARS]+")");
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
				cmdName=cmdName.trim();
				parameters=parameters.trim();
				arguments=arguments.trim();
//				System.out.println("cmdName: |"+cmdName+"|");
//				System.out.println("parameters: |"+parameters+"|");
//				System.out.println("arguments: |"+arguments+"|");
				ans.setState(state);
				ans.setIndex(i);
				ans.setAnswer(ans.getAnswer()+runFunction(parseCmd(cmdName,this.getJSArray(parameters),this.getJSArray(arguments))));
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
	
	private String getJSArray(String input) {
		final String PATTERN = "[\\w]+=\"[\\w ]+\"|([\\w]+=[\\w]+|[\\w]+=[\\w ]+)|\"[\\w ]+\"|[\\w]";
		String ans = "[";
		Pattern pattern = Pattern.compile(PATTERN);
		Matcher str = pattern.matcher(input);
		while(str.find()) {
			String x = str.group().trim();
			ans += x + ((str.hitEnd())?"":",");
//			System.out.println("Matcher returns: |"+x+"|; hits the end?: "+str.hitEnd());
		}
		ans =  ( (ans.endsWith(","))? ans.substring(0, ans.length()-1) : ans ) +  "]";
//		System.out.println("getJSArray(input: |"+input+"|) = |"+ans+"|");
		return ans;
	}
	
	private String[] parseCmd(String cmd,String pars, String args) {
		String[] ans = new String[3];
		ans[CMD] = cmd;
		ans[PARS] = pars;
		ans[ARGS] = args;
		return ans;
	}
	
	public void repl() {
		Scanner in = new Scanner(System.in);
		boolean isRunning = true;
		do {
			System.out.print("> ");
			String cmd = in.nextLine();
//			System.out.println("CMD: "+cmd);
			try {
				switch (Operator.startWith(cmd)) {
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
					String[]args = cmd.split("[ ]+");
//					System.out.println("CMD args: "+args.length);
					if(args.length == 2) {
						Scanner fd = new Scanner(new File(args[PARS]));
//						System.out.println("CMD args ARGS: "+args[PARS]);
						StringBuffer file = new StringBuffer();
						while(fd.hasNextLine()) {
							file.append(fd.nextLine());
							if(fd.hasNextLine()) {
								file.append("\n");
							}
						}
						fd.close();
						System.out.println(execScript(file.toString(), INIT_STATE, 0).getAnswer());
					}else {
						throw new ParseException(cmd,INIT_INDEX);
					}
					break;
				default:
					break;
				}
			}catch(NullPointerException | IllegalArgumentException e) {
				try {
					System.out.println(execScript(cmd, INIT_STATE, INIT_INDEX).getAnswer());
				} catch (ScriptException | ParseException e1) {
					System.err.println("Syntax error in script: "+cmd);
				}
			} catch (ScriptException | FileNotFoundException | ParseException e) {
				System.err.println("Syntax error in script: "+cmd);
			}
		}while(isRunning);
		in.close();
	}
	
	public static void main(String[] args) throws FileNotFoundException, ScriptException, ParseException {
		Engine engine = new Engine();
		engine.init();
		engine.repl();
		
		
		
//		System.out.println(engine.getJSArray("a b=c w=xyz pqr=\"stu\" 1 pqr=\" st u \" 2 3 \"a b c\"    d \" f g \" 1 2 3"));
//		System.out.println(engine.getJSArray("1 1"));
		
		
		
//		String script = "a b c \\add[a=1,b=2]{ 1 \\mul[a=1,b=2]{ 2 \\mul[a=1,b=2]{ 2  2   } } 3 \\mul[a=1,b=2]{ 2  2   }} hello \\mul[a=1,b=2]{ 2  2   } world!";
//		String script = "a b c \\add[a=1,b=2]{ 1 \\mul[a=1,b=2]{ 2 \\mul[a=1,b=2]{ 2  2   } } 3 \\mul[a=1,b=2]{ 2  2   }} \\wc[a=1,b=2]{ \"a b c\" } hello \\mul[a=1,b=2]{ 2  2   } world!";
//		String script = "\\add[a=0]{ 1 1 }";
//		String script = "\\wc[]{ \"abc bcd cde f g 1 2 34 \" }";
		
//		engine.init();
//		System.out.println(engine.execScript(script, INIT_STATE, 0).getAnswer());
//		System.out.println("Script: "+script);
	}

}
