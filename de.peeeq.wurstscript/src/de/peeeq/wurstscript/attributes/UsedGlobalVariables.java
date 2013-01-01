package de.peeeq.wurstscript.attributes;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;

import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.ast.ConstructorDef;
import de.peeeq.wurstscript.ast.ExprNewObject;
import de.peeeq.wurstscript.ast.ExprOrStatements;
import de.peeeq.wurstscript.ast.FuncRef;
import de.peeeq.wurstscript.ast.FunctionDefinition;
import de.peeeq.wurstscript.ast.FunctionImplementation;
import de.peeeq.wurstscript.ast.GlobalVarDef;
import de.peeeq.wurstscript.ast.NameDef;
import de.peeeq.wurstscript.ast.NameRef;
import de.peeeq.wurstscript.ast.NativeFunc;
import de.peeeq.wurstscript.ast.OnDestroyDef;
import de.peeeq.wurstscript.ast.StmtDestroy;
import de.peeeq.wurstscript.ast.TupleDef;
import de.peeeq.wurstscript.ast.VarDef;
import de.peeeq.wurstscript.types.WurstType;
import de.peeeq.wurstscript.types.WurstTypeClass;

public class UsedGlobalVariables {

	public static List<VarDef> getUsedGlobals(ExprOrStatements e) {
		List<VarDef> result = Lists.newArrayList();
		if (e instanceof FuncRef) {
			FuncRef funcRef = (FuncRef) e;
			FunctionDefinition f = funcRef.attrFuncDef();
			if (f != null) {
				result.addAll(f.attrUsedGlobalVariables());
			}
		
		} else if (e instanceof ExprNewObject) {
			ExprNewObject exprNewObject = (ExprNewObject) e;
			ConstructorDef constr = exprNewObject.attrConstructorDef();
			if (constr != null) {
				result.addAll(constr.getBody().attrUsedGlobalVariables());
			}
		} else if (e instanceof StmtDestroy) {
			StmtDestroy stmtDestroy = (StmtDestroy) e;
			WurstType t = stmtDestroy.getDestroyedObj().attrTyp();
			if (t instanceof WurstTypeClass) {
				WurstTypeClass ct = (WurstTypeClass) t;
				OnDestroyDef ondestr = ct.getClassDef().getOnDestroy();
				result.addAll(ondestr.getBody().attrUsedGlobalVariables());
			}
		} else if (e instanceof NameRef) {
			NameRef nameRef = (NameRef) e;
			NameDef def = nameRef.attrNameDef();
			if (def instanceof GlobalVarDef) {
				GlobalVarDef varDef = (GlobalVarDef) def;
				result.add(varDef);
			}
		}
		// check children:
		for (int i=0; i<e.size(); i++) {
			AstElement child = e.get(i);
			if (child instanceof ExprOrStatements) {
				ExprOrStatements child2 = (ExprOrStatements) child;
				result.addAll(child2.attrUsedGlobalVariables());
			}
		}
		return result;
	}
	

	public static List<VarDef> getUsedGlobals(FunctionImplementation func) {
		return func.getBody().attrUsedGlobalVariables();
	}

	public static List<VarDef> getUsedGlobals(NativeFunc nativeFunc) {
		return Collections.emptyList();
	}

	public static List<VarDef> getUsedGlobals(TupleDef tupleDef) {
		return Collections.emptyList();
	}
	
	

}
