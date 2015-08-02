package de.peeeq.eclipsewurstplugin.editor;

import static de.peeeq.eclipsewurstplugin.WurstConstants.DEFAULT_MATCHING_BRACKETS_COLOR;
import static de.peeeq.eclipsewurstplugin.WurstConstants.EDITOR_MATCHING_BRACKETS;
import static de.peeeq.eclipsewurstplugin.WurstConstants.EDITOR_MATCHING_BRACKETS_COLOR;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.DefaultCharacterPairMatcher;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelExtension;
import org.eclipse.jface.text.source.ICharacterPairMatcher;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.projection.ProjectionAnnotation;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;
import org.eclipse.jface.text.source.projection.ProjectionSupport;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.jface.viewers.IPostSelectionProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPersistableEditor;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.SourceViewerDecorationSupport;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

import com.google.common.collect.Sets;

import de.peeeq.eclipsewurstplugin.WurstOccurencesMarker;
import de.peeeq.eclipsewurstplugin.builder.ModelManager;
import de.peeeq.eclipsewurstplugin.builder.ModelManagerStub;
import de.peeeq.eclipsewurstplugin.builder.WurstNature;
import de.peeeq.eclipsewurstplugin.editor.outline.WurstContentOutlinePage;
import de.peeeq.eclipsewurstplugin.editor.reconciling.WPosition;
import de.peeeq.eclipsewurstplugin.editor.reconciling.WurstReconcilingStategy;
import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.FuncRef;
import de.peeeq.wurstscript.ast.NameDef;
import de.peeeq.wurstscript.ast.NameRef;
import de.peeeq.wurstscript.ast.TypeRef;
import de.peeeq.wurstscript.parser.WPos;
import de.peeeq.wurstscript.utils.Utils;

public class WurstEditor extends TextEditor implements IPersistableEditor, CompilationUnitChangeListener, ISelectionChangedListener {
	
	private WurstContentOutlinePage fOutlinePage;
	private ModelManager modelManager = new ModelManagerStub();
	private Set<CompilationUnitChangeListener> changeListeners = Sets.newLinkedHashSet();
	private CompilationUnit compiationUnit;
	private WurstReconcilingStategy reconciler;
	private ProjectionSupport projectionSupport;
	private ProjectionAnnotationModel projectionAnnotationModel;
	private int reconcileCount;
	private IAnnotationModel annotationModel;
	private final WurstOccurencesMarker occurencesMarker = new WurstOccurencesMarker();

	public WurstEditor() {
		super();
		setSourceViewerConfiguration(new WurstEditorConfig(this));
		setDocumentProvider(new WurstDocumentProvider());
		
	}
	
	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		super.init(site, input);
		
        IContextService cs = (IContextService)getSite().getService(IContextService.class);
        cs.activateContext("de.peeeq.eclipsewurstplugin.wursteditorscope");
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

	public void doWithCompilationUnit(Consumer<CompilationUnit> action) { // TODO rename
		getNature().getModelManager().doWithCompilationUnit(getFilename(), action);
	}
	public <T> T doWithCompilationUnitR(Function<CompilationUnit,T> action) { // TODO rename
		return getNature().getModelManager().doWithCompilationUnitR(getFilename(), action);
	}

	public String getFilename() {
		WurstNature nature = getNature();
		IFile file = getFile();
		if (file == null || nature == null) {
			return null;
		}
		return file.getProjectRelativePath().toString();
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
//		WLogger.info("property changed " + propId + ", " + source);
//		if (propId == IEditorPart.PROP_INPUT) {
//			WLogger.info("property changed: PROP_INPUT ");
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
			reconciler.reconcile(false);
		}
		
	    ProjectionViewer viewer =(ProjectionViewer)getSourceViewer();

	    projectionSupport = new ProjectionSupport(viewer,getAnnotationAccess(),getSharedColors());
	    projectionSupport.install();

	    //turn projection mode on
	    viewer.enableProjection();
	    viewer.doOperation(ProjectionViewer.COLLAPSE);

