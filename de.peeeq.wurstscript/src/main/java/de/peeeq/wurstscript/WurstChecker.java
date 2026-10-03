package de.peeeq.wurstscript;

import com.google.common.base.Preconditions;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.WurstModel;
import de.peeeq.wurstscript.attributes.ErrorHandler;
import de.peeeq.wurstscript.attributes.names.DesugarArrayLength;
import de.peeeq.wurstscript.gui.WurstGui;
import de.peeeq.wurstscript.validation.GlobalCaches;
import de.peeeq.wurstscript.validation.WurstValidator;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.eclipse.jdt.annotation.Nullable;

public class WurstChecker {

    private final WurstGui gui;
    private final ErrorHandler errorHandler;
    private final boolean legacyJassTypeChecks;
    private final boolean quickMode;
    private final @Nullable File cacheDir;

    public WurstChecker(WurstGui gui, ErrorHandler errorHandler) {
        this(gui, errorHandler, false, false, null);
    }

    public WurstChecker(WurstGui gui, ErrorHandler errorHandler, boolean legacyJassTypeChecks) {
        this(gui, errorHandler, legacyJassTypeChecks, false, null);
    }

    public WurstChecker(WurstGui gui, ErrorHandler errorHandler, boolean legacyJassTypeChecks, boolean quickMode) {
        this(gui, errorHandler, legacyJassTypeChecks, quickMode, null);
    }

    public WurstChecker(WurstGui gui, ErrorHandler errorHandler, boolean legacyJassTypeChecks, boolean quickMode, @Nullable File cacheDir) {
        this.gui = gui;
        this.errorHandler = errorHandler;
        this.legacyJassTypeChecks = legacyJassTypeChecks;
        this.quickMode = quickMode;
        this.cacheDir = cacheDir;
    }

    public void checkProg(WurstModel root, Collection<CompilationUnit> toCheck) {
        Preconditions.checkNotNull(root);
        Preconditions.checkNotNull(toCheck);
        if (root.isEmpty()) {
            return;
        }
        long t0 = System.currentTimeMillis();
        WurstValidator validator = new WurstValidator(root, legacyJassTypeChecks);
        validator.setQuickMode(quickMode);
        validator.setCacheDir(cacheDir);

        Collection<CompilationUnit> unitsToWalk = validator.getUnitsToWalk(toCheck);
        Collection<CompilationUnit> unitsForCheck = (unitsToWalk != null) ? unitsToWalk : toCheck;

        new DesugarArrayLength().run(root);
        gui.sendProgress("Checking Files");

        if (errorHandler.getErrorCount() > 0) return;

        attachErrorHandler(root);
        clearGlobalCaches(root, unitsForCheck);

        long tModules = System.currentTimeMillis();
        expandModules(root);
        long tExpand = System.currentTimeMillis();

        if (errorHandler.getErrorCount() > 0) return;

        SyntacticSugar syntacticSugar = new SyntacticSugar();
        List<SyntacticSugar.DeferredModuleCall> detachedTemplates = new ArrayList<>();
        for (CompilationUnit cu : toCheck) {
            if (quickMode && cu.getCuInfo().isLibrary()) {
                continue;
            }
            syntacticSugar.expandFieldIterations(cu);
        }
        for (CompilationUnit cu : unitsForCheck) {
            if (quickMode && cu.getCuInfo().isLibrary()) {
                continue;
            }
            detachedTemplates.addAll(syntacticSugar.detachModuleTemplateFieldIterations(cu));
        }
        long tSugar = System.currentTimeMillis();
        try {
            // compute the flow attributes
            if (!quickMode) {
                for (CompilationUnit cu : toCheck) {
                    WurstValidator.computeFlowAttributes(cu);
                }
            }
            long tFlow = System.currentTimeMillis();

            validator.validate(unitsForCheck);
            long tVal = System.currentTimeMillis();
            String checkerTiming = String.format("WurstChecker: total=%dms (expandModules=%dms, sugar=%dms, flow=%dms, validate=%dms)",
                tVal - t0, tExpand - tModules, tSugar - tExpand, tFlow - tSugar, tVal - tFlow);
            de.peeeq.wurstscript.WLogger.info(checkerTiming);
            System.out.println(checkerTiming);
        } finally {
            syntacticSugar.restoreModuleTemplateFieldIterations(detachedTemplates);
        }
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

    private void expandModules(WurstModel root) {
        for (CompilationUnit cu : root) {
            ModuleExpander.expandModules(cu);
        }
    }

}
