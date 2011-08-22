package de.peeeq.wurstscript.attributes;

import katja.common.NE;
import de.peeeq.wurstscript.ast.ExprPos;
import de.peeeq.wurstscript.ast.GlobalVarDefPos;
import de.peeeq.wurstscript.ast.LocalVarDefPos;
import de.peeeq.wurstscript.ast.NoTypeExprPos;
import de.peeeq.wurstscript.ast.OptExprPos;
import de.peeeq.wurstscript.ast.OptTypeExprPos;
import de.peeeq.wurstscript.ast.TypeExprPos;
import de.peeeq.wurstscript.ast.VarDefPos;
import de.peeeq.wurstscript.ast.WParameterPos;
import de.peeeq.wurstscript.types.PscriptType;


/**
 * this attribute can give you the type of a variable definition
 *
 */
public class AttrVarDefType extends Attribute<VarDefPos, PscriptType> {

	public AttrVarDefType(Attributes attr) {
		super(attr);
	}

	@Override
	protected PscriptType calculate(VarDefPos node) {
		return node.Switch(new VarDefPos.Switch<PscriptType, NE>() {

			@Override
			public PscriptType CaseGlobalVarDefPos(GlobalVarDefPos term)
					throws NE {
				return defaultCase(term.typ(), term.initialExpr());
			}

			private PscriptType defaultCase(OptTypeExprPos typ,
					final OptExprPos initialExpr) {
				return typ.Switch(new OptTypeExprPos.Switch<PscriptType, NE>() {

					@Override
					public PscriptType CaseNoTypeExprPos(NoTypeExprPos nt)
							throws NE {
						if (initialExpr instanceof ExprPos) {
							return attr.exprType.get((ExprPos) initialExpr);
						} else {
							throw new Error("Vardef must either have a type or an initial value");
						}
					}

					@Override
					public PscriptType CaseTypeExprPos(TypeExprPos term)
							throws NE {
						return attr.typeExprType.get(term);
					}
				});
			}

			@Override
			public PscriptType CaseLocalVarDefPos(LocalVarDefPos term)
					throws NE {
				return defaultCase(term.typ(), term.initialExpr());
			}

			@Override
			public PscriptType CaseWParameterPos(WParameterPos term) throws NE {
				return attr.typeExprType.get(term.typ());
			}
		});
	}

}
