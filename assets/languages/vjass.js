/*
Language: vJass
*/

hljs.LANGUAGES.vjass = function(){
  var WURST_KEYWORDS = {
    'keyword': {
       'not' : 1, 'operator' : 1, 'readonly' : 1, 'keyword' : 1, 'and' : 1, 'or' : 1, 'null' : 1, 'debug' : 1, 'function' : 1, 'endfunction' : 1, 'local' : 1, 'set' : 1, 'call' : 1, 'takes' : 1, 'return' : 1, 'returns' : 1, 'globals' : 1, 'endglobals' : 1, 'struct' : 1, 'endstruct' : 1, 'interface' : 1, 'endinterface' : 1, 'library' : 1, 'endlibrary' : 1, 'private' : 1, 'method' : 1, 'endmethod' : 1, 'static' : 1, 'constant' : 1, 'initializer' : 1, 'if' : 1, 'elseif' : 1, 'else' : 1, 'endif' : 1, 'then' : 1, 'extends' : 1, 'type' : 1, 'public' : 1, 'scope' : 1, 'endscope' : 1, 'loop' : 1, 'exitwhen' : 1, 'endloop' : 1, 'array' : 1, 'requires' : 1, 'native' : 1, 'this' : 1, 'thistype' : 1, 'module' : 1, 'optional' : 1, 'implement' : 1, 'super' : 1, 'delegate' : 1, 'while' : 1, 'for' : 1, 'break' :1
    },
    'constant': {
       'true': 1, 'false': 1, 'null': 1
    },
    'typename': {
       'hashtable' : 1, 'nothing' : 1, 'integer' : 1, 'real' : 1, 'string' : 1, 'boolean' : 1, 'handle' : 1, 'event' : 1, 'player' : 1, 'widget' : 1, 'unit' : 1, 'destructable' : 1, 'item' : 1, 'ability' : 1, 'buff' : 1, 'force' : 1, 'group' : 1, 'trigger' : 1, 'triggercondition' : 1, 'triggeraction' : 1, 'timer' : 1, 'location' : 1, 'region' : 1, 'rect' : 1, 'boolexpr' : 1, 'sound' : 1, 'conditionfunc' : 1, 'filterfunc' : 1, 'unitpool' : 1, 'itempool' : 1, 'race' : 1, 'alliancetype' : 1, 'racepreference' : 1, 'gamestate' : 1, 'igamestate ' : 1, 'fgamestate' : 1, 'playerstate' : 1, 'playerscore' : 1, 'playergameresult' : 1, 'unitstate' : 1, 'aidifficulty' : 1, 'eventid' : 1, 'gameevent' : 1, 'playerevent' : 1, 'playerunitevent' : 1, 'unitevent' : 1, 'limitop' : 1, 'widgetevent' : 1, 'dialogevent' : 1, 'unittype' : 1, 'gamespeed' : 1, 'gamedifficulty' : 1, 'gametype' : 1, 'mapflag' : 1, 'mapvisibility' : 1, 'mapsetting' : 1, 'mapdensity' : 1, 'mapcontrol' : 1, 'playerslotstate' : 1, 'volumegroup' : 1, 'camerafield' : 1, 'camerasetup' : 1, 'playercolor' : 1, 'placement' : 1, 'startlocprio' : 1, 'raritycontrol' : 1, 'blendmode' : 1, 'texmapflags' : 1, 'effect' : 1, 'effecttype' : 1, 'weathereffect' : 1, 'terraindeformation' : 1, 'fogstate' : 1, 'fogmodifier' : 1, 'dialog' : 1, 'button' : 1, 'quest' : 1, 'questitem' : 1, 'defeatcondition' : 1, 'timerdialog' : 1, 'leaderboard' : 1, 'multiboard' : 1, 'multiboarditem' : 1, 'trackable' : 1, 'gamecache' : 1, 'version' : 1, 'itemtype' : 1, 'texttag' : 1, 'attacktype' : 1, 'damagetype' : 1, 'weapontype' : 1, 'soundtype' : 1, 'lightning' : 1, 'pathingtype' : 1, 'image' : 1, 'ubersplat' : 1
   }
  };
  return {
    defaultMode: {
      keywords: WURST_KEYWORDS,
      illegal: '</',
      contains: [
        hljs.C_LINE_COMMENT_MODE,
//        hljs.C_BLOCK_COMMENT_MODE,
        hljs.QUOTE_STRING_MODE,
        {
          className: 'string',
          begin: '\'', end: '[^\\\\]\'',
          relevance: 0
        },
        {
          className: 'string',
          begin: '`', end: '[^\\\\]`'
        },
        {
          className: 'number',
          begin: '[^a-zA-Z_0-9](\\-|\\+)?\\d+(\\.\\d+|\\/\\d+)?((d|e|f|l|s)(\\+|\\-)?\\d+)?',
          relevance: 0
        },
        hljs.C_NUMBER_MODE
      ]
    }
  };
}();

