script = {
    name:"wc",
    action:function(args,params){
        var regex = new RegExp('\\s*');
        var ans = 0;
        
        args.forEach( function(e1) {
            //ans += e1.split(regex).length;
            var tmp = e1.split(regex);
            tmp.forEach(function(e2) {
                if(e2.trim().length>0){
                    ans++;
                }
            });
            
        });
        return ans;
    },
    includes: []
};