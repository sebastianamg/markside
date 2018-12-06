package org.markside;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Scanner;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class Engine {
	private final int CMD = 0, ARGS = 1, PARAMS = 2;
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
		System.out.println("Executing ... "+cmd);
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
		return eval("runScript(\""+args[CMD]+"\",["+args[ARGS]+"],[])");
	}
	
	private String execScript(String input) {
		final int[][] graph = {
			//	 *	/	L	#	{	[	=	,	]	}
				{0,	1,	0,	0,	0,	0,	0,	0,	0,	0}, // 0 (Start)
				{10,10,	2,	10,	10,	10,	10,	10,	10,	10}, // 1
				{10,10,	2,	2,	3,	5,	10,	10,	10,	10}, // 2
				{3,	3,	3,	3,	3,	3,	3,	3,	3,	4}, // 3
				{0,	1,	0,	0,	0,	0,	0,	0,	0,	0}, // 4 (Final)
				{10,10,	6,	10,	10,	10,	10,	10,	9,	10}, // 5
				{10,10,	6,	6,	10,	10,	7,	10,	10,	10}, // 6
				{7,	7,	7,	7,	7,	7,	7,	8,	9,	7}, // 7
				{10,10,	6,	10,	10,	10,	10,	10,	10,	10}, // 8
				{10,10,	10,	10,	3,	10,	10,	10,	10,	10}, // 9
				{10,10,	10,	10,	10,	10,	10,	10,	10,	10}, // 10 (Sink / Error)
		};
		// TODO ...
		for (int i = 0; i < input.length(); i++) {
			// TODO ...
			// 
		}
		
		return null;
	}
	
	
	private String[] parseCmd(String input) {
		String[] ans = new String[3];
		ans[CMD] = ans[ARGS] = ans[PARAMS] = ""; 
		Scanner str = new Scanner(input);
		int i = 0;
		boolean isString = false;
		while(str.hasNext()) {
			String s = str.next();
			if(s.startsWith("\"") || s.endsWith("\"")) {
				isString = !isString;
			}
			ans[i] +=  s + ((i==1 && !isString && str.hasNext())?",":" ");
			i = 1;
		}
		ans[CMD] = ans[CMD].trim();
		ans[ARGS] = ans[ARGS].trim();
		str.close();
		return ans;
	}
	
	public void repl() throws FileNotFoundException {
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
	
	public static void main(String[] args) throws FileNotFoundException, ScriptException {
		Engine engine = new Engine();
		engine.init();
		engine.repl();
	}

}
