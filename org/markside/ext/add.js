script = {
    name:"add",
    action:function(args){
    	var ans = 0;
    	for(i in args){
    		ans += args[i];
    	}
        return ans;
    },
    includes: []
};