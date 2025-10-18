package de.peeeq.wurstscript.attributes;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.attributes.names.FuncLink;
import de.peeeq.wurstscript.attributes.names.NameResolution;
import de.peeeq.wurstscript.types.FunctionSignature;
import de.peeeq.wurstscript.types.FunctionSignature.ArgsMatchResult;
import de.peeeq.wurstscript.types.VariableBinding;
import de.peeeq.wurstscript.types.VariablePosition;
import de.peeeq.wurstscript.types.WurstType;

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
        // Fast path: nothing to consider
        if (res.isEmpty()) {
            return ImmutableList.of();
        }

        // Materialize once to a random-access list (cheap for Immutable*)
        final ImmutableList<FunctionSignature> sigs =
            (res instanceof ImmutableList)
                ? (ImmutableList<FunctionSignature>) res
                : ImmutableList.copyOf(res);

        // Compute arg types once
        final List<WurstType> argTypes = AttrFuncDef.argumentTypesPre(fc);

        // --- Pass 1: exact matches only (cheap) ---------------------------------
        // Use a single ArrayList and only copy if we actually have matches.
        List<FunctionSignature> exact = new java.util.ArrayList<>(sigs.size());
        for (int i = 0, n = sigs.size(); i < n; i++) {
            FunctionSignature matched = sigs.get(i).matchAgainstArgs(argTypes, fc);
            if (matched != null) {
                exact.add(matched);
            }
        }
        if (!exact.isEmpty()) {
            return ImmutableList.copyOf(exact);
        }

        // --- Pass 2: best-effort matches (no exact match) ------------------------
        // We must:
        //  * find the min-badness result (to emit its errors)
        //  * return ALL resulting signatures (to preserve current semantics)
        final int n = sigs.size();
        FunctionSignature[] inferredSigs = new FunctionSignature[n];
        int bestIdx = -1;
        int bestBadness = Integer.MAX_VALUE;
        ArgsMatchResult bestResult = null;

        // Cache args node once
        final Arguments argsNode = fc.getArgs();

        for (int i = 0; i < n; i++) {
            // tryMatchAgainstArgs may also perform type-arg inference; we must keep its result sig
            ArgsMatchResult r = sigs.get(i).tryMatchAgainstArgs(argTypes, argsNode, fc);
            inferredSigs[i] = r.getSig();
            int b = r.getBadness();
            if (b < bestBadness) {
                bestBadness = b;
                bestIdx = i;
                bestResult = r;
            }
        }

        if (bestIdx == -1 || bestResult == null) {
            // Shouldn’t happen, but be safe
            return ImmutableList.of();
        }

        // Emit errors from the best match (same as before)
        for (CompileError c : bestResult.getErrors()) {
            fc.getErrorHandler().sendError(c);
        }

        // Return ALL candidate signatures (same as previous behavior)
        // Avoid another stream/collect
        ImmutableList.Builder<FunctionSignature> out = ImmutableList.builderWithExpectedSize(n);
        for (int i = 0; i < n; i++) {
            out.add(inferredSigs[i]);
        }
        return out.build();
    }

    public static com.google.common.collect.ImmutableCollection<FunctionSignature> calculate(ExprMemberMethod mm) {
        // 1) Collect all member candidates (includes inherited overloads)
        WurstType recvT = mm.getLeft().attrTyp();
        com.google.common.collect.ImmutableCollection<FuncLink> fs =
            NameResolution.lookupMemberFuncs(mm, recvT, mm.getFuncName(), /*showErrors=*/false);

        // 2) Convert to signatures using the bound link (receiver/params/return + type-arg binding)
        com.google.common.collect.ImmutableList.Builder<FunctionSignature> out = com.google.common.collect.ImmutableList.builder();
        for (FuncLink f : fs) {
            FunctionSignature sig = FunctionSignature.fromNameLink(f);

            // Apply any explicit/given type-args at the call-site to the signature mapping:
            VariableBinding mapping = givenBinding(mm, sig.getDefinitionTypeVariables());
            sig = sig.setTypeArgs(mm, mapping);

            out.add(sig);
        }

        // 3) Reuse the common “pick best” logic (exact matches first, then best-errors)
        return findBestSignature(mm, out.build());
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
