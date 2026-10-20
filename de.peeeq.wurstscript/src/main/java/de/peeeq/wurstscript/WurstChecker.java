package de.peeeq.wurstscript;

import com.google.common.base.Preconditions;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.WurstModel;
import de.peeeq.wurstscript.attributes.ErrorHandler;
import de.peeeq.wurstscript.attributes.names.DesugarArrayLength;
import de.peeeq.wurstscript.gui.WurstGui;
import de.peeeq.wurstscript.validation.GlobalCaches;
import de.peeeq.wurstscript.validation.WurstValidator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class WurstChecker {

    private final WurstGui gui;
    private final ErrorHandler errorHandler;
    private final boolean legacyJassTypeChecks;

    public WurstChecker(WurstGui gui, ErrorHandler errorHandler) {
        this(gui, errorHandler, false);
    }

    public WurstChecker(WurstGui gui, ErrorHandler errorHandler, boolean legacyJassTypeChecks) {
        this.gui = gui;
        this.errorHandler = errorHandler;
        this.legacyJassTypeChecks = legacyJassTypeChecks;
    }

    /**
     * Checks the given compilation units of the model.
     *
     * @return whether the units were validated. Errors found before validation stop the check, and
     * then nothing is known about the units.
     */
    public boolean checkProg(WurstModel root, Collection<CompilationUnit> toCheck) {
        return checkProg(root, toCheck, root);
    }

    public boolean checkProg(WurstModel root, Collection<CompilationUnit> toCheck,
                             Collection<CompilationUnit> compilationUnits) {
        Preconditions.checkNotNull(root);
        Preconditions.checkNotNull(toCheck);
        if (root.isEmpty()) {
            return true;
        }
        DesugarArrayLength desugar = new DesugarArrayLength();
        compilationUnits.forEach(cu -> cu.accept(desugar));
        gui.sendProgress("Checking Files");

        if (errorHandler.getErrorCount() > 0) return false;

        attachErrorHandler(root);
        clearGlobalCaches(root, toCheck);

        compilationUnits.forEach(ModuleExpander::expandModules);

        if (errorHandler.getErrorCount() > 0) return false;

        SyntacticSugar syntacticSugar = new SyntacticSugar();
        List<SyntacticSugar.DeferredModuleCall> detachedTemplates = new ArrayList<>();
        for (CompilationUnit cu : toCheck) {
            syntacticSugar.expandFieldIterations(cu);
            detachedTemplates.addAll(syntacticSugar.detachModuleTemplateFieldIterations(cu));
        }
        try {
            // compute the flow attributes
            for (CompilationUnit cu : toCheck) {
                WurstValidator.computeFlowAttributes(cu);
            }

            // validate the resource:
            WurstValidator validator = new WurstValidator(root, legacyJassTypeChecks, compilationUnits);
            validator.validate(toCheck);
        } finally {
            syntacticSugar.restoreModuleTemplateFieldIterations(detachedTemplates);
        }
        return true;
    }

    /**
     * Lets a model which was checked completely by another checker report to this one, so that errors
     * found while translating it reach this checker's gui.
     */
    public void adoptCheckedModel(WurstModel root) {
        attachErrorHandler(root);
    }

    public void adoptCheckedModel(WurstModel root, Collection<CompilationUnit> compilationUnits) {
        attachErrorHandler(root);
        new WurstValidator(root, legacyJassTypeChecks, compilationUnits).prepareNamePreservation();
    }

    private void clearGlobalCaches(WurstModel root, Collection<CompilationUnit> toCheck) {
        if (toCheck == root || toCheck.size() >= root.size()) {
            GlobalCaches.clearAll();
        } else {
            GlobalCaches.clearLookupCacheFor(toCheck);
        }
    }

    private void attachErrorHandler(WurstModel root) {
        for (CompilationUnit cu : root) {
            cu.getCuInfo().setCuErrorHandler(errorHandler);
        }
    }

}
