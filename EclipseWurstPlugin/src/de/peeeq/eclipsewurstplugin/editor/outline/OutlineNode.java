package de.peeeq.eclipsewurstplugin.editor.outline;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.swt.graphics.Image;

import com.google.common.collect.Lists;

import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.ast.AstElementWithName;
import de.peeeq.wurstscript.ast.AstElementWithNameId;
import de.peeeq.wurstscript.ast.ClassDef;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.ConstructorDef;
import de.peeeq.wurstscript.ast.ExtensionFuncDef;
import de.peeeq.wurstscript.ast.FunctionDefinition;
import de.peeeq.wurstscript.ast.InitBlock;
import de.peeeq.wurstscript.ast.InterfaceDef;
import de.peeeq.wurstscript.ast.StructureDef;
import de.peeeq.wurstscript.ast.TypeExpr;
import de.peeeq.wurstscript.ast.TypeExprArray;
import de.peeeq.wurstscript.ast.TypeExprSimple;
import de.peeeq.wurstscript.ast.VarDef;
import de.peeeq.wurstscript.ast.WEntity;
import de.peeeq.wurstscript.ast.WImport;
import de.peeeq.wurstscript.ast.WImports;
import de.peeeq.wurstscript.ast.WPackage;

public class OutlineNode {

	private static final OutlineNode[] EMPTY = new OutlineNode[0];
	private AstElement node;
	private OutlineNode parent;
	private OutlineNode[] children;
	private static final Comparator<OutlineNode> sourcePosComperator = new Comparator<OutlineNode>() {
		
		@Override
		public int compare(OutlineNode a, OutlineNode b) {
			int astart = a.node.attrSource().getLeftPos();
			int bstart = b.node.attrSource().getLeftPos();
			return astart - bstart;
		}
	};
	
	
	public OutlineNode(AstElement node, OutlineNode parent) {
		this.node = node;
		this.parent = parent;
	}

	public OutlineNode getParent() {
		return parent;
	}


	public AstElement getNode() {
		return node;
	}

	public OutlineNode[] getChildren() {
		if (children == null) {
			if (node instanceof CompilationUnit) {
				CompilationUnit cu = (CompilationUnit) node;
				children = sorted(makeChilds(cu.getJassDecls(), cu.getPackages()));
			} else if (node instanceof WPackage) {
				WPackage p = (WPackage) node;
				List<OutlineNode> r = Lists.newArrayList();
				r.add(childNode(p.getImports()));
				for (WEntity e : p.getElements()) {
					r.add(childNode(e));
				}
				return toArray(r);
			} else if (node instanceof WImports) {
				WImports wImports = (WImports) node;
				return newChildrenArray(wImports);
			} else if (node instanceof StructureDef) {
				StructureDef s = (StructureDef) node;
				children = sorted(makeChilds(s.getConstructors(), s.getMethods(),
						s.getModuleUses(), s.getVars()));
				// TODO init block, ondestroy etc.
			} else if (
				node instanceof WImports
				) {
				children = newChildrenArray(node);
			} else {
				children = EMPTY;
			}
		}
		return children;
	}

	private OutlineNode[] sorted(OutlineNode[] r) {
		Arrays.sort(r, sourcePosComperator);
		return r;
	}

	private OutlineNode[] makeChilds(AstElement ... elems) {
		List<OutlineNode> r = Lists.newArrayList();
		for (AstElement elem : elems) {
			for (int i=0; i<elem.size(); i++) {
				r.add(childNode(elem.get(i)));
			}
		}
		return toArray(r);
	}

	private OutlineNode[] toArray(List<OutlineNode> r) {
		return r.toArray(new OutlineNode[r.size()]);
	}

	private OutlineNode childNode(AstElement childNode) {
		return new OutlineNode(childNode, this);
	}

	private OutlineNode[] sorted(List<OutlineNode> r) {
		Collections.sort(r, sourcePosComperator);
		return toArray(r);
	}

	private OutlineNode[] newChildrenArray(AstElement e) {
		OutlineNode[] result = new OutlineNode[e.size()];
		for (int i=0; i<e.size(); i++) {
			result[i] = new OutlineNode(e.get(i), this);
		}
		return result;
	}

	@Override
	public String toString() {
		if (node instanceof ExtensionFuncDef) {
			ExtensionFuncDef extensionFuncDef = (ExtensionFuncDef) node;
			return printTypeExpr(extensionFuncDef.getExtendedType()) + "." + extensionFuncDef.getName(); 
		} else if (node instanceof AstElementWithNameId) {
			return ((AstElementWithNameId) node).getNameId().getName();
		} else if (node instanceof WImports) {
			return "imports";
		} else if (node instanceof InitBlock) {
			return "init";
		} else if (node instanceof WImport) {
			WImport wImport = (WImport) node;
			return "import " + wImport.getPackagename();
		} else if (node instanceof ConstructorDef) {
			return "constructor";
		}
		return node.getClass().getSimpleName();
	}

	private String printTypeExpr(TypeExpr t) {
		if (t instanceof TypeExprSimple) {
			return ((TypeExprSimple) t).getTypeName();
		} else if (t instanceof TypeExprArray) {
			TypeExprArray a = (TypeExprArray) t;
			return printTypeExpr(a.getBase()) + " array"; 
		} else {
			return "...";
		}
	}

	public Image getImage() {
		if (node instanceof WPackage) {
			return Icons.wpackage;
		} else if (node instanceof ClassDef) {
			return Icons.wclass;
		} else if (node instanceof InterfaceDef) {
			return Icons.winterface;
		} else if (node instanceof WImports) {
			return Icons.wimports;
		} else if (node instanceof WImport) {
			return Icons.wimport;
		} else if (node instanceof FunctionDefinition) { 
			return Icons.function;
		} else if (node instanceof VarDef) {
			return Icons.var;
		}
		return Icons.block;
	}
	
	// TODO custom equals method
	@Override
	public boolean equals(Object other) {
		if (other == null) return false;
		if (this == other) return true;
		if (other instanceof OutlineNode) {
			OutlineNode otherN = (OutlineNode) other;
			return node.equals(otherN.node);
		}
		return false;
	}
	
	public int hashCode() {
		return node.hashCode();
	}
	
}
