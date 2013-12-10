package de.peeeq.wurstscript;

import java.util.List;

import com.google.common.collect.Lists;

public class RunArgs {

	private List<String> files  = Lists.newArrayList();
	private boolean optimize = false;
	private boolean gui = false;
	private String mapFile = null;
	private String outFile = null;
	private boolean showAbout = false;
	private boolean showLastErrors = false;
	private boolean inline;
	private boolean runCompiletimeFunctions;
	private boolean runtests;
	private boolean createHotDoc;
	private boolean localOptimizations;
	private boolean includeStacktraces;
	private boolean noDebugMessages;
	
	public static RunArgs defaults() {
		return new RunArgs(new String[] {});
	}
	
	public RunArgs(String[] args) {
		for (int i=0; i<args.length; i++) {
			String a = args[i];
			if (a.startsWith("-")) {
				if (a.equals("-help")) {
					printHelpAndExit();
				} else if (a.equals("-opt")) {
					this.optimize = true;
				} else if (a.equals("-inline")) {
					inline = true;
				} else if (a.equals("-localOptimizations")) {
					localOptimizations = true;
				} else if (a.equals("-runtests")) {
					runtests = true;
				} else if (a.equals("-gui")) {
					this.gui = true;
				} else if (a.equals("-out")) {
					i++;
					this.outFile = args[i];
				} else if (a.equals("--about")) {
					this.showAbout = true;			
				} else if (a.equals("--hotdoc")) {
					this.createHotDoc = true;
				} else if (a.equals("--showerrors")) {
					this.showLastErrors = true;
				} else if (a.equals("-runcompiletimefunctions")) {
					runCompiletimeFunctions  = true;
				} else if (a.equals("-stacktraces")) {
					includeStacktraces = true;
				} else if (a.equals("-nodebug")) {
					noDebugMessages = true;
				} else {
					throw new Error("Unknown option: " + a);
				}
			} else {
				files.add(a);
				if (a.endsWith(".w3x") || a.endsWith(".w3g")) {
					mapFile = a;
				}
			}
		}
	}

	public RunArgs(List<String> runArgs) {
		this(runArgs.toArray(new String[runArgs.size()]));
	}

	public static void printHelpAndExit() {
		System.out.println("Usage: ");
		System.out.println("wurst <options> <files>");
		System.out.println();
		System.out.println("Example: wurst -opt common.j Blizzard.j myMap.w3x");
		System.out.println("Compiles the given map with the two script files and optimizations enabled.");
		System.out.println();
		System.out.println("Options:");
		System.out.println("-help");
		System.out.println("	Prints this help message.");
		System.out.println();
		System.out.println("-opt");
		System.out.println("	Enable the Froptimizer. Compresses names.");
		System.out.println();
		System.out.println("-localOptimizations");
		System.out.println("	Experimental feature. Does some local optimizations like removing redundant variables.");
		System.out.println();
		System.out.println("-runtests");
		System.out.println("	Run all test functions found in the scripts.");
		System.out.println();
		System.out.println("-gui");
		System.out.println("	Show a graphical user interface.");
		System.out.println();
		System.out.println("-out <filename>");
		System.out.println("	Write the translated script to the given file.");
		System.out.println();
		System.out.println("--about");
		System.out.println("	Shows the 'about' window.");
		System.out.println();
		System.out.println("--hotdoc");
		System.out.println("	Generate hotdoc html documentation.");
		System.out.println();
		System.out.println("--showerrors");
		System.out.println("	Show errors from last compilation. Not yet implemented.");
		System.out.println();
		System.out.println("--runcompiletimefunctions");
		System.out.println("	Run all compiletime functions found in the scripts.");
		System.out.println();
		System.exit(0);
	}

	public List<String> getFiles() {
		return files;
	}

	public boolean isOptimize() {
		return optimize;
	}

	public boolean isGui() {
		return gui;
	}

	public String getMapFile() {
		return mapFile;
	}

	public String getOutFile() {
		return outFile;
	}

	public boolean showAbout() {
		return showAbout;
	}

	public boolean showLastErrors() {
		return showLastErrors;
	}

	public boolean isInline() {
		return inline;
	}

	public boolean runCompiletimeFunctions() {
		return runCompiletimeFunctions;
	}

	public boolean runtests() {
		return runtests;
	}

	public boolean createHotDoc() {
		return createHotDoc;
	}

	public boolean isNullsetting() {
		return true;
	}

	public boolean isLocalOptimizations() {
		return localOptimizations;
	}

	public boolean isIncludeStacktraces() {
		return includeStacktraces;
	}
	
	public boolean isNoDebugMessages() {
		return noDebugMessages;
	}
}
