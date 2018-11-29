package org.markside;

public enum Operator {
	list,
	reset,
	exit;
	public String getName() {
        switch(this) {
            case list:   return "list";
            case reset: return "reset";
            case exit:  return "exit";
            default: return null;
        }
    }
}
