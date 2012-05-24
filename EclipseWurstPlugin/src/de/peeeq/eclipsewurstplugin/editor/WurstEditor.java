package de.peeeq.eclipsewurstplugin.editor;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.source.DefaultCharacterPairMatcher;
import org.eclipse.jface.text.source.ICharacterPairMatcher;
import org.eclipse.ui.IPersistableEditor;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.texteditor.SourceViewerDecorationSupport;

import de.peeeq.eclipsewurstplugin.WurstPlugin;

import static de.peeeq.eclipsewurstplugin.WurstPlugin.*;

public class WurstEditor extends TextEditor implements IPersistableEditor {
	
	public WurstEditor() {
		super();
		setSourceViewerConfiguration(new WurstEditorConfig(this));
		setDocumentProvider(new WurstDocumentProvicer());
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
		System.out.println("hl");
	}
}
