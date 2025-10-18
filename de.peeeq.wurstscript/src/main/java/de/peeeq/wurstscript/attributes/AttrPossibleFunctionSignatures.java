package de.peeeq.wurstscript.attributes;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.attributes.names.FuncLink;
import de.peeeq.wurstscript.attributes.names.NameResolution;
import de.peeeq.wurstscript.attributes.names.Visibility;
import de.peeeq.wurstscript.types.*;
import de.peeeq.wurstscript.types.FunctionSignature.ArgsMatchResult;

import java.util.ArrayList;
import java.util.List;

import static de.peeeq.wurstscript.attributes.GenericsHelper.givenBinding;
import static de.peeeq.wurstscript.attributes.GenericsHelper.typeParameters;

public class AttrPossibleFunctionSignatures {

    public static ImmutableCollection<FunctionSignature> calculate(FunctionCall fc) {
        ImmutableCollection<FuncLink> fs = fc.attrPossibleFuncDefs();
        ImmutableCollection.Builder<FunctionSignature> resultBuilder = ImmutableList.builder();
        for (FuncLink f : fs) {
            FunctionSignature sig = FunctionSignature.fromNameLink(f);

            OptExpr implicitParameterOpt = AttrImplicitParameter.getFunctionCallImplicitParameter(fc, f, false);
            if (implicitParameterOpt instanceof Expr) {
                Expr expr = (Expr) implicitParameterOpt;
                VariableBinding mapping = expr.attrTyp().matchAgainstSupertype(sig.getReceiverType(), fc, sig.getMapping(), VariablePosition.RIGHT);
                if (mapping == null) {
                    // TODO error message? Or just ignore wrong parameter type?
                    continue;
                } else {
                    sig = sig.setTypeArgs(fc, mapping);
                }
            } // TODO else check?

            VariableBinding mapping = givenBinding(fc, sig.getDefinitionTypeVariables());
            sig = sig.setTypeArgs(fc, mapping);

            resultBuilder.add(sig);
        }
        return findBestSignature(fc, resultBuilder.build());
    }

    private static ImmutableCollection<FunctionSignature> findBestSignature(StmtCall fc,
                                                                            ImmutableCollection<FunctionSignature> res) {
        if (res.isEmpty()) return ImmutableList.of();

        final ImmutableList<FunctionSignature> sigs = (res instanceof ImmutableList)
            ? (ImmutableList<FunctionSignature>) res
            : ImmutableList.copyOf(res);

        final List<WurstType> argTypes = AttrFuncDef.argumentTypesPre(fc);

        // Pass 1: exact matches
        List<FunctionSignature> exact = new ArrayList<>(sigs.size());
        for (FunctionSignature s : sigs) {
            FunctionSignature m = s.matchAgainstArgs(argTypes, fc);
            if (m != null) exact.add(m);
        }
        if (!exact.isEmpty()) return ImmutableList.copyOf(exact);

        // Pass 2: best effort + (maybe) errors
        boolean allKnown = true;
        for (WurstType t : argTypes) {
            if (t instanceof WurstTypeUnknown) { allKnown = false; break; }
        }

        int n = sigs.size(), bestIdx = -1, bestBad = Integer.MAX_VALUE;
        FunctionSignature[] inferred = new FunctionSignature[n];
        FunctionSignature.ArgsMatchResult best = null;

        final Arguments argsNode = fc.getArgs();
        for (int i = 0; i < n; i++) {
            var r = sigs.get(i).tryMatchAgainstArgs(argTypes, argsNode, fc);
            inferred[i] = r.getSig();
            if (r.getBadness() < bestBad) { bestBad = r.getBadness(); bestIdx = i; best = r; }
        }

        if (allKnown && best != null) {
            for (var c : best.getErrors()) {
                fc.getErrorHandler().sendError(c);
            }
        }

        return ImmutableList.copyOf(inferred);
    }


    public static boolean isExtension(de.peeeq.wurstscript.attributes.names.FuncLink f) {
        return f.getDef() instanceof de.peeeq.wurstscript.ast.ExtensionFuncDef;
    }

    public static boolean isVisible(de.peeeq.wurstscript.attributes.names.FuncLink f) {
        Visibility v = f.getVisibility();
        // NameLinks pre-resolve visibility “relative to the current site”.
        // *_OTHER means: *not* visible here.
        return v != Visibility.PRIVATE_OTHER && v != Visibility.PROTECTED_OTHER;
    }

