package de.peeeq.wurstscript.intermediateLang.interpreter;

import java.io.File;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.gui.WurstGui;
import de.peeeq.wurstscript.intermediateLang.ILconst;
import de.peeeq.wurstscript.jassIm.ImClass;
import de.peeeq.wurstscript.jassIm.ImProg;
import de.peeeq.wurstscript.jassIm.ImStmt;

public class ProgramState extends State {

	public static final int GENERATED_BY_WURST = 42;
	private ImStmt lastStatement;
	private WurstGui gui;
	private File mapFile;
	private int id = 0;
	private PrintStream outStream = System.out;
	private List<NativesProvider> nativeProviders = Lists.newArrayList();
	private ImProg prog;
	private int objectIdCounter;
	private Map<Integer, ImClass> objectToClass = Maps.newHashMap();

	public ProgramState(File mapFile, WurstGui gui) {
		this.gui = gui;
		this.mapFile = mapFile;
	}

	public void setLastStatement(ImStmt s) {
		lastStatement = s;		
	}

	public ImStmt getLastStatement() {
		return lastStatement;
	}

	public WurstGui getGui() {
		return gui;
	}



	public void writeBack() {
		gui.sendProgress("Writing back generated objects", 0.9);
	}


	public PrintStream getOutStream() {
		return outStream ;
	}
	
	public void setOutStream(PrintStream os) {
		outStream = os;
		for (NativesProvider natives : nativeProviders) {
			natives.setOutStream(os);
		}
	}

	public void addNativeProvider(NativesProvider np) {
		np.setOutStream(outStream);
		nativeProviders.add(np);
	}

	public Iterable<NativesProvider> getNativeProviders() {
		return nativeProviders;
	}

	public ProgramState setProg(ImProg p) {
		prog = p;
		return this;
	}
	
	public ImProg getProg() {
		return prog;
	}

	public int allocate(ImClass clazz, AstElement trace) {
		objectIdCounter++;
		objectToClass.put(objectIdCounter, clazz);
		return objectIdCounter;
	}

	public void deallocate(int obj, ImClass clazz, AstElement trace) {
		assertAllocated(obj, trace);
		objectToClass.remove(obj);
		// TODO recycle ids
	}

	public void assertAllocated(int obj, AstElement trace) {
		if (obj == 0) {
			throw new RuntimeException("Null pointer derefenced at " + trace);
		}
		if (!objectToClass.containsKey(obj)) {
			throw new InterprationError("Object already destroyed " + trace);
		}
	}

	public boolean isInstanceOf(int obj, ImClass clazz, AstElement trace) {
		assertAllocated(obj, trace);
		return objectToClass.get(obj).isSubclassOf(clazz); // TODO more efficient check
	}

	public int getTypeId(int obj,  AstElement trace) {
		assertAllocated(obj, trace);
		return objectToClass.get(obj).attrTypeId();
	}


	
}

