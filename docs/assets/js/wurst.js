Prism.languages.wurst= {
	'comment': [
		{
			pattern: /(^|[^\\])\/\*[\s\S]*?\*\//,
			lookbehind: true
		},
		{
			pattern: /(^|[^\\:])\/\/.*/,
			lookbehind: true
		}
	],
	'string': {
		pattern: /("|')(?:\\\\|\\?[^\\\r\n])*?\1/,
		greedy: true
	},
	'keyword' : /\b(?:class|return|if|else|while|for|in|break|new|package|endpackage|function|returns|public|private|protected|import|initlater|native|nativetype|extends|interface|implements|module|use|abstract|static|thistype|override|immutable|it|array|and|or|not|this|construct|ondestroy|destroy|type|globals|endglobals|constant|endfunction|nothing|takes|local|loop|endloop|exitwhen|set|call|then|elseif|endif|init|castTo|tuple|div|mod|let|var|from|to|downto|step|endpackage|switch|case|default|super|skip|instanceof|enum|begin|end)\b/,
	'boolean' : /\b(?:true|false)\b/,
	'builtin':  /\b(?:null|hashtable|nothing|integer|int|real|string|boolean|handle|event|player|widget|unit|destructable|item|ability|buff|force|group|trigger|triggercondition|triggeraction|timer|location|region|rect|boolexpr|sound|conditionfunc|filterfunc|unitpool|itempool|race|alliancetype|racepreference|gamestate|igamestate |fgamestate|playerstate|playerscore|playergameresult|unitstate|aidifficulty|eventid|gameevent|playerevent|playerunitevent|unitevent|limitop|widgetevent|dialogevent|unittype|gamespeed|gamedifficulty|gametype|mapflag|mapvisibility|mapsetting|mapdensity|mapcontrol|playerslotstate|volumegroup|camerafield|camerasetup|playercolor|placement|startlocprio|raritycontrol|blendmode|texmapflags|effect|effecttype|weathereffect|terraindeformation|fogstate|fogmodifier|dialog|button|quest|questitem|defeatcondition|timerdialog|leaderboard|multiboard|multiboarditem|trackable|gamecache|version|itemtype|texttag|attacktype|damagetype|weapontype|soundtype|lightning|pathingtype|image|ubersplat|code)\b/,
	'number' : /\b-?(?:0[bo])?(?:(?:\d|0x[\da-f])[\da-f]*\.?\d*|\.\d+)(?:e[+-]?\d+)?j?\b/i,
	'operator' : /[-+%=]=?|!=|\*\*?=?|\/\/?=?|<[<=>]?|>[=>]?|[&|^~]|\b(?:or|and|not)\b/,
	'punctuation' : /(\.|[..])/,
	'annotation' : /@\w+\s/
};
