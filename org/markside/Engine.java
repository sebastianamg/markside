package org.markside;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Scanner;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class Engine {
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
	
	public void repl() throws ScriptException, FileNotFoundException {
		Scanner in = new Scanner(System.in);
		boolean isRunning = true;
		do {
			System.out.print("> ");
			String cmd = in.nextLine();
			try {
				switch (Operator.valueOf(cmd)) {
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
				System.out.println(eval("runScript(\""+cmd+"\",null)"));
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
