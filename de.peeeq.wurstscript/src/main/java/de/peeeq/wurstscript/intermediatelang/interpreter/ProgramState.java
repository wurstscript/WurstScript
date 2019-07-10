package de.peeeq.wurstscript.intermediatelang.interpreter;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import de.peeeq.wurstio.jassinterpreter.InterpreterException;
import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.gui.WurstGui;
import de.peeeq.wurstscript.intermediatelang.*;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.parser.WPos;
import de.peeeq.wurstscript.utils.LineOffsets;
import de.peeeq.wurstscript.utils.Utils;

import java.io.PrintStream;
import java.util.*;

public class ProgramState extends State {

    public static final int GENERATED_BY_WURST = 42;
    private de.peeeq.wurstscript.jassIm.Element lastStatement;
    protected WurstGui gui;
    private PrintStream outStream = System.err;
    private List<NativesProvider> nativeProviders = Lists.newArrayList();
    private ImProg prog;
    private int objectIdCounter;
    private HashMap<Integer, ILconstObject> indexToObject = new HashMap<>();
    private Deque<ILStackFrame> stackFrames = new ArrayDeque<>();
    private Deque<de.peeeq.wurstscript.jassIm.Element> lastStatements = new ArrayDeque<>();
    private boolean isCompiletime;
    private HashMap<Integer, IlConstHandle> handleMap = new HashMap<>();


    public ProgramState(WurstGui gui, ImProg prog, boolean isCompiletime) {
        this.gui = gui;
        this.prog = prog;
        this.isCompiletime = isCompiletime;
    }

    public void setLastStatement(ImStmt s) {
        lastStatement = s;
    }

    public de.peeeq.wurstscript.jassIm.Element getLastStatement() {
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

    public ILconstObject allocate(ImClassType clazz, Element trace) {
        objectIdCounter++;
        ILconstObject res = new ILconstObject(clazz, objectIdCounter, trace);
        indexToObject.put(objectIdCounter, res);
        return res;
    }

    protected Object classKey(ImClass clazz) {
        return clazz;
    }

    public void deallocate(ILconstObject obj, ImClass clazz, Element trace) {
        assertAllocated(obj, trace);
        obj.destroy();
        // TODO recycle ids
    }

    public void assertAllocated(ILconstObject obj, Element trace) {
        if (obj == null) {
            throw new InterpreterException(trace, "Null pointer dereference");
        }
        if (obj.isDestroyed()) {
            throw new InterpreterException(trace, "Object already destroyed");
        }
    }

    public boolean isInstanceOf(ILconstObject obj, ImClass clazz, Element trace) {
        if (obj == null) {
            return false;
        }
        assertAllocated(obj, trace);
        return obj.getImClass().isSubclassOf(clazz); // TODO more efficient check
    }

    public int getTypeId(ILconstObject obj, Element trace) {
        assertAllocated(obj, trace);
        return obj.getImClass().attrTypeId();
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
        de.peeeq.wurstscript.jassIm.Element stmt = this.lastStatement;
        if (stmt == null) {
            stmt = f;
        }
        lastStatements.push(stmt);
    }

    public void pushStackframe(ImCompiletimeExpr f, WPos trace) {
        stackFrames.push(new ILStackFrame(f, trace));
        de.peeeq.wurstscript.jassIm.Element stmt = this.lastStatement;
        if (stmt == null) {
            stmt = f;
        }
        lastStatements.push(stmt);
    }

    public void popStackframe() {
        if (!stackFrames.isEmpty()) {
            stackFrames.pop();
        }
        if (!lastStatements.isEmpty()) {
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

    public void compilationError(String errorMessage) {
        de.peeeq.wurstscript.jassIm.Element lastStatement = getLastStatement();
        if (lastStatement != null) {
            WPos source = lastStatement.attrTrace().attrSource();
            getGui().sendError(new CompileError(source, errorMessage));
        } else {
            getGui().sendError(new CompileError(new WPos("", new LineOffsets(), 0, 0), errorMessage));
        }
        for (ILStackFrame sf : Utils.iterateReverse(getStackFrames().getStackFrames())) {
            getGui().sendError(sf.makeCompileError());
        }
    }

    public ILconst getObjectByIndex(int val) {
        return indexToObject.get(val);
    }

    public HashMap<Integer, IlConstHandle> getHandleMap() {
        return handleMap;
    }

    public ILconst getHandleByIndex(int val) {
        return handleMap.get(val);
    }

    public ILconstObject toObject(ILconst val) {
        if (val instanceof ILconstObject) {
            return (ILconstObject) val;
        } else if (val instanceof ILconstInt) {
            return indexToObject.get(((ILconstInt) val).getVal());
        }
        throw new InterpreterException(this, "Value " + val + " (" + val.getClass().getSimpleName() + ") cannot be cast to object.");
    }

    public static class StackTrace {
        private final List<ILStackFrame> stackFrames;

        public StackTrace(Deque<ILStackFrame> stackFrames) {
            ImmutableList.Builder<ILStackFrame> builder = ImmutableList.builder();
            for (ILStackFrame stackFrame : stackFrames) {
                builder.add(stackFrame);
            }
            this.stackFrames = builder.build();
        }

        public void appendTo(StringBuilder sb) {
            for (ILStackFrame stackFrame : stackFrames) {
                sb.append(stackFrame.getMessage());
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

    protected ILconstArray getArray(ImVar v) {
        ILconstArray r = arrayValues.get(v);
        if (r == null) {
            ImType vType = v.getType();
            r = createArrayConstantFromType(vType);
            arrayValues.put(v, r);
            List<ImExpr> e = prog.getGlobalInits().get(v);
            if (e != null) {
                LocalState ls = new LocalState();
                for (int i = 0; i < e.size(); i++) {
                    ILconst val = e.get(i).evaluate(this, ls);
                    r.set(i, val);
                }
            }
        }
        return r;
    }



    public Collection<ILconstObject> getAllObjects() {
        return indexToObject.values();
    }

}

