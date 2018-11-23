package org.markside;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Scanner;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class Engine {
	private static final String INIT_CONFIG = "config.js";
	private HashMap<String, FileReader> operators;
	
	public Engine() {
		this.operators = new HashMap<>();
	}
	
	private ScriptEngine getJSEngine() {
		// create a script engine manager
		ScriptEngineManager factory = new ScriptEngineManager();
		// create a JavaScript engine
		ScriptEngine engine = factory.getEngineByName("JavaScript");
		return engine;
	}
	
	private String getScriptName(File operator) throws FileNotFoundException, ScriptException {
		ScriptEngine engine = this.getJSEngine();
		engine.eval(new FileReader(operator));
		return engine.eval("script.name").toString();
	}
	
	public void init() throws FileNotFoundException, ScriptException {
		ScriptEngine engine = this.getJSEngine();
		engine.eval(new java.io.FileReader(new File(INIT_CONFIG)));
		for (int i = 0; i < Integer.parseInt(engine.eval("config.length").toString()); i++) {
			String script = engine.eval("config["+i+"]").toString();
			System.out.println(script);
			addOperator(new File(script));
		}
		System.out.println(this.operators);
	}
	
	private void addOperator(File operator) throws FileNotFoundException, ScriptException {
		this.operators.put(getScriptName(operator), new FileReader(operator));
	}
	
	public void repl() throws ScriptException {
		Scanner in = new Scanner(System.in);
		final String EXIT_CMD = "exit";
		boolean isRunning = true;
		do {
			System.out.print("> ");
			String cmd = in.nextLine();
			if(!cmd.equals(EXIT_CMD)) {
				switch (cmd) {
				case "list":
					for (String op : this.operators.keySet()) {
						System.out.println(op);
					}
					break;
				default:
					if(this.operators.containsKey(cmd)) {
						ScriptEngine engine = getJSEngine(); 
						engine.eval(this.operators.get(cmd));
						System.out.println(engine.eval("script.action()"));
					}
					break;
				}
			}else {
				isRunning = false;
			}
		}while(isRunning);
	}
	
	public static void main(String[] args) throws FileNotFoundException, ScriptException {
		Engine engine = new Engine();
		engine.init();
		engine.repl();
	}

}
