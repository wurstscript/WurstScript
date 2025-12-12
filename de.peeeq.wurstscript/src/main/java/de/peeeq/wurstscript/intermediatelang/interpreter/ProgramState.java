package de.peeeq.wurstscript.intermediatelang.interpreter;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import de.peeeq.wurstio.jassinterpreter.InterpreterException;
import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.gui.WurstGui;
import de.peeeq.wurstscript.intermediatelang.*;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.parser.WPos;
import de.peeeq.wurstscript.translation.imtranslation.ImPrinter;
import de.peeeq.wurstscript.utils.LineOffsets;
import de.peeeq.wurstscript.utils.Utils;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.eclipse.jdt.annotation.Nullable;

import java.io.PrintStream;
import java.util.*;

public class ProgramState extends State {

    public static final int GENERATED_BY_WURST = 42;
    private de.peeeq.wurstscript.jassIm.Element lastStatement;
    protected WurstGui gui;
    private PrintStream outStream = System.err;
    private final List<NativesProvider> nativeProviders = Lists.newArrayList();
    private ImProg prog;
    private int objectIdCounter;
    private final Int2ObjectOpenHashMap<ILconstObject> indexToObject = new Int2ObjectOpenHashMap<>();
    private final Int2ObjectOpenHashMap<IlConstHandle> handleMap = new Int2ObjectOpenHashMap<>();
    private final Deque<ILStackFrame> stackFrames = new ArrayDeque<>();
    private final Deque<de.peeeq.wurstscript.jassIm.Element> lastStatements = new ArrayDeque<>();
    private final boolean isCompiletime;


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
            throw new InterpreterException(trace, "Null pointer dereference. The interpreter expected an object here, but found null.");
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


    public void pushStackframeWithTypes(ImFunction f, @Nullable ILconstObject receiver,
                                        ILconst[] args, WPos trace,
                                        Map<ImTypeVar, ImType> typeSubstitutions) {

        // normalize the incoming map: RHS = fully resolved type
        Map<ImTypeVar, ImType> normalized = new HashMap<>();
        for (Map.Entry<ImTypeVar, ImType> e : typeSubstitutions.entrySet()) {
            ImType rhs = resolveType(e.getValue()); // resolve through existing frames
            // skip self-maps (T -> T)
            if (rhs instanceof ImTypeVarRef &&
                ((ImTypeVarRef) rhs).getTypeVariable() == e.getKey()) {
                continue;
            }
            normalized.put(e.getKey(), rhs);
        }

//        System.out.println("pushStackframe " + f + " with receiver " + receiver
//            + " and args " + Arrays.toString(args) + " and typesubst " + normalized);

        stackFrames.push(new ILStackFrame(f, receiver, args, trace, normalized));
        de.peeeq.wurstscript.jassIm.Element stmt = this.lastStatement;
        if (stmt == null) stmt = f;
        lastStatements.push(stmt);
    }


    // NEW: Resolve a generic type using current stack frame's type substitutions
// Replace both resolveType(...) and substituteTypeVars(...) with this:

    public ImType resolveType(ImType t) {
        return resolveTypeDeep(t, 32); // small budget to avoid cycles
    }

    private ImType resolveTypeDeep(ImType t, int budget) {
        if (budget <= 0 || t == null) return t;

        return t.match(new ImType.Matcher<ImType>() {
            @Override
            public ImType case_ImTypeVarRef(ImTypeVarRef tvr) {
                // search from top-most frame down
                for (ILStackFrame sf : stackFrames) { /* we iterate deque top-first: use an iterator if needed */
                    ImType mapped = sf.typeSubstitutions.get(tvr.getTypeVariable());
                    if (mapped != null) {
                        ImType resolved = resolveTypeDeep(mapped, budget - 1);
                        return resolved;
                    }
                }
                // no mapping anywhere -> keep the type var
                return tvr;
            }

            @Override
            public ImType case_ImClassType(ImClassType ct) {
                if (ct.getTypeArguments().isEmpty()) return ct;
                ImTypeArguments newArgs = JassIm.ImTypeArguments();
                boolean changed = false;
                for (ImTypeArgument ta : ct.getTypeArguments()) {
                    ImType rt = resolveTypeDeep(ta.getType(), budget - 1);
                    newArgs.add(JassIm.ImTypeArgument(rt, ta.getTypeClassBinding()));
                    changed |= (rt != ta.getType());
                }
                return changed ? JassIm.ImClassType(ct.getClassDef(), newArgs) : ct;
            }

            @Override
            public ImType case_ImArrayType(ImArrayType at) {
                ImType et = resolveTypeDeep(at.getEntryType(), budget - 1);
                return et == at.getEntryType() ? at : JassIm.ImArrayType(et);
            }

            @Override
            public ImType case_ImArrayTypeMulti(ImArrayTypeMulti at) {
                ImType et = resolveTypeDeep(at.getEntryType(), budget - 1);
                return et == at.getEntryType() ? at : JassIm.ImArrayTypeMulti(et, at.getArraySize());
            }

            @Override
            public ImType case_ImTupleType(ImTupleType tt) {
                List<ImType> nts = new ArrayList<>();
                List<String> names = new ArrayList<>();
                boolean changed = false;
                var i = 0;
                for (ImType it : tt.getTypes()) {
                    ImType rt = resolveTypeDeep(it, budget - 1);
                    nts.add(rt);
                    changed |= (rt != it);
                    i++;
                    names.add(i + "");
                }
                return changed ? JassIm.ImTupleType(nts, names) : tt;
            }

            @Override public ImType case_ImSimpleType(ImSimpleType st) { return st; }
            @Override public ImType case_ImAnyType(ImAnyType at)       { return at; }
            @Override public ImType case_ImVoid(ImVoid v)              { return v; }
        });
    }


