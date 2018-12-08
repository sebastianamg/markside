package org.markside;

public enum Operator {
	list,
	reset,
	exec,
	exit;
	public String getName() {
        switch(this) {
            case list:   return "list";
            case reset: return "reset";
            case exec:  return "exec";
            case exit:  return "exit";
            default: return null;
        }
    }
	public static Operator startWith(String op){
		for (Operator x : Operator.values()) {
			if(op.startsWith(x.getName())) {
				return x;
			}
		}
        return null;
    }
}
