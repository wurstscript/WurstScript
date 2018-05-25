package de.peeeq.wurstscript.intermediatelang.interpreter;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import de.peeeq.wurstio.jassinterpreter.InterpreterException;
import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.gui.WurstGui;
import de.peeeq.wurstscript.intermediatelang.ILconst;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.parser.WPos;
import org.eclipse.jdt.annotation.Nullable;

import java.io.PrintStream;
import java.util.*;

public class ProgramState extends State {

    public static final int GENERATED_BY_WURST = 42;
    private @Nullable ImStmt lastStatement;
    protected WurstGui gui;
    private PrintStream outStream = System.err;
    private List<NativesProvider> nativeProviders = Lists.newArrayList();
    private ImProg prog;
    private int objectIdCounter;
    private Map<Integer, Object> objectToClassKey = Maps.newLinkedHashMap();
    private Stack<ILStackFrame> stackFrames = new Stack<>();
    private Stack<ImStmt> lastStatements = new Stack<>();
    private boolean isCompiletime;


    public ProgramState(WurstGui gui, ImProg prog, boolean isCompiletime) {
        this.gui = gui;
        this.prog = prog;
        this.isCompiletime = isCompiletime;
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
    }


    public PrintStream getOutStream() {
        return outStream;
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

    public int allocate(ImClass clazz, Element trace) {
        objectIdCounter++;
        objectToClassKey.put(objectIdCounter, classKey(clazz));
        return objectIdCounter;
    }

    protected Object classKey(ImClass clazz) {
        return clazz;
    }

    public void deallocate(int obj, ImClass clazz, Element trace) {
        assertAllocated(obj, trace);
        objectToClassKey.remove(obj);
        // TODO recycle ids
    }

    public void assertAllocated(int obj, Element trace) {
        if (obj == 0) {
            throw new InterpreterException(trace, "Null pointer dereference");
        }
        if (!objectToClassKey.containsKey(obj)) {
            throw new InterpreterException(trace, "Object already destroyed");
        }
    }

    public boolean isInstanceOf(int obj, ImClass clazz, Element trace) {
        assertAllocated(obj, trace);
        return getObjectClass(obj).isSubclassOf(clazz); // TODO more efficient check
    }

    public int getTypeId(int obj, Element trace) {
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

    public StackTrace getStackFrames() {
        return new StackTrace(stackFrames);
    }

    public static class StackTrace {
        private final List<ILStackFrame> stackFrames;

        public StackTrace(Stack<ILStackFrame> stackFrames) {
            ImmutableList.Builder<ILStackFrame> builder = ImmutableList.builder();
            for (ILStackFrame stackFrame : stackFrames) {
                builder.add(stackFrame);
            }
            this.stackFrames = builder.build();
        }

        public void appendTo(StringBuilder sb) {
            for (int i = stackFrames.size()-1; i>= 0; i--) {
                sb.append(stackFrames.get(i).getMessage());
                sb.append("\n");
            }
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            appendTo(sb);
            return sb.toString();
        }

        public List<ILStackFrame> getStackFrames() {
            return stackFrames;
        }

        public Iterable<ILStackFrame> getStackFramesReversed() {
            return () -> {
                ListIterator<ILStackFrame> it = stackFrames.listIterator();
                return new Iterator<ILStackFrame>() {
                    @Override
                    public boolean hasNext() {
                        return it.hasPrevious();
                    }

                    @Override
                    public ILStackFrame next() {
                        return it.previous();
                    }
                };
            };
        }


    }

    public boolean isCompiletime() {
        return isCompiletime;
    }

    protected Map<Integer, ILconst> getArray(ImVar v) {
        Map<Integer, ILconst> r = arrayValues.get(v);
        if (r == null) {
            r = Maps.newLinkedHashMap();
            arrayValues.put(v, r);
            ImExpr e = prog.getGlobalInits().get(v);
            if (e instanceof ImTupleExpr) {
                ImTupleExpr te = (ImTupleExpr) e;
                LocalState ls = new LocalState();
                for (int i = 0; i < te.getExprs().size(); i++) {
                    ILconst val = te.getExprs().get(i).evaluate(this, ls);
                    r.put(i, val);
                }
            }
        }
        return r;
    }


}

