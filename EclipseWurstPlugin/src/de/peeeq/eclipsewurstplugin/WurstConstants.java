package de.peeeq.eclipsewurstplugin;

import org.eclipse.swt.graphics.RGB;

public class WurstConstants {

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

	public static final String SYNTAXCOLOR_TEXT        = "Text";
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
	
	public static final RGB SYNTAXCOLOR_RGB_TEXT         = new RGB(0, 0, 0);
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

	// config
	public static final String WURST_AUTOCOMPLETION_DELAY = "WURST_AUTOCOMPLETION_DELAY";
	public static final String WURST_ENABLE_AUTOCOMPLETE = "WURST_ENABLE_AUTOCOMPLETE";
	public static final String WURST_ENABLE_RECONCILING = "WURST_ENABLE_RECONCILING";
	public static final String WURST_RECONCILATION_DELAY = "WURST_RECONCILATION_DELAY";
	public static final String WURST_WC3_PATH = "WURST_WC3_PATH";
	public static final String WURST_MPQEDIT_PATH = "WURST_MPQEDIT_PATH";
	
	
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