    // Helper method to substitute type variables
    private ImType substituteTypeVars(ImType type, Map<ImTypeVar, ImType> substitutions) {
        return type.match(new ImType.Matcher<ImType>() {
            @Override
            public ImType case_ImTypeVarRef(ImTypeVarRef typeVarRef) {
                ImType concrete = substitutions.get(typeVarRef.getTypeVariable());
                return concrete != null ? concrete : typeVarRef;
            }

            @Override
            public ImType case_ImClassType(ImClassType classType) {
                // Recursively substitute in type arguments
                ImTypeArguments newArgs = JassIm.ImTypeArguments();
                for (ImTypeArgument arg : classType.getTypeArguments()) {
                    ImType substituted = substituteTypeVars(arg.getType(), substitutions);
                    newArgs.add(JassIm.ImTypeArgument(substituted, arg.getTypeClassBinding()));
                }
                return JassIm.ImClassType(classType.getClassDef(), newArgs);
            }

            // For other types, return as-is
            @Override
            public ImType case_ImSimpleType(ImSimpleType t) {
                return t;
            }

            @Override
            public ImType case_ImArrayType(ImArrayType t) {
                return t;
            }

            @Override
            public ImType case_ImTupleType(ImTupleType t) {
                return t;
            }

            @Override
            public ImType case_ImVoid(ImVoid t) {
                return t;
            }

            @Override
            public ImType case_ImAnyType(ImAnyType t) {
                return t;
            }

            @Override
            public ImType case_ImArrayTypeMulti(ImArrayTypeMulti t) {
                return t;
            }
        });
    }

    public void pushStackframe(ImCompiletimeExpr f, WPos trace) {
//        System.out.println("pushStackframe compiletime expr " + f);
        stackFrames.push(new ILStackFrame(f, trace));
        de.peeeq.wurstscript.jassIm.Element stmt = this.lastStatement;
        if (stmt == null) {
            stmt = f;
        }
        lastStatements.push(stmt);
    }

