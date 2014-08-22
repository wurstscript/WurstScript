package de.peeeq.wurstscript.attributes;

import org.eclipse.jdt.annotation.Nullable;

import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.ast.AstElementWithSource;
import de.peeeq.wurstscript.ast.ClassDef;
import de.peeeq.wurstscript.ast.ConstructorDef;
import de.peeeq.wurstscript.ast.Expr;
import de.peeeq.wurstscript.ast.ExprMember;
import de.peeeq.wurstscript.ast.ExprMemberMethod;
import de.peeeq.wurstscript.ast.ExtensionFuncDef;
import de.peeeq.wurstscript.ast.FuncDef;
import de.peeeq.wurstscript.ast.InitBlock;
import de.peeeq.wurstscript.ast.LocalVarDef;
import de.peeeq.wurstscript.ast.LoopStatement;
import de.peeeq.wurstscript.ast.OnDestroyDef;
import de.peeeq.wurstscript.ast.StmtIf;
import de.peeeq.wurstscript.ast.StmtLoop;
import de.peeeq.wurstscript.ast.StmtWhile;
import de.peeeq.wurstscript.ast.StructureDef;
import de.peeeq.wurstscript.ast.SwitchStmt;
import de.peeeq.wurstscript.ast.WImport;
import de.peeeq.wurstscript.ast.WImports;
import de.peeeq.wurstscript.ast.WPackage;
import de.peeeq.wurstscript.parser.WPos;
import de.peeeq.wurstscript.utils.LineOffsets;

public class AttrPos {

	/**
	 * makes a best effort to get a precise position for this element
	 * @param e
	 * @return
	 */
	public static WPos getPos(AstElement e) {
		if (e instanceof AstElementWithSource) {
			AstElementWithSource ws = (AstElementWithSource) e;
			return ws.getSource();
		}
		if (e.size() > 0) { // try to find the position by examining the childs
			int min = Integer.MAX_VALUE;
			int max = Integer.MIN_VALUE;
			for (int i = 0; i < e.size(); i++) {
				AstElement child = e.get(i);
				WPos childSource = child.attrSource();
				min = Math.min(min, childSource.getLeftPos());
				max = Math.max(max, childSource.getRightPos());
			}
			return new WPos(e.get(0).attrSource().getFile(), e.get(0).attrSource().getLineOffsets(), min, max);
		}
		// if no childs exist, search a parent element with a explicit position
		return getParentSource(e);
	}

	public static WPos getPos(WImports e) {
		int min = Integer.MAX_VALUE;
		int max = Integer.MIN_VALUE;
		for (WImport i : e) {
			if (i.getPackagename().equals("Wurst")) {
				continue;
			}
			WPos childSource = i.getSource();
			min = Math.min(min, childSource.getLeftPos());
			max = Math.max(max, childSource.getRightPos());
		}
		if (min != Integer.MAX_VALUE) {
			return new WPos(e.get(0).attrSource().getFile(), e.get(0).attrSource().getLineOffsets(), min, max);
		} else {
			return getParentSource(e);
		}
	}
	

	private static WPos getParentSource(AstElement e) {
		AstElement parent = e.getParent();
		while (parent != null) {
			if (parent instanceof AstElementWithSource) {
				WPos parentSource = ((AstElementWithSource) parent).getSource();
				// use parent position but with size -1, so we do not go into this
				return new WPos(parentSource.getFile(), parentSource.getLineOffsets(), parentSource.getLeftPos(), parentSource.getLeftPos()-1);
			}
			parent = parent.getParent();
		} 
		return new WPos("<source of " + e + " not found>", new LineOffsets(), 0, 0);
	}
	
	
	public static int getColumn(WPos p) {
		LineOffsets lineOffsets = getLineOffsets(p);
		if (lineOffsets == null) return 0;
		return p.getLeftPos() - lineOffsets.get(p.getLine() - 1);
	}

	private static @Nullable LineOffsets getLineOffsets(WPos p) {
		LineOffsets lineOffsets;
		if (p.getLineOffsets() instanceof LineOffsets) {
			lineOffsets = (LineOffsets) p.getLineOffsets();
		} else {
			lineOffsets = new LineOffsets();
		}
		return lineOffsets;
	}
	
