package de.peeeq.wurstscript.translation.imoptimizer;

import com.google.common.collect.Sets;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.ast.GlobalVarDef;
import de.peeeq.wurstscript.ast.InitBlock;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.WImport;
import de.peeeq.wurstscript.ast.WEntity;
import de.peeeq.wurstscript.ast.WPackage;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.translation.imtranslation.ImHelper;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;
import de.peeeq.wurstscript.utils.Utils;
import de.peeeq.wurstscript.validation.NamePreservation;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class GlobalsInliner implements OptimizerPass {
    private final Set<WPackage> initLaterPackages = Collections.newSetFromMap(new IdentityHashMap<>());
    private boolean initLaterPackagesCollected;

    public int optimize(ImTranslator trans) {
        int obsoleteCount = 0;
        ImProg prog = trans.getImProg();
        prog.clearAttributes(); // TODO only clear read/write attributes

        Set<ImVar> obsoleteVars = Sets.newLinkedHashSet();
        for (final ImVar v : prog.getGlobals()) {
            if (trans.isUnitTestMode() && v.getName().equals("MagicFunctions_compiletime")) {
                // in unit test mode we run tests and compiletime functions with optimizations,
                // so it is important, that we do not optimize away the compiletime constant
                continue;
            }
            if (v.getName().equals("MagicFunctions_isLua") && trans.isLuaTarget()) {
                // In Lua mode, isLua must evaluate to true.
                // Normal inlining would use the declared value (false); override it here.
                for (ImVarRead read : new ArrayList<>(v.attrReads())) {
                    read.replaceBy(JassIm.ImBoolVal(true));
                }
                for (ImVarWrite write : new ArrayList<>(v.attrWrites())) {
                    if (write.getParent() != null) {
                        write.replaceBy(ImHelper.nullExpr());
                    }
                }
                obsoleteVars.add(v);
                obsoleteCount++;
                continue;
            }
            if (v.getType() instanceof ImArrayType
                || v.getType() instanceof ImArrayTypeMulti) {
                // cannot optimize arrays yet
                continue;
            }
            if (NamePreservation.isPreserved(v)) {
                // keep names which are part of the external Warcraft III API
                continue;
            }

            boolean literalConstant = isLiteralConstantGlobal(v.getTrace(), prog);
            if (v.attrWrites().size() == 1 || literalConstant) {
                ImExpr right = null;
                ImVarWrite obs = null;
                for (ImVarWrite write : v.attrWrites()) {
                    ImFunction func = write.getNearestFunc();
                    if (isInInitGlobals(func) || (literalConstant && isLiteral(write.getRight()))) {
                        right = write.getRight();
                        obs = write;
                        break;
                    }
                }
                if (obs == null) {
                    continue;
                }

                ImExpr replacement = findReplacement(right, obs);
                if (replacement != null) {
                    for (ImVarRead v3 : v.attrReads()) {
                        v3.replaceBy(replacement.copy());
                    }
                }
                if ((replacement != null || v.attrReads().size() == 0) && v.attrWrites().size() == 1) {
                    obsoleteVars.add(v);
                }
            } else if (v.attrWrites().size() > 1 && !(v.getType() instanceof ImTupleType)) {
                List<ImVarWrite> initWrites = new ArrayList<>();
                for (ImVarWrite imVarWrite : v.attrWrites()) {
                    ImFunction nearestFunc = imVarWrite.getNearestFunc();
                    if (isInInitGlobals(nearestFunc)) {
                        initWrites.add(imVarWrite);
                    }
                }
                if (initWrites.size() == 1) {
                    if(v.getType() instanceof ImSimpleType) {
                        ImVarWrite initWrite = initWrites.get(0);
                        ImExpr write = initWrite.getRight();
                        try {
                            ImExpr defaultValue = ImHelper.defaultValueForType((ImSimpleType) v.getType());
                            boolean isDefault = defaultValue.structuralEquals(write);
                            if (isDefault) {
                                // Only remove the init write when it assigns the default value.
                                // Never touch non-init writes here.
                                initWrite.replaceBy(ImHelper.nullExpr());
                            }
                        } catch (Exception e) {
                            throw new CompileError(write.attrTrace().attrErrorPos(),
                                "Could not inline " + Utils.printElementWithSource(Optional.of(v.getTrace())),
                                CompileError.ErrorType.ERROR, e);
                        }
                    }
                }
            }

        }
        obsoleteCount += obsoleteVars.size();
        for (ImVar i : obsoleteVars) {
            // remove the write
            if (i.attrWrites().size() > 0) {
                ImVarWrite write = Utils.getFirstAndOnly(i.attrWrites());
                if (write.getParent() != null) {
                    write.replaceBy(write.getRight().copy());
                }
            }
        }
        prog.getGlobals().removeAll(obsoleteVars);
        return obsoleteCount;
    }

    @Nullable
    private ImExpr findReplacement(ImExpr right, ImVarWrite obs) {
        ImExpr replacement;
        if (right instanceof ImIntVal) {
            ImIntVal val = (ImIntVal) right;
            replacement = (JassIm.ImIntVal(val.getValI()));
            if (obs.getParent() != null)
                obs.replaceBy(ImHelper.nullExpr());
        } else if (right instanceof ImRealVal) {
            ImRealVal val = (ImRealVal) right;
            replacement = (JassIm.ImRealVal(val.getValR()));
            if (obs.getParent() != null)
                obs.replaceBy(ImHelper.nullExpr());
        } else if (right instanceof ImStringVal) {
            ImStringVal val = (ImStringVal) right;
            replacement = (JassIm.ImStringVal(val.getValS()));
            if (obs.getParent() != null)
                obs.replaceBy(ImHelper.nullExpr());
        } else if (right instanceof ImBoolVal) {
            ImBoolVal val = (ImBoolVal) right;
            replacement = (JassIm.ImBoolVal(val.getValB()));
            if (obs.getParent() != null)
                obs.replaceBy(ImHelper.nullExpr());
        } else {
            replacement = null;
        }
        return replacement;
    }

    private static boolean isLiteral(ImExpr expr) {
        return expr instanceof ImIntVal || expr instanceof ImRealVal || expr instanceof ImStringVal || expr instanceof ImBoolVal;
    }

    @Override
    public String getName() {
        return "Globals Inlined";
    }


    private static boolean isInInitGlobals(ImFunction func) {
        return func != null && func.getName().equals("initGlobals");
    }

    /**
     * Package globals are initialized by package init functions, rather than initGlobals.
     * A source-level constant is immutable, but an earlier initializer in the same
     * package may still observe its default value before the constant is assigned.
     * Configurable constants stay runtime globals until configuration resolution owns them.
     */
    private boolean isLiteralConstantGlobal(de.peeeq.wurstscript.ast.Element trace, ImProg prog) {
        if (!(trace instanceof GlobalVarDef)) {
            return false;
        }
        GlobalVarDef global = (GlobalVarDef) trace;
        if (!global.attrIsConstant() || global.hasAnnotation("@configurable")) {
            return false;
        }
        WPackage packageOfGlobal = packageOf(global);
        if (packageOfGlobal == null) {
            return true;
        }
        if (isInitializedLater(packageOfGlobal)) {
            return false;
        }
        for (WEntity entity : packageOfGlobal.getElements()) {
            if (entity instanceof InitBlock
                && entity.attrSource().getLeftPos() < global.attrSource().getLeftPos()) {
                return false;
            }
        }
        for (ImVar other : prog.getGlobals()) {
            if (other.getTrace() instanceof GlobalVarDef
                && packageOf((GlobalVarDef) other.getTrace()) == packageOfGlobal
                && other.getTrace().attrSource().getLeftPos() < global.attrSource().getLeftPos()
                && !other.attrWrites().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Nullable
    private static WPackage packageOf(GlobalVarDef global) {
        de.peeeq.wurstscript.ast.Element element = global;
        while (element != null && !(element instanceof WPackage)) {
            element = element.getParent();
        }
        return (WPackage) element;
    }

    private boolean isInitializedLater(WPackage target) {
        if (!initLaterPackagesCollected) {
            collectInitLaterPackages(target);
            initLaterPackagesCollected = true;
        }
        return initLaterPackages.contains(target);
    }

    private void collectInitLaterPackages(WPackage target) {
        for (CompilationUnit unit : target.getModel()) {
            for (WPackage candidate : unit.getPackages()) {
                for (WImport imported : candidate.getImports()) {
                    if (imported.getIsInitLater() && imported.attrImportedPackage() instanceof WPackage importedPackage) {
                        initLaterPackages.add(importedPackage);
                    }
                }
            }
        }
    }

}
