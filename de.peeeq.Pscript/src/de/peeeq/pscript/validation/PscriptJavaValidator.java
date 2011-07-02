package de.peeeq.pscript.validation;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.validation.Check;

import com.google.inject.Inject;

import de.peeeq.pscript.analysis.ITypechecker;
import de.peeeq.pscript.attributes.AttrExprType;
import de.peeeq.pscript.attributes.infrastructure.AttributeManager;
import de.peeeq.pscript.pscript.ClassDef;
import de.peeeq.pscript.pscript.Expr;
import de.peeeq.pscript.pscript.ExprFunctioncall;
import de.peeeq.pscript.pscript.FuncDef;
import de.peeeq.pscript.pscript.NameDef;
import de.peeeq.pscript.pscript.PackageDeclaration;
import de.peeeq.pscript.pscript.Program;
import de.peeeq.pscript.pscript.PscriptPackage;
import de.peeeq.pscript.pscript.Statements;
import de.peeeq.pscript.types.PscriptType;
import de.peeeq.pscript.types.PscriptTypeError;

 

public class PscriptJavaValidator extends AbstractPscriptJavaValidator {

public static final Integer TYPE_NAME = 2;
public static final String INVALID_TYPE_NAME = "INVALID_TYPE_NAME";
public static final String OTHER_ERROR = "OTHER_ERROR";
private static final String INVALID_TYPE = "INVALID_TYPE";

//	@Check
//	public void checkGreetingStartsWithCapital(Greeting greeting) {
//		if (!Character.isUpperCase(greeting.getName().charAt(0))) {
//			warning("Name should start with a capital", MyDslPackage.GREETING__NAME);
//		}
//	}

//	@Inject
//	private ITypechecker tc;

	@Inject
	private AttributeManager attrManager;
	
	
	@Check
	public void checkTypes(Expr e) {
		PscriptType t = attrManager.getAttValue(AttrExprType.class, e);
		if (t == null) {
			this.
			error("Could not determine type of expression", PscriptPackage.Literals.EXPR.getEStructuralFeature(0) , INVALID_TYPE, e.toString());
		}
		if (t instanceof PscriptTypeError) {
			error(t.getName(), PscriptPackage.Literals.EXPR.getEStructuralFeature(0), INVALID_TYPE, e.toString());
		}
	}
	
//	@Check
//	public void checkTypesystemRules( EObject x ) {
//		tc.check(x, this);
//	}
	
	
	@Check
	public void checkClassNames(ClassDef c) {
		if (!Character.isUpperCase(c.getName().charAt(0))) {
			error("Type names must start with a capital letter", 
					PscriptPackage.Literals.CLASS_DEF.getEStructuralFeature("name")
					/*PscriptPackage.CLASS_DEF__NAME*/ , INVALID_TYPE_NAME, c.getName());
			//error("Type names must start with a capital letter", TYPE_NAME, INVALID_TYPE_NAME, c.getName());
		}
		
	}
	
	@Check
	public void checkCompilationUnit(Program p) {
		attrManager.reset();
	}
	
//	@Check
//	public void checkTypes(FuncDef f) {
//		Statements statements = f.getBody();
//	}
//	
//	@Check
//	public void checkTypes(ExprFunctioncall fc) {
//		NameDef funcRef = fc.getNameVal().getReferenced();
//		if (funcRef instanceof FuncDef) {
//			
//		} else {
//			error(funcRef.getName() + " is not a function.", PscriptPackage.EXPR_FUNCTIONCALL__NAME_VAL , OTHER_ERROR, funcRef.getName());
//		}
//	}
	

}
