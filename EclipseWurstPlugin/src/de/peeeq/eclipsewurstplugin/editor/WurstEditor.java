package de.peeeq.eclipsewurstplugin.editor;

import static de.peeeq.eclipsewurstplugin.WurstConstants.DEFAULT_MATCHING_BRACKETS_COLOR;
import static de.peeeq.eclipsewurstplugin.WurstConstants.EDITOR_MATCHING_BRACKETS;
import static de.peeeq.eclipsewurstplugin.WurstConstants.EDITOR_MATCHING_BRACKETS_COLOR;

import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.source.DefaultCharacterPairMatcher;
import org.eclipse.jface.text.source.ICharacterPairMatcher;
import org.eclipse.jface.viewers.IPostSelectionProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPersistableEditor;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.texteditor.SourceViewerDecorationSupport;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

import com.google.common.collect.Sets;

import de.peeeq.eclipsewurstplugin.builder.ModelManager;
import de.peeeq.eclipsewurstplugin.builder.ModelManagerStub;
import de.peeeq.eclipsewurstplugin.builder.WurstNature;
import de.peeeq.eclipsewurstplugin.editor.outline.WurstContentOutlinePage;
import de.peeeq.eclipsewurstplugin.editor.reconciling.WurstReconcilingStategy;
import de.peeeq.wurstscript.ast.CompilationUnit;

public class WurstEditor extends TextEditor implements IPersistableEditor, CompilationUnitChangeListener, ISelectionChangedListener {
	
	private WurstContentOutlinePage fOutlinePage;
	private ModelManager modelManager = new ModelManagerStub();
	private Set<CompilationUnitChangeListener> changeListeners = Sets.newHashSet();
	private CompilationUnit compiationUnit;
	private WurstReconcilingStategy reconciler;

	public WurstEditor() {
		super();
		setSourceViewerConfiguration(new WurstEditorConfig(this));
		setDocumentProvider(new WurstDocumentProvider());
		
	}
	
	
	@Override
	protected void configureSourceViewerDecorationSupport (SourceViewerDecorationSupport support) {
		super.configureSourceViewerDecorationSupport(support);
		IPreferenceStore store = getPreferenceStore();
		char[] matchChars = {'(', ')', '[', ']', '{', '}'}; //which brackets to match
		ICharacterPairMatcher matcher = new DefaultCharacterPairMatcher(matchChars ,
				IDocumentExtension3.DEFAULT_PARTITIONING);
		support.setCharacterPairMatcher(matcher);
		support.setMatchingCharacterPainterPreferenceKeys(EDITOR_MATCHING_BRACKETS, EDITOR_MATCHING_BRACKETS_COLOR);
		//Enable bracket highlighting in the preference store
		store.setDefault(EDITOR_MATCHING_BRACKETS, true);
		store.setDefault(EDITOR_MATCHING_BRACKETS_COLOR, DEFAULT_MATCHING_BRACKETS_COLOR);
	}
	
	@Override
	public Object getAdapter(Class adapter) {
		if (IContentOutlinePage.class.equals(adapter)) {
			if (fOutlinePage == null) {
				fOutlinePage= new WurstContentOutlinePage(getDocumentProvider(), this);
				if (getEditorInput() != null)
					fOutlinePage.setInput(getEditorInput());
			}
			return fOutlinePage;
		}
		
		return super.getAdapter(adapter);
	}

	public CompilationUnit getCompilationUnit() {
		WurstNature nature = getNature();
		IFile file = getFile();
		if (file != null && nature != null) {
			String fileName = file.getProjectRelativePath().toString();
			CompilationUnit cu = nature.getModelManager().getCompilationUnit(fileName);
			return cu;
		}
		return null;
	}

	public IFile getFile() {
		if (getEditorInput() instanceof IFileEditorInput) {
			IFileEditorInput fileEditorInput = (IFileEditorInput) getEditorInput();
			return fileEditorInput.getFile();
		}
		return null;
	}
	
	public IProject getProject() {
		IFile file = getFile();
		if (file != null) {
			return file.getProject();
		}
		return null;
	}
	
	private WurstNature getNature() {
		IProject project = getProject();
		return WurstNature.get(project);
	}

	public ModelManager getModelManager() {
		return modelManager;
	}
	
	public void registerCompilationUnitChangeListener(CompilationUnitChangeListener listener) {
		this.changeListeners.add(listener);		
	}


//	@Override
//	public void propertyChanged(Object source, int propId) {
//		System.out.println("property changed " + propId + ", " + source);
//		if (propId == IEditorPart.PROP_INPUT) {
//			System.out.println("property changed: PROP_INPUT ");
//			WurstNature nature = getNature();
//			if (nature != null) {
//				modelManager = nature.getModelManager();
//				modelManager.registerChangeListener(getFile().getProjectRelativePath().toString(), this);
//			}
//		}
//		
//	}

	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		WurstNature nature = getNature();
		if (nature != null) {
			modelManager = nature.getModelManager();
			modelManager.registerChangeListener(getFile().getProjectRelativePath().toString(), this);
		}
		
		IPostSelectionProvider sp = (IPostSelectionProvider) this.getSelectionProvider();
		sp.addPostSelectionChangedListener(this);
		
		
		if (reconciler != null) {
			reconciler.reconcile();
		}
	
	}


	
	
	
	@Override
	public void onCompilationUnitChanged(CompilationUnit newCu) {
		this.compiationUnit = newCu;
		for (CompilationUnitChangeListener cl : changeListeners) {
			cl.onCompilationUnitChanged(newCu);
		}
	}


	public void setReconciler(WurstReconcilingStategy reconciler) {
		this.reconciler = reconciler;
	}


	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		ISelection selection = event.getSelection();
		if (selection instanceof ITextSelection) {
			ITextSelection ts = (ITextSelection) selection;
			if (fOutlinePage != null) {
				fOutlinePage.selectNodeByPos(ts.getOffset());
			}
		}
		
	}


	public CompilationUnit reconcile() {
		return reconciler.reconcile();
	}
	
	
}
