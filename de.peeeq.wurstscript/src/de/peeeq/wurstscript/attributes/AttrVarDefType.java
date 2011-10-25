package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.ast.Expr;
import de.peeeq.wurstscript.ast.GlobalVarDef;
import de.peeeq.wurstscript.ast.LocalVarDef;
import de.peeeq.wurstscript.ast.NoTypeExpr;
import de.peeeq.wurstscript.ast.OptExpr;
import de.peeeq.wurstscript.ast.OptTypeExpr;
import de.peeeq.wurstscript.ast.TypeExpr;
import de.peeeq.wurstscript.ast.VarDef;
import de.peeeq.wurstscript.ast.WParameter;
import de.peeeq.wurstscript.types.PscriptType;


/**
 * this attribute can give you the type of a variable definition
 *
 */
public class AttrVarDefType {
	
	public static  PscriptType calculate(VarDef node) {
		return node.match(new VarDef.Matcher<PscriptType>() {

			@Override
			public PscriptType case_GlobalVarDef(GlobalVarDef term)
					 {
				return defaultCase(term.getTyp(), term.getInitialExpr());
			}

			private PscriptType defaultCase(OptTypeExpr typ,
					final OptExpr initialExpr) {
				return typ.match(new OptTypeExpr.Matcher<PscriptType>() {

					@Override
					public PscriptType case_NoTypeExpr(NoTypeExpr nt)
							 {
						if (initialExpr instanceof Expr) {
							return ((Expr) initialExpr).attrTyp();
						} else {
							throw new Error("Vardef must either have a type or an initial value");
						}
					}

					@Override
					public PscriptType case_TypeExpr(TypeExpr term)
							 {
						return term.attrTyp();
					}
				});
			}

			@Override
			public PscriptType case_LocalVarDef(LocalVarDef term)
					 {
				return defaultCase(term.getTyp(), term.getInitialExpr());
			}

			@Override
			public PscriptType case_WParameter(WParameter term)  {
				return term.getTyp().attrTyp();
			}
		});
	}

}
