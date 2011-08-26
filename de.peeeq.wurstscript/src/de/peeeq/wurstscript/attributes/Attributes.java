package de.peeeq.wurstscript.attributes;

import java.util.List;

import de.peeeq.wurstscript.ast.WPos;
import de.peeeq.wurstscript.ast.WPosPos;
import de.peeeq.wurstscript.utils.NotNullList;

public class Attributes {

	public final AttrDoesReturn doesReturn = new AttrDoesReturn(this);
	public final AttrTypeExprType typeExprType = new AttrTypeExprType(this);
	public final AttrExprType exprType = new AttrExprType(this);
	public final AttrFuncDef funcDef = new AttrFuncDef(this);
	public final AttrVarDef varDef = new AttrVarDef(this);
	public final AttrVarScope varScope = new AttrVarScope(this);
	public final AttrFuncScope funcScope = new AttrFuncScope(this);
	public final AttrVarDefType varDefType = new AttrVarDefType(this);
	public final AttrPackageElements packageElements = new AttrPackageElements(this);
	public final AttrNearestPackage nearestPackage = new AttrNearestPackage(this);
	public final AttrNearestFuncDef nearestFuncDef = new AttrNearestFuncDef(this);
	public final AttrTypeDef typeDef = new AttrTypeDef(this);
	
	private List<CompileError> errors = new NotNullList<CompileError>();
	
	public void addError(WPos pos, String msg) {
		errors.add(new CompileError(pos, msg));
	}
	
	public void addError(WPosPos pos, String msg) {
		errors.add(new CompileError(pos, msg));
	}
	
}