    public void popStackframe() {
//        System.out.println("popStackframe " + (stackFrames.isEmpty() ? "empty" : stackFrames.peek().f));
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

    public Map<Integer, IlConstHandle> getHandleMap() {
        return handleMap;
    }

    public ILconst getHandleByIndex(int val) {
        return handleMap.get(val);
    }

    public ILconstObject ensureObject(ImClassType clazz, int objectId, Element trace) {
        ILconstObject existing = indexToObject.get(objectId);
        if (existing != null) {
            return existing;
        }
        ILconstObject res = new ILconstObject(clazz, objectId, trace);
        indexToObject.put(objectId, res);
        return res;
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

        public StackTrace(Deque<ILStackFrame> frames) {
            // copy once into an array-backed list
            this.stackFrames = Collections.unmodifiableList(new ArrayList<>(frames));
        }

        public void appendTo(StringBuilder sb) {
            for (ILStackFrame sf : stackFrames) {
                sb.append(sf.getMessage()).append('\n');
            }
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(stackFrames.size() * 32);
            appendTo(sb);
            return sb.toString();
        }

        public List<ILStackFrame> getStackFrames() {
            return stackFrames;
        }

        public Iterable<ILStackFrame> getStackFramesReversed() {
            return () -> new Iterator<ILStackFrame>() {
                int i = stackFrames.size() - 1;

                @Override
                public boolean hasNext() {
                    return i >= 0;
                }

                @Override
                public ILStackFrame next() {
                    return stackFrames.get(i--);
                }
            };
        }
    }

    private final Object2ObjectOpenHashMap<String, ILconst> genericStaticVals = new Object2ObjectOpenHashMap<>();
    private final Object2ObjectOpenHashMap<String, ILconstArray> genericStaticArrays = new Object2ObjectOpenHashMap<>();

    private final Object2ObjectOpenHashMap<String, ImClass> genericStaticOwnerCache = new Object2ObjectOpenHashMap<>();


    private @Nullable ImClass tryGetGenericOwnerClassForGlobal(ImVar v) {
        if (!v.isGlobal()) return null;

        String name = v.getName();
        int underscore = name.indexOf('_');
        if (underscore <= 0) return null;

        String className = name.substring(0, underscore);

        ImClass cached = genericStaticOwnerCache.get(className);
        if (cached != null) {
            // could be non-generic too; we store it anyway and check below
            return cached.getTypeVariables().isEmpty() ? null : cached;
        }

        // lookup class by name (fast enough; cached afterwards)
        for (ImClass c : prog.getClasses()) {
            if (c.getName().equals(className)) {
                genericStaticOwnerCache.put(className, c);
                return c.getTypeVariables().isEmpty() ? null : c;
            }
        }

        // remember "not found" as null by storing a dummy? keep simple: don't cache misses
        return null;
    }

    private @Nullable ILStackFrame topFrameWithReceiver() {
        ILStackFrame top = stackFrames.peek();
        return (top != null && top.receiver != null) ? top : null;
    }

    private @Nullable String genericStaticKey(ImVar v) {
        // Old behavior: also treat "static T ..." lowered to global of ImTypeVarRef as generic
        boolean isTypeVarStatic = v.getType() instanceof ImTypeVarRef;

        // New behavior: treat ANY global that belongs to a generic class as generic-static
        ImClass genericOwner = tryGetGenericOwnerClassForGlobal(v);

        if (!isTypeVarStatic && genericOwner == null) {
            return null;
        }

        ILStackFrame top = topFrameWithReceiver();
        if (top == null) {
            // No receiver context => cannot pick instantiation; fall back to shared global semantics
            return null;
        }

        // Use the receiver's *concrete* type (already concrete for objects)
        ImClassType recvType = top.receiver.getType();
        return v.getName() + "@" + instantiationKey(recvType);
    }



    public String instantiationKey(ImClassType ct) {
        StringBuilder sb = new StringBuilder();
        sb.append(ct.getClassDef().getName());
        if (!ct.getTypeArguments().isEmpty()) {
            sb.append('<');
            boolean first = true;
            for (ImTypeArgument ta : ct.getTypeArguments()) {
                if (!first) sb.append(',');
                first = false;
                sb.append(ta.toString()); // or your own stable printer
            }
            sb.append('>');
        }
        return sb.toString();
    }

    @Override
    public void setVal(ImVar v, ILconst val) {
        String key = genericStaticKey(v);
        if (key != null) {
            genericStaticVals.put(key, val);
            return;
        }
        super.setVal(v, val);
    }

    @Override
    public @Nullable ILconst getVal(ImVar v) {
        String key = genericStaticKey(v);
        if (key != null) {
            ILconst r = genericStaticVals.get(key);
            if (r != null) return r;
            // If never written, fall through to default global storage (which might have init/defaults)
        }
        return super.getVal(v);
    }

    public boolean isCompiletime() {
        return isCompiletime;
    }

    // Reuse a shared, empty LocalState (ensure it's safe/side-effect free)
    private static final LocalState EMPTY_LOCAL_STATE = new LocalState();

    @Override
    protected ILconstArray getArray(ImVar v) {
        String key = genericStaticKey(v);
        if (key != null) {
            ILconstArray r = genericStaticArrays.get(key);
            if (r != null) return r;

            r = createArrayConstantFromType(v.getType());
            genericStaticArrays.put(key, r);

            // Initialize from globalInits only once per instantiation
            List<ImSet> inits = prog.getGlobalInits().get(v);
            if (inits != null && !inits.isEmpty()) {
                final LocalState ls = EMPTY_LOCAL_STATE;
                for (int i = 0; i < inits.size(); i++) {
                    ILconst val = inits.get(i).getRight().evaluate(this, ls);
                    r.set(i, val);
                }
            }
            return r;
        }

        // old behavior for non-generic statics
        Object2ObjectOpenHashMap<ImVar, ILconstArray> arrayValues = ensureArrayValues();
        ILconstArray r = arrayValues.get(v);
        if (r != null) return r;

        r = createArrayConstantFromType(v.getType());
        arrayValues.put(v, r);

        List<ImSet> inits = prog.getGlobalInits().get(v);
        if (inits != null && !inits.isEmpty()) {
            final LocalState ls = EMPTY_LOCAL_STATE;
            for (int i = 0; i < inits.size(); i++) {
                ILconst val = inits.get(i).getRight().evaluate(this, ls);
                r.set(i, val);
            }
        }
        return r;
    }


    public Collection<ILconstObject> getAllObjects() {
        return indexToObject.values();
    }

}