    public static <T> java.util.List<T> keepMostSpecificReceivers(
        java.util.List<T> candidates,
        java.util.function.Function<T, de.peeeq.wurstscript.types.WurstType> recvOf,
        de.peeeq.wurstscript.ast.Element site // for isSubtypeOf diagnostics
    ) {
        if (candidates.size() <= 1) return candidates;
        java.util.ArrayList<T> kept = new java.util.ArrayList<>(candidates.size());
        outer:
        for (int i = 0; i < candidates.size(); i++) {
            var ri = recvOf.apply(candidates.get(i));
            if (ri == null) { kept.add(candidates.get(i)); continue; } // static/global
            for (int j = 0; j < candidates.size(); j++) {
                if (i == j) continue;
                var rj = recvOf.apply(candidates.get(j));
                if (rj == null) continue;
                // If rj is a strict subtype of ri, drop ri
                boolean rj_le_ri = rj.isSubtypeOf(ri, site);
                boolean ri_le_rj = ri.isSubtypeOf(rj, site);
                if (rj_le_ri && !ri_le_rj) {
                    continue outer;
                }
            }
            kept.add(candidates.get(i));
        }
        return kept.isEmpty() ? candidates : kept;
    }



    public static ImmutableCollection<FunctionSignature> calculate(ExprMemberMethod mm) {
        // Receiver and method name
        final Expr left = mm.getLeft();
        final WurstType leftType = left.attrTyp();
        final String name = mm.getFuncName();

        // Collect raw member candidates (no errors yet)
        final ImmutableCollection<FuncLink> raw =
            mm.lookupMemberFuncs(leftType, name, /*showErrors*/ false);

        if (raw.isEmpty()) {
            // Let downstream handle "not found"
            return ImmutableList.of();
        }

        // Partition by accessibility
        final java.util.List<FuncLink> visible = new java.util.ArrayList<>(raw.size());
        final java.util.List<FuncLink> hidden  = new java.util.ArrayList<>(2);
        for (FuncLink f : raw) {
            de.peeeq.wurstscript.attributes.names.Visibility v = f.getVisibility();
            boolean accessible = (v != de.peeeq.wurstscript.attributes.names.Visibility.PRIVATE_OTHER
                && v != de.peeeq.wurstscript.attributes.names.Visibility.PROTECTED_OTHER);
            if (accessible) {
                visible.add(f);
            } else {
                hidden.add(f);
            }
        }

        if (visible.isEmpty()) {
            // Only hidden matches exist → keep the old precise message
            if (!hidden.isEmpty()) {
                mm.addError(
                    de.peeeq.wurstscript.utils.Utils.printElement(java.util.Optional.of(hidden.get(0).getDef()))
                        + " is not visible here.");
            }
            // Return non-empty to avoid a second generic "not found" message
            return ImmutableList.of(FunctionSignature.empty);
        }

        // Prepare function signatures: bind receiver + explicit type args
        final java.util.List<FunctionSignature> prepared = new java.util.ArrayList<>(visible.size());
        for (FuncLink f : visible) {
            FunctionSignature sig = FunctionSignature.fromNameLink(f);

            // Bind type variables using the actual receiver
            WurstType recv = sig.getReceiverType();
            if (recv != null) {
                VariableBinding m = leftType.matchAgainstSupertype(
                    recv, mm, sig.getMapping(), VariablePosition.RIGHT);
                if (m == null) {
                    // Should not happen; lookupMemberFuncs already checked. Skip defensively.
                    continue;
                }
                sig = sig.setTypeArgs(mm, m);
            }

            // Apply explicit type args from the call-site (e.g., c.foo<T,...>(...))
            VariableBinding explicit = GenericsHelper.givenBinding(mm, sig.getDefinitionTypeVariables());
            sig = sig.setTypeArgs(mm, explicit);

            prepared.add(sig);
        }

        if (prepared.isEmpty()) {
            return ImmutableList.of();
        }

        // Rank by argument match FIRST (fixes subclass-overload case)
        final java.util.List<WurstType> argTypes = AttrFuncDef.argumentTypesPre(mm);
        final java.util.List<FunctionSignature.ArgsMatchResult> results =
            new java.util.ArrayList<>(prepared.size());

        int minBadness = Integer.MAX_VALUE;
        for (FunctionSignature sig : prepared) {
            FunctionSignature.ArgsMatchResult r =
                sig.tryMatchAgainstArgs(argTypes, mm.getArgs(), mm);
            results.add(r);
            if (r.getBadness() < minBadness) {
                minBadness = r.getBadness();
            }
        }

        // Keep only best-badness candidates
        final java.util.List<FunctionSignature.ArgsMatchResult> best =
            new java.util.ArrayList<>();
        for (FunctionSignature.ArgsMatchResult r : results) {
            if (r.getBadness() == minBadness) {
                best.add(r);
            }
        }

        // Among equally good matches, prefer the ones that fully infer type vars
        boolean anyFullyInferred = false;
        for (FunctionSignature.ArgsMatchResult r : best) {
            if (!r.getSig().getMapping().hasUnboundTypeVars()) {
                anyFullyInferred = true;
                break;
            }
        }
        if (anyFullyInferred) {
            final java.util.List<FunctionSignature.ArgsMatchResult> filtered = new java.util.ArrayList<>(best.size());
            for (FunctionSignature.ArgsMatchResult r : best) {
                if (!r.getSig().getMapping().hasUnboundTypeVars()) {
                    filtered.add(r);
                }
            }
            if (!filtered.isEmpty()) {
                best.clear();
                best.addAll(filtered);
            }
        }

        // Among still-equal matches, prefer the most-specific receiver
        if (best.size() > 1) {
            final int n = best.size();
            final boolean[] drop = new boolean[n];

            for (int i = 0; i < n; i++) {
                WurstType ri = best.get(i).getSig().getReceiverType();
                if (ri == null) continue;
                for (int j = 0; j < n; j++) {
                    if (i == j || drop[i]) continue;
                    WurstType rj = best.get(j).getSig().getReceiverType();
                    if (rj == null) continue;

                    boolean jSubI = rj.isSubtypeOf(ri, mm);
                    boolean iSubJ = ri.isSubtypeOf(rj, mm);

                    // j strictly more specific than i → drop i
                    if (jSubI && !iSubJ) {
                        drop[i] = true;
                    }
                }
            }

            boolean anyDrop = false;
            for (boolean d : drop) { if (d) { anyDrop = true; break; } }
            if (anyDrop) {
                java.util.List<FunctionSignature.ArgsMatchResult> filtered = new java.util.ArrayList<>(n);
                for (int i = 0; i < n; i++) {
                    if (!drop[i]) filtered.add(best.get(i));
                }
                if (!filtered.isEmpty()) {
                    best.clear();
                    best.addAll(filtered);
                }
            }
        }

        // IMPORTANT: do NOT emit errors here (defer to AttrFunctionSignature)
        ImmutableList.Builder<FunctionSignature> out = ImmutableList.builderWithExpectedSize(best.size());
        for (FunctionSignature.ArgsMatchResult r : best) {
            out.add(r.getSig());
        }
        return out.build();
    }


