package wursteditor.rsyntax;

import java.awt.event.MouseEvent;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextArea;
import org.fife.ui.rtextarea.ToolTipSupplier;

import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.ast.ClassDef;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.ExtensionFuncDef;
import de.peeeq.wurstscript.ast.FuncDef;
import de.peeeq.wurstscript.ast.FuncRef;
import de.peeeq.wurstscript.ast.FunctionDefinition;
import de.peeeq.wurstscript.ast.GlobalVarDef;
import de.peeeq.wurstscript.ast.LocalVarDef;
import de.peeeq.wurstscript.ast.ModuleDef;
import de.peeeq.wurstscript.ast.ModuleInstanciation;
import de.peeeq.wurstscript.ast.NameDef;
import de.peeeq.wurstscript.ast.NameRef;
import de.peeeq.wurstscript.ast.NativeFunc;
import de.peeeq.wurstscript.ast.NativeType;
import de.peeeq.wurstscript.ast.TypeExpr;
import de.peeeq.wurstscript.ast.TypeParamDef;
import de.peeeq.wurstscript.ast.WParameter;

import wursteditor.controller.SyntaxCodeAreaController;

public class WurstToolTipSupplier implements ToolTipSupplier {

	@Override
	public String getToolTipText(RTextArea t, MouseEvent e) {
		if (t instanceof RSyntaxTextArea) {
			RSyntaxTextArea rs = (RSyntaxTextArea) t;
			int pos = rs.viewToModel(e.getPoint());
			SyntaxCodeAreaController controller = SyntaxCodeAreaController.getFor(rs);
			CompilationUnit ast = controller.getNewAst();
			AstElement elem = AstHelper.getAstElementAtPos(ast, pos);
			if (elem instanceof NameRef) {
				NameRef nameRef = (NameRef) elem;
				NameDef def = nameRef.attrNameDef();
				return getTooltipForNameDef(def);
			} else if (elem instanceof FuncRef) {
				FuncRef fr = (FuncRef) elem;
				FunctionDefinition def = fr.attrFuncDef();
				return getTooltipForNameDef(def); 
			} else if (elem instanceof TypeExpr) {
				TypeExpr te = (TypeExpr) elem;
				return "type: " + te.attrTyp();
			}
		}
		return null;
	}

	private String getTooltipForNameDef(NameDef def) {
		if (def == null) return null;
		return def.match(new NameDef.Matcher<String>() {

			@Override
			public String case_ClassDef(ClassDef classDef) {
				return "class: " + classDef.getName();
			}

			@Override
			public String case_NativeType(NativeType nativeType) {
				return "native type: " + nativeType.getName();
			}

			@Override
			public String case_ExtensionFuncDef(ExtensionFuncDef extensionFuncDef) {
				return "extension function: " + extensionFuncDef.getName();
			}

			@Override
			public String case_FuncDef(FuncDef funcDef) {
				return "function: " + funcDef.getName();
			}

			@Override
			public String case_WParameter(WParameter wParameter) {
				return "Parameter: " + wParameter.attrTyp() + " " +wParameter.getName();
			}

			@Override
			public String case_NativeFunc(NativeFunc nativeFunc) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public String case_GlobalVarDef(GlobalVarDef g) {
				return "global variable: " + g.attrTyp() + " " + g.getName();
			}

			@Override
			public String case_ModuleInstanciation(
					ModuleInstanciation moduleInstanciation) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public String case_ModuleDef(ModuleDef moduleDef) {
				return "module: " + moduleDef.getName();
			}

			@Override
			public String case_LocalVarDef(LocalVarDef v) {
				return "local variable: " + v.attrTyp() + " " + v.getName();
			}

			@Override
			public String case_TypeParamDef(TypeParamDef typeParamDef) {
				return "type parameter: " + typeParamDef.getName();
			}
		});
	}

}
