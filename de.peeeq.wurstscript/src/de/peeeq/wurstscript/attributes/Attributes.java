package de.peeeq.wurstscript.attributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.TopLevelDeclaration;
import de.peeeq.wurstscript.ast.WImport;
import de.peeeq.wurstscript.ast.WPackage;
import de.peeeq.wurstscript.ast.WPos;
import de.peeeq.wurstscript.gui.WurstGui;
import de.peeeq.wurstscript.utils.NotNullList;
import de.peeeq.wurstscript.utils.Utils;

public class Attributes {

//	// breaking the law of demeter :P
//	public final AttrDoesReturn doesReturn = new AttrDoesReturn(this);
//	public final AttrTypeExprType typeExprType = new AttrTypeExprType(this);
//	public final AttrExprType exprType = new AttrExprType(this);
//	public final AttrFuncDef funcDef = new AttrFuncDef(this);
//	public final AttrVarDef varDef = new AttrVarDef(this);
//	public final AttrScopeVariables varScope = new AttrScopeVariables(this);
//	public final AttrScopeFunctions funcScope = new AttrScopeFunctions(this);
//	public final AttrVarDefType varDefType = new AttrVarDefType(this);
//	public final AttrPackageElements packageElements = new AttrPackageElements(this);
//	public final AttrNearestPackage nearestPackage = new AttrNearestPackage(this);
//	public final AttrNearestFuncDef nearestFuncDef = new AttrNearestFuncDef(this);
//	public final AttrTypeDef typeDef = new AttrTypeDef(this);
//	public final AttrNearestClassDef nearestClassDef = new AttrNearestClassDef(this);
//	public final AttrConstructorDef constrDef = new AttrConstructorDef(this);
//	public final AttrExportedFunctions exportedFunctions = new AttrExportedFunctions(this);
//	public final AttrExportedVariables exportedVariables = new AttrExportedVariables(this);
//	public final AttrExportedTypes exportedTypes = new AttrExportedTypes(this);
	
	
	private List<CompileError> errors = new NotNullList<CompileError>();
	private WurstGui gui;
	
	
	public Attributes(WurstGui gui) {
		this.gui = gui;
	}

	public void addError(WPos pos, String msg) {
		CompileError c = new CompileError(pos, msg);
		errors.add(c);
		gui.sendError(c);
	}
	
	
	public int getErrorCount() {
		return errors.size();
	}
	
	public List<CompileError> getErrors() {
		return errors;
	}
	
	public void setProgress(String message, double percent) {
		gui.sendProgress(message, percent);
	}

	
	private Map<CompilationUnit, Map<String, WPackage>> packages = new HashMap<CompilationUnit, Map<String,WPackage>>();  
	
	/**
	 * Get the package imported by an import statement
	 * adds an error if the import could not be resolved and returns null
	 * @param i
	 * @return
	 */
	public WPackage getImportedPackage(WImport i) {
		CompilationUnit root = (CompilationUnit) Utils.getRoot(i);
		
		String packageName = i.getPackagename();
		Map<String, WPackage> p;
		if (!packages.containsKey(root)) {
			p = new HashMap<String, WPackage>();
			packages.put(root, p);
			
			for (TopLevelDeclaration x : root) {
				if (x instanceof WPackage) {
					WPackage WPackage = (WPackage) x;
					p.put(WPackage.getName(), WPackage);
				}
			}
		} else {
			p = packages.get(root);
		}
		
		WPackage pack = p.get(packageName);
		if (pack == null) {
			addError(i.getSource(), "Imported package " + packageName + " could not be found.");
		}
		return pack;
		
		
		
	}
}
