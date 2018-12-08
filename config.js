extensions = [
	"org/markside/ext/add.js",
	"org/markside/ext/mul.js",
	"org/markside/ext/wordcounter.js"
];

script = {
    name:"config",
	action:function(args){
        return true;
    },
    includes: []    
};

scripts = {};

function loadScript(script){
	scripts[script.name] = script;
	return Object.keys(scripts).join("\n-----------\n");
};

function getListOfScripts(){
	return Object.keys(scripts).join("\n");
};

function runScript(name,args,pars){
	return scripts[name].action(args,pars);
};