    public static ImmutableCollection<FunctionSignature> calculate(ExprNewObject fc) {
        TypeDef typeDef = fc.attrTypeDef();
        if (!(typeDef instanceof ClassDef)) {
            return ImmutableList.of();
        }

        ClassDef classDef = (ClassDef) typeDef;

        List<ConstructorDef> constructors = classDef.getConstructors();

        ImmutableList.Builder<FunctionSignature> res = ImmutableList.builder();
        for (ConstructorDef f : constructors) {
            WurstType returnType = classDef.attrTyp().dynamic();
            VariableBinding binding2 = givenBinding(fc, typeParameters(classDef));
            List<WurstType> paramTypes = Lists.newArrayList();
            for (WParameter p : f.getParameters()) {
                paramTypes.add(p.attrTyp());
            }
            List<String> pNames = FunctionSignature.getParamNames(f.getParameters());
            List<TypeParamDef> typeParams = classDef.getTypeParameters();
            VariableBinding mapping = VariableBinding.emptyMapping().withTypeVariables(typeParams);
            FunctionSignature sig = new FunctionSignature(f, mapping, null, "construct", paramTypes, pNames, returnType);
            sig = sig.setTypeArgs(fc, binding2);
            res.add(sig);
        }
        return findBestSignature(fc, res.build());
    }

}