	public static int getLine(WPos p) {
		LineOffsets lineOffsets = getLineOffsets(p);
		if (lineOffsets == null) return 0;
		return lineOffsets.getLine(p.getLeftPos()) + 1;
	}
	
	public static int getEndColumn(WPos p) {
		LineOffsets lineOffsets = getLineOffsets(p);
		if (lineOffsets == null) return 0;
		return p.getRightPos() - lineOffsets.get(p.getEndLine() - 1);
	}
	
	public static int getEndLine(WPos p) {
		LineOffsets lineOffsets = getLineOffsets(p);
		if (lineOffsets == null) return 0;
		return lineOffsets.getLine(p.getRightPos()) + 1;
	}


	public static WPos getErrorPos(AstElement e) {
		return e.attrSource();
	}
	
	public static WPos getErrorPos(WPackage e) {
		WPos pos = e.getSource();
		return pos.withRightPos(pos.getLeftPos() + ("package " + e.getName()).length());
	}
	
	public static WPos getErrorPos(FuncDef e) {
		WPos pos = e.getSource();
		int start;
		if (e.getModifiers().isEmpty()) {
			start = pos.getLeftPos();
		} else {
			start = e.getModifiers().attrSource().getRightPos() + 1;
		}
		start += "function ".length();
		return pos
				.withLeftPos(start)
				.withRightPos(start + e.getName().length());
	}
	
	public static WPos getErrorPos(ExtensionFuncDef e) {
		WPos pos = e.getSource();
		return pos.withRightPos(e.getExtendedType().getSource().getRightPos() + (".function " + e.getName()).length());
	}
	
	public static WPos getErrorPos(ClassDef e) {
		WPos pos = e.getSource();
		return pos.withRightPos(pos.getLeftPos() + ("class " + e.getName()).length());
	}
	
	public static WPos getErrorPos(ConstructorDef e) {
		WPos pos = e.getSource();
		return pos.withRightPos(pos.getLeftPos() + ("construct").length());
	}
	
	public static WPos getErrorPos(InitBlock e) {
		WPos pos = e.getSource();
		return pos.withRightPos(pos.getLeftPos() + ("init").length());
	}
	
	public static WPos getErrorPos(OnDestroyDef e) {
		WPos pos = e.getSource();
		return pos.withRightPos(pos.getLeftPos() + ("ondestroy").length());
	}
	
	public static WPos getErrorPos(StructureDef e) {
		WPos pos = e.getSource();
		return pos.withRightPos(pos.getLeftPos() + 5 + e.getName().length());
	}
	
	
	public static WPos getErrorPos(LoopStatement e) {
		WPos pos = e.getSource();
		return pos.withRightPos(pos.getLeftPos() + 3);
	}
	
	public static WPos getErrorPos(StmtWhile e) {
		WPos pos = e.getSource();
		return pos.withRightPos(pos.getLeftPos() + 5);
	}
	
	public static WPos getErrorPos(StmtLoop e) {
		WPos pos = e.getSource();
		return pos.withRightPos(pos.getLeftPos() + 5);
	}
	
	public static WPos getErrorPos(StmtIf e) {
		WPos pos = e.getSource();
		return pos.withRightPos(pos.getLeftPos() + 2);
	}
	
	public static WPos getErrorPos(SwitchStmt e) {
		WPos pos = e.getSource();
		return pos.withRightPos(pos.getLeftPos() + 6);
	}
	
	
	public static WPos getErrorPos(ExprMember e) {
		WPos pos = e.getSource();
		pos = pos.withLeftPos(e.getLeft().attrSource().getRightPos());
		return pos;
	}
	
	public static WPos getErrorPos(ExprMemberMethod e) {
		WPos pos = e.getSource();
		pos = pos.withLeftPos(e.getLeft().attrSource().getRightPos() + 1);
		if (!e.getArgs().isEmpty()) {
			pos = pos.withRightPos(e.getArgs().attrSource().getLeftPos() - 1);
		} else {
			pos = pos.withRightPos(pos.getRightPos() - 2);
		}
		return pos;
	}
	
	
	public static WPos getErrorPos(LocalVarDef e) {
		WPos pos = e.getSource();
		if (e.getInitialExpr() instanceof Expr) {
			pos = pos.withRightPos(e.getInitialExpr().attrSource().getLeftPos() - 3);
		}
		return pos;
	}
	
	
}
