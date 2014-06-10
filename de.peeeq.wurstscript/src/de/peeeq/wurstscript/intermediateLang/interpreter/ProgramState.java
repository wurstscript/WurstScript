package de.peeeq.wurstscript.intermediateLang.interpreter;

import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.eclipse.jdt.annotation.Nullable;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import de.peeeq.wurstio.jassinterpreter.InterpreterException;
import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.gui.WurstGui;
import de.peeeq.wurstscript.intermediateLang.ILconst;
import de.peeeq.wurstscript.jassIm.ImClass;
import de.peeeq.wurstscript.jassIm.ImFunction;
import de.peeeq.wurstscript.jassIm.ImProg;
import de.peeeq.wurstscript.jassIm.ImStmt;
import de.peeeq.wurstscript.parser.WPos;

public class ProgramState extends State {

	public static final int GENERATED_BY_WURST = 42;
	private @Nullable ImStmt lastStatement;
	protected WurstGui gui;
	private PrintStream outStream = System.out;
	private List<NativesProvider> nativeProviders = Lists.newArrayList();
	private ImProg prog;
	private int objectIdCounter;
	private Map<Integer, Object> objectToClassKey = Maps.newLinkedHashMap();
	private Stack<ILStackFrame> stackFrames = new Stack<>();
	private Stack<ImStmt> lastStatements = new Stack<>();
	
	
	public ProgramState(WurstGui gui, ImProg prog) {
		this.gui = gui;
		this.prog = prog;
	}

	public void setLastStatement(ImStmt s) {
		lastStatement = s;		
	}

	public @Nullable ImStmt getLastStatement() {
		return lastStatement;
	}

	public WurstGui getGui() {
		return gui;
	}



	public void writeBack(boolean injectObjects) {
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
		objectToClassKey.put(objectIdCounter, classKey(clazz));
		return objectIdCounter;
	}

	protected Object classKey(ImClass clazz) {
		return clazz;
	}

	public void deallocate(int obj, ImClass clazz, AstElement trace) {
		assertAllocated(obj, trace);
		objectToClassKey.remove(obj);
		// TODO recycle ids
	}

	public void assertAllocated(int obj, AstElement trace) {
		if (obj == 0) {
			throw new InterpreterException(trace, "Null pointer derefenced");
		}
		if (!objectToClassKey.containsKey(obj)) {
			throw new InterpreterException(trace, "Object already destroyed");
		}
	}

	public boolean isInstanceOf(int obj, ImClass clazz, AstElement trace) {
		assertAllocated(obj, trace);
		return getObjectClass(obj).isSubclassOf(clazz); // TODO more efficient check
	}

	public int getTypeId(int obj,  AstElement trace) {
		assertAllocated(obj, trace);
		return getObjectClass(obj).attrTypeId();
	}

	private ImClass getObjectClass(int obj) {
		return findClazz(objectToClassKey.get(obj));
	}

	private ImClass findClazz(Object key) {
		for (ImClass c : prog.getClasses()) {
			if (classKey(c).equals(key)) {
				return c;
			}
		}
		throw new Error("no class found for key " + key);
	}

	public void pushStackframe(ImFunction f, ILconst[] args, WPos trace) {
		stackFrames.push(new ILStackFrame(f, args, trace));
		lastStatements.push(lastStatement);
	}

	public void popStackframe() {
		if (!stackFrames.isEmpty()) {
			stackFrames.pop();
		}
		if (!lastStatements.empty()) {
			lastStatement = lastStatements.pop();
		}
	}
	
	public void resetStackframes() {
		stackFrames.clear();
		lastStatements.clear();
	}
	
	public Stack<ILStackFrame> getStackFrames() {
		return stackFrames;
	}


	
}

