package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.ast.ClassDef;
import de.peeeq.wurstscript.ast.ClassOrModule;
import de.peeeq.wurstscript.ast.FunctionImplementation;
import de.peeeq.wurstscript.ast.ModuleDef;
import de.peeeq.wurstscript.ast.ModuleInstanciation;
import de.peeeq.wurstscript.ast.NamedScope;
import de.peeeq.wurstscript.ast.PackageOrGlobal;
import de.peeeq.wurstscript.ast.StructureDef;
import de.peeeq.wurstscript.ast.WScope;
import de.peeeq.wurstscript.utils.Utils;

public class AttrNearest {

	
	public static  PackageOrGlobal nearestPackage(AstElement node) {
		if (node == null) {
			return null;
		}
		if (node instanceof ModuleInstanciation) {
			ModuleInstanciation mi = (ModuleInstanciation) node;
			ModuleDef m = mi.attrModuleOrigin();
			return m.attrNearestPackage();
		}
		if (node instanceof PackageOrGlobal) {
			return (PackageOrGlobal) node;
		}
		if (node.getParent() == null) {
			return null;
		}
		return node.getParent().attrNearestPackage();
	}
	
	public static  ClassDef nearestClassDef(AstElement node) {
		if (node == null) {
			return null;
		}
		if (node instanceof ClassDef) {
			return (ClassDef) node;
		}
		if (node.getParent() == null) {
			return null;
		}
		return node.getParent().attrNearestClassDef();
	}

	
	public static  FunctionImplementation nearestFuncDef(AstElement node) {
		if (node == null) {
			return null;
		}
		if (node instanceof FunctionImplementation) {
			return (FunctionImplementation) node;
		}
		if (node.getParent() == null) {
			return null;
		}
		return node.getParent().attrNearestFuncDef();
	}
	
	public static ClassOrModule nearestClassOrModule(AstElement node) {
		if (node == null) {
			return null;
		}
		if (node instanceof ClassOrModule) {
			return (ClassOrModule) node;
		}
		if (node.getParent() == null) {
			return null;
		}
		return node.getParent().attrNearestClassOrModule();
	}

	public static NamedScope nearestNamedScope(AstElement node) {
		if (node == null) {
			return null;
		}
		if (node instanceof NamedScope) {
			return (NamedScope) node;
		}
		if (node.getParent() == null) {
			return null;
		}
		return node.getParent().attrNearestNamedScope();
	}

	public static WScope nearestScope(AstElement e) {
		if (e instanceof WScope) {
			return (WScope) e;
		}
		if (e.getParent() == null) {
			return null;
		}
		return e.getParent().attrNearestScope();
	}

	public static WScope nextScope(WScope scope) {
		if (scope instanceof ModuleInstanciation) {
			ModuleInstanciation mi = (ModuleInstanciation) scope;
			ModuleDef m = mi.attrModuleOrigin();
			return m.attrNextScope();			
		} else {
			if (scope.getParent() != null) {
				return scope.getParent().attrNearestScope();
			} else {
				return null;
			}
		}
	}

	public static StructureDef nearestStructureDef(AstElement e) {
		if (e instanceof StructureDef) {
			return (StructureDef) e;
		} else if (e.getParent() != null) {
				return e.getParent().attrNearestStructureDef(); 
		} else { 
			return null;
		}
	}
	
	

}
