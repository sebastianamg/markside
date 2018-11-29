package org.markside;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Scanner;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class Engine {
	private final int CMD = 0, ARGS = 1;
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
	
	public void init() throws FileNotFoundException, ScriptException {
		loadScript(INIT_CONFIG);
		for (int i = 0; i < evalInt("extensions.length"); i++) {
			String script = eval("extensions["+i+"]");
			loadScript(script);
		}
	}
	
	private void loadScript(String script) throws FileNotFoundException, ScriptException {
		this.jsEng.eval(new FileReader(script));
		System.out.println(this.jsEng.eval("loadScript(script)"));
	}
	
	private String[] parseCmd(String input) {
		String[] ans = new String[2];
		ans[CMD] = ans[ARGS] = ""; 
		Scanner str = new Scanner(input);
		int i = 0;
		while(str.hasNext()) {
			ans[i] +=  str.next() + ((i==1 && str.hasNext())?",":" ");
			i = 1;
		}
		ans[CMD] = ans[CMD].trim();
		ans[ARGS] = ans[ARGS].trim();
		str.close();
		return ans;
	}
	
	public void repl() throws ScriptException, FileNotFoundException {
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
				System.out.println(eval("runScript(\""+parsedCmd[CMD]+"\",["+parsedCmd[ARGS]+"])"));
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
