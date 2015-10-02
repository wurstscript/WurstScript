package de.peeeq.eclipsewurstplugin.editor.reconciling;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension;
import org.eclipse.swt.widgets.Display;

import com.google.common.collect.ImmutableList;

import de.peeeq.eclipsewurstplugin.builder.ModelManager;
import de.peeeq.eclipsewurstplugin.editor.WurstEditor;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.WPackage;
import de.peeeq.wurstscript.gui.WurstGui;
import de.peeeq.wurstscript.gui.WurstGuiLogger;
import de.peeeq.wurstscript.parser.WPos;

public class WurstReconcilingStategy implements IReconcilingStrategy, IReconcilingStrategyExtension {

	private WurstEditor editor;
	private IDocument document;
	private int lastReconcileDocumentHashcode = -1;

	public WurstReconcilingStategy(WurstEditor editor) {
		this.editor = editor;
		editor.setReconciler(this);
		
	}
	
	@Override
	public void setDocument(IDocument document) {
		this.document = document;
		
	}

	@Override
	public void reconcile(DirtyRegion dirtyRegion, IRegion subRegion) {
		reconcile(true);
	}

	@Override
	public void reconcile(IRegion partition) {
		reconcile(true);
	}

	public void reconcile(boolean doTypecheck) {
		ModelManager mm = editor.getModelManager();
		WurstGui gui = new WurstGuiLogger();
		IFile file = editor.getFile();
		if (file != null) {
			// TODO handle parser-error markers
			
			String doc = document.get();
			lastReconcileDocumentHashcode = doc.hashCode();
			String fileName = file.getProjectRelativePath().toString();
			mm.parse(gui, fileName, new StringReader(doc));
			if (gui.getErrorCount() > 0) {
				// errors after parse, abort
				return;
			}
			if (doTypecheck) {
				// check only the updated compilation unit
				mm.typeCheckModelPartial(gui, true, ImmutableList.of(fileName));
			}
			
			mm.doWithCompilationUnit(fileName, cu -> codeFolding(cu));
		}
	}

	/**
	 * calculates the points for doing code folding 
	 */
	private void codeFolding(CompilationUnit cu) {
		final List<WPosition> positions = new ArrayList<>();
		for (WPackage p : cu.getPackages()) {
			if (p.getImports().size() <= 2) {
				// do not fold when there is only one import (and the implicit Wurst import)
				continue;
			}
			WPos importsPos = p.getImports().attrSource();
			int start = importsPos.getLeftPos();
			int length = importsPos.getRightPos() - start;
			if (length > 0) {
				positions.add(new WPosition(start, length));
			}
		}
		
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				editor.updateFoldingStructure(positions);
			}

		});
	}

	public int getLastReconcileDocumentHashcode() {
		return lastReconcileDocumentHashcode;
	}

	@Override
	public void setProgressMonitor(IProgressMonitor monitor) {
	}

	@Override
	public void initialReconcile() {
		reconcile(true);
	}


}
