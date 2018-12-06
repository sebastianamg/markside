script = {
    name:"mul",
    action:function(args,params){
    	var ans = 1;
    	for(i in args) {
    		ans *= args[i];
    	}
        return ans;
    },
    includes: ["org/markside/ext/add.js"]
};