	    projectionAnnotationModel = viewer.getProjectionAnnotationModel();
	    annotationModel = viewer.getAnnotationModel();
	
	}

	@Override
	protected ISourceViewer createSourceViewer(Composite parent, IVerticalRuler ruler, int styles) {
		ISourceViewer viewer = new ProjectionViewer(parent, ruler,
				getOverviewRuler(), isOverviewRulerVisible(), styles);

		// ensure decoration support has been created and configured.
		getSourceViewerDecorationSupport(viewer);

		return viewer;
	}
	
	
	private Map<WPosition, Annotation> oldAnnotations = new HashMap<>();
	private @Nullable ITextSelection currentSelection;

	public void updateFoldingStructure(List<WPosition> positions) {
		Map<Annotation, Position> newAnnotations = new HashMap<>();
		
		Set<Annotation> removed = new HashSet<>(oldAnnotations.values());
		for (WPosition position : positions) {
			Annotation a = oldAnnotations.get(position);
			if (a == null) {
				ProjectionAnnotation annotation = new ProjectionAnnotation();
				newAnnotations.put(annotation, position.toEclipsePosition());
			} else {
				removed.remove(a);
			}
		}
		
		Utils.removeValuesFromMap(oldAnnotations, removed);
		
		for (Entry<Annotation, Position> a : newAnnotations.entrySet()) {
			oldAnnotations.put(new WPosition(a.getValue()), a.getKey());
		}
		
		projectionAnnotationModel.modifyAnnotations(removed.toArray(new Annotation[0]), newAnnotations, null);
		ProjectionViewer viewer = (ProjectionViewer) getSourceViewer();
		if (reconcileCount < 2 && viewer != null) {
			viewer.doOperation(ProjectionViewer.COLLAPSE_ALL);
		}
		reconcileCount++;

	}
	
	
	@Override
	public void onCompilationUnitChanged(CompilationUnit newCu) {
		this.compiationUnit = newCu;
		for (CompilationUnitChangeListener cl : changeListeners) {
			cl.onCompilationUnitChanged(newCu);
		}
		updateMarkOccurences();
	}


	public void setReconciler(WurstReconcilingStategy reconciler) {
		this.reconciler = reconciler;
	}


	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		ISelection selection = event.getSelection();
		if (selection instanceof ITextSelection) {
			ITextSelection ts = (ITextSelection) selection;
			currentSelection = ts;
			if (fOutlinePage != null) {
				fOutlinePage.selectNodeByPos(ts.getOffset());
			}
			updateMarkOccurences();
		}
		
	}


	private void updateMarkOccurences() {
		occurencesMarker.update(currentSelection, annotationModel, getFilename(), modelManager);
	}

	

	public void reconcile(boolean doTypecheck) {
		reconciler.reconcile(doTypecheck);
	}
	
	public void reconcileIfNecessary(int lastdocumentHash, boolean doTypecheck) {
		if (getLastReconcileDocumentHashcode() != lastdocumentHash) {
			reconcile(doTypecheck);
		}
	}
	

	public static WurstEditor getActiveEditor() {
		IWorkbenchWindow wb = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (wb == null) {
			WLogger.info("not called from UI thread?");
			return null;
		}
		IWorkbenchPage activePage = wb.getActivePage();
		if (activePage == null) {
			WLogger.info("no active page");
			return null;
		}
		IEditorPart editor = activePage.getActiveEditor();
		if (editor instanceof WurstEditor) {
			return (WurstEditor) editor;
		}
		for (IEditorReference r : activePage.getEditorReferences()) {
			editor = r.getEditor(false);
			if (editor instanceof WurstEditor) {
				return (WurstEditor) editor;
			}
		}
		return null;
	}
	
	@Override
	protected void doSetInput(IEditorInput input) throws CoreException {
		setDocumentProvider(createDocumentProvider(input));
		super.doSetInput(input);
	}
	
	public void refresh() {
		try {
			// TODO is there a nicer solution to refresh? :D
			doSetInput(getEditorInput());
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	private IDocumentProvider createDocumentProvider(IEditorInput input) {
		if (input instanceof IStorageEditorInput){
			return new WurstDocumentProvider();
		} else if(input instanceof IFileEditorInput
				|| input instanceof FileStoreEditorInput) {
			return new WurstTextDocumentProvider();
		}
		throw new Error("Got IEditorInput of type " + input.getClass());
		
	}

	public int getLastReconcileDocumentHashcode() {
		return reconciler.getLastReconcileDocumentHashcode();
	}

	
	
}
