package de.peeeq.eclipsewurstplugin;

import org.eclipse.swt.graphics.RGB;

public class WurstConstants {


	public static final String[] KEYWORDS = new String[] { "class", "return", "if", "else", "while", "for", "in", "break", "new", "null",
		"package", "endpackage", "function", "returns", "public", "private", "protected", "import", "native", "nativetype", "extends",
		"interface", "implements", "module", "use", "abstract", "static", "thistype", "override", "immutable", "it", "array", "and",
		"or", "not", "this", "construct", "ondestroy", "destroy", "type", "globals", "endglobals", "constant", "endfunction",
		"nothing", "takes", "local", "loop", "endloop", "exitwhen", "set", "call", "then", "elseif", "endif", "init", "castTo",
		"tuple", "div", "mod", "let", "from", "to", "downto", "step", "endpackage", "skip", "true", "false" };
	
	public static final String[] JASSTYPES = new String[] { 
		"int", "integer", "real", "code", "boolean", "string",
		"agent",  "event",  "player",  "widget",  "unit",  "destructable", 
		 "item",  "ability",  "buff",  "force",  "group", 
		 "trigger",  "triggercondition",  "triggeraction",  "timer",  "location", 
		 "region",  "rect",  "boolexpr",  "sound",  "conditionfunc", 
		 "filterfunc",  "unitpool",  "itempool",  "race",  "alliancetype", 
		 "racepreference",  "gamestate",  "igamestate",  "fgamestate",  "playerstate", 
		 "playerscore",  "playergameresult",  "unitstate",  "aidifficulty",  "eventid", 
		 "gameevent",  "playerevent",  "playerunitevent",  "unitevent",  "limitop", 
		 "widgetevent",  "dialogevent",  "unittype",  "gamespeed",  "gamedifficulty", 
		 "gametype",  "mapflag",  "mapvisibility",  "mapsetting",  "mapdensity", 
		 "mapcontrol",  "playerslotstate",  "volumegroup",  "camerafield",  "camerasetup", 
		 "playercolor",  "placement",  "startlocprio",  "raritycontrol",  "blendmode", 
		 "texmapflags",  "effect",  "effecttype",  "weathereffect",  "terraindeformation", 
		 "fogstate",  "fogmodifier",  "dialog",  "button",  "quest", 
		 "questitem",  "defeatcondition",  "timerdialog",  "leaderboard",  "multiboard", 
		 "multiboarditem",  "trackable",  "gamecache",  "version",  "itemtype", 
		 "texttag",  "attacktype",  "damagetype",  "weapontype",  "soundtype", 
		 "lightning",  "pathingtype",  "image",  "ubersplat",  "hashtable" };
	
	public static final String PLUGIN_PACKAGE ="de.peeeq.eclipsewurstplugin";
	
	//Parenthesis Highlighting
	public final static String EDITOR_MATCHING_BRACKETS       = "matchingBrackets";
	public final static String EDITOR_MATCHING_BRACKETS_COLOR = "matchingBracketsColor";
	public static final String DEFAULT_MATCHING_BRACKETS_COLOR = "128,128,128";
	
	
	//PreferenceStore Strings
	public static final String SYNTAXCOLOR_COLOR         = "Color";
	public static final String SYNTAXCOLOR_BOLD          = "Bold";
	public static final String SYNTAXCOLOR_ITALIC        = "Italic";
	public static final String SYNTAXCOLOR_UNDERLINE     = "Underline";
	public static final String SYNTAXCOLOR_STRIKETHROUGH = "Strikethrough";

	public static final String SYNTAXCOLOR_KEYWORD     = "Keyword";
	public static final String SYNTAXCOLOR_JASSTYPE    = "Jasstype";
	public static final String SYNTAXCOLOR_STRING      = "String";
	public static final String SYNTAXCOLOR_COMMENT     = "Comment";
	public static final String SYNTAXCOLOR_FUNCTION    = "Function";
	public static final String SYNTAXCOLOR_DATATYPE    = "Datatype";
	public static final String SYNTAXCOLOR_VAR         = "Var";
	public static final String SYNTAXCOLOR_PARAM       = "Param";
	public static final String SYNTAXCOLOR_FIELD       = "Field";
	public static final String SYNTAXCOLOR_INTERFACE   = "Interface";
	public static final String SYNTAXCOLOR_CONSTRUCTOR = "Constructor";

	//Colors
	private static final RGB COLOR_BLACK = new RGB(0,0,0);
	
	public static final RGB SYNTAXCOLOR_RGB_JASSTYPE     = new RGB(34, 136, 143);
	public static final RGB SYNTAXCOLOR_RGB_KEYWORD      = new RGB(127, 0, 85);
	public static final RGB SYNTAXCOLOR_RGB_STRING       = new RGB(42, 0, 255);
	public static final RGB SYNTAXCOLOR_RGB_COMMENT      = new RGB(63, 127, 95);
	public static final RGB SYNTAXCOLOR_RGB_FUNCTION     = COLOR_BLACK;
	public static final RGB SYNTAXCOLOR_RGB_DATATYPE     = new RGB(64, 64, 64);
	public static final RGB SYNTAXCOLOR_RGB_VAR          = COLOR_BLACK;
	public static final RGB SYNTAXCOLOR_RGB_PARAM        = COLOR_BLACK;
	public static final RGB SYNTAXCOLOR_RGB_FIELD        = new RGB(0, 0, 128); 
	public static final RGB SYNTAXCOLOR_RGB_INTERFACE    = SYNTAXCOLOR_RGB_FIELD;          
	public static final RGB SYNTAXCOLOR_RGB_CONSTRUCTOR  = COLOR_BLACK;

	//Partition Scanner
	public static final String PARTITION_SINLGE_LINE_COMMENT = "singleComment";
	public static final String PARTITION_MULTI_LINE_COMMENT  = "multiComment";
	public static final String PARTITION_STRING              = "string";
	public static final String PARTITION_CHARACTER           = "char";
	public static final String[] PARTITION_TYPES = {PARTITION_SINLGE_LINE_COMMENT, PARTITION_MULTI_LINE_COMMENT, PARTITION_STRING, PARTITION_CHARACTER};
	
	// marker
	public static final String START_POS = PLUGIN_PACKAGE + ".startPos";
	public static final String END_POS = PLUGIN_PACKAGE + ".endPos";

	public static final String PLUGIN_ID = "EclipseWurstPlugin";

	public static final String WURST_PARTITIONING = "__wurst_partitioning";
	
}
