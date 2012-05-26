package de.peeeq.eclipsewurstplugin.editor;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;

import de.peeeq.eclipsewurstplugin.builder.WurstNature;
import de.peeeq.wurstscript.ast.AstElementWithSource;

public class WurstHyperlinik implements IHyperlink {

	private IProject project;
	private AstElementWithSource decl;
	private int startOffset;
	private int endOffset;

	public WurstHyperlinik(IProject project, AstElementWithSource decl, int start, int end) {
		this.project = project;
		this.decl = decl;
		this.startOffset = start;
		this.endOffset = end;
	}

	@Override
	public IRegion getHyperlinkRegion() {
		return new Region(startOffset, endOffset+1-startOffset);
	}

	@Override
	public String getTypeLabel() {
		return null;
	}

	@Override
	public String getHyperlinkText() {
		return null;
	}

	@Override
	public void open() {
		WurstNature.open(project, decl.getSource().getFile(), decl.getSource().getLeftPos());

	}

}
