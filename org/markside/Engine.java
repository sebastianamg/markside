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
	private HashMap<File, FileReader> operators;
	
	public Engine() {
		this.operators = new HashMap<>();
	}
	
	public void init() throws FileNotFoundException, ScriptException {
		 // create a script engine manager
		 ScriptEngineManager factory = new ScriptEngineManager();
		 // create a JavaScript engine
		 ScriptEngine engine = factory.getEngineByName("JavaScript");
		// evaluate JavaScript code from given file
		engine.eval(new java.io.FileReader(new File(INIT_CONFIG)));
		 
		for (int i = 0; i < Integer.parseInt(engine.eval("config.length").toString()); i++) {
			System.out.println(engine.eval("config["+i+"]"));
		}
	}
	
	private void addOperator(File operator) throws FileNotFoundException {
		this.operators.put(operator, new FileReader(operator));
	}
		
	
	public static void main(String[] args) throws FileNotFoundException, ScriptException {
		Engine engine = new Engine();
		engine.init();
		
	}

}
