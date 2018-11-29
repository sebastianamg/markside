extensions = [
	"org/markside/ext/helloworld.js",
	"org/markside/ext/byeworld.js"
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

function runScript(name,args){
	return scripts[name].action(args);
};