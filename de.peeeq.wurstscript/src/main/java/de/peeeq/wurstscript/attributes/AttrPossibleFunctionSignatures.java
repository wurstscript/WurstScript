package de.peeeq.wurstscript.attributes;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import de.peeeq.wurstscript.TypeClasses;
import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.attributes.names.FuncLink;
import de.peeeq.wurstscript.types.*;
import de.peeeq.wurstscript.types.FunctionSignature.ArgsMatchResult;
import de.peeeq.wurstscript.utils.Pair;
import de.peeeq.wurstscript.utils.Utils;

import java.util.*;
import java.util.stream.Collectors;

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

    private static ImmutableCollection<FunctionSignature> findBestSignature(StmtCall fc, ImmutableCollection<FunctionSignature> res) {
        ImmutableCollection.Builder<FunctionSignature> resultBuilder2 = ImmutableList.builder();
        ImmutableCollection.Builder<Pair<FunctionSignature, List<CompileError>>> typeClassErrors = ImmutableList.builder();
        List<WurstType> argTypes = AttrFuncDef.argumentTypesPre(fc);
        for (FunctionSignature sig : res) {
            FunctionSignature sig2 = sig.matchAgainstArgs(argTypes, fc);
            if (sig2 == null) {
                continue;
            }
            Pair<FunctionSignature, List<CompileError>> typeClassMatched = TypeClasses.findTypeClasses(sig2, fc);
            if (typeClassMatched.getB().isEmpty()) {
                resultBuilder2.add(typeClassMatched.getA());
            } else {
                typeClassErrors.add(typeClassMatched);
            }
        }
        ImmutableCollection<FunctionSignature> res2 = resultBuilder2.build();
        if (!res2.isEmpty()) {
            return res2;
        }
        // if no precise matches, check if there are any matches that just miss some type class constraints:
        ImmutableCollection<Pair<FunctionSignature, List<CompileError>>> typeClassErrorsL = typeClassErrors.build();
        if (!typeClassErrorsL.isEmpty()) {
            CompileError err = typeClassErrorsL.stream()
                .flatMap(x -> x.getB().stream())
                .findFirst()
                .get();

            fc.getErrorHandler().sendError(err);

            return typeClassErrorsL.stream()
                .map(Pair::getA)
                .collect(ImmutableList.toImmutableList());
        }

        // no signature matches precisely --> try to match as good as possible
        ImmutableList<ArgsMatchResult> match3 = res.stream()
            .map(sig -> sig.tryMatchAgainstArgs(argTypes, fc.getArgs(), fc))
            .collect(ImmutableList.toImmutableList());

        if (match3.isEmpty()) {
            return ImmutableList.of();
        } else {
            // add errors from best match (minimal badness)
            ArgsMatchResult min = Collections.min(match3, Comparator.comparing(ArgsMatchResult::getBadness));
            for (CompileError c : min.getErrors()) {
                fc.getErrorHandler().sendError(c);
            }

            return match3.stream()
                .map(ArgsMatchResult::getSig)
                .collect(ImmutableList.toImmutableList());
        }
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
