package de.peeeq.eclipsewurstplugin.editor;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;

import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.ast.AstElementWithSource;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.ExprVarAccess;
import de.peeeq.wurstscript.ast.FuncRef;
import de.peeeq.wurstscript.ast.FunctionDefinition;
import de.peeeq.wurstscript.ast.NameDef;
import de.peeeq.wurstscript.ast.NameRef;
import de.peeeq.wurstscript.ast.TypeDef;
import de.peeeq.wurstscript.ast.TypeExpr;
import de.peeeq.wurstscript.ast.TypeExprSimple;
import de.peeeq.wurstscript.ast.WImport;
import de.peeeq.wurstscript.ast.WPackage;
import de.peeeq.wurstscript.utils.Utils;

public class WurstHylerlinkDetector implements IHyperlinkDetector {

	private static final IHyperlink[] NONE = new IHyperlink[] {};
	private WurstEditor editor;

	public WurstHylerlinkDetector(WurstEditor editor) {
		this.editor = editor;
	}

	@Override
	public IHyperlink[] detectHyperlinks(ITextViewer textViewer, IRegion region, boolean canShowMultipleHyperlinks) {
		CompilationUnit cu = editor.getCompilationUnit();
		if (cu != null) {
			AstElement e = Utils.getAstElementAtPos(cu, region.getOffset());
			System.out.println("hover: " + e.getClass().getSimpleName());
			if (e instanceof FuncRef) {
				FuncRef funcRef = (FuncRef) e;
				FunctionDefinition decl = funcRef.attrFuncDef();
				return linkTo(decl, e.attrSource().getLeftPos(), e.attrSource().getRightPos());
			} else if (e instanceof NameRef) {
				NameRef nameRef = (NameRef) e;
				NameDef decl = nameRef.attrNameDef();
				return linkTo(decl, e.attrSource().getLeftPos(), e.attrSource().getRightPos()-1);
			} else if (e instanceof TypeExpr) {
				TypeExpr typeExpr = (TypeExpr) e;
				TypeDef decl = typeExpr.attrTypeDef();
				return linkTo(decl, e.attrSource().getLeftPos(), e.attrSource().getRightPos()-1);
			} else if (e instanceof WImport) {
				WImport wImport = (WImport) e;
				WPackage p = wImport.attrImportedPackage();
				return linkTo(p, e.attrSource().getLeftPos(), e.attrSource().getRightPos()-1);
			}
		}
		return null;
	}

	private IHyperlink[] linkTo(AstElementWithSource decl, int start, int end) {
		if (decl == null) return null;
		return new IHyperlink[] {
			new WurstHyperlinik(editor.getProject(), decl, start, end)
		};
	}

}
