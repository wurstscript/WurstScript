package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.ast.ClassDef;
import de.peeeq.wurstscript.ast.NativeType;
import de.peeeq.wurstscript.ast.NoTypeExpr;
import de.peeeq.wurstscript.ast.OptTypeExpr;
import de.peeeq.wurstscript.ast.TypeDef;
import de.peeeq.wurstscript.ast.TypeExpr;
import de.peeeq.wurstscript.types.NativeTypes;
import de.peeeq.wurstscript.types.PScriptTypeArray;
import de.peeeq.wurstscript.types.PScriptTypeUnknown;
import de.peeeq.wurstscript.types.PScriptTypeVoid;
import de.peeeq.wurstscript.types.PscriptNativeType;
import de.peeeq.wurstscript.types.PscriptType;
import de.peeeq.wurstscript.types.PscriptTypeClass;
import de.peeeq.wurstscript.utils.Utils;


/**
 * this attribute gives you the type for a type expr
 *
 */
public class AttrTypeExprType extends Attribute<OptTypeExpr, PscriptType> {


	public AttrTypeExprType(Attributes attr) {
		super(attr);
	}

	@Override
	protected PscriptType calculate(OptTypeExpr optType) {
		if (optType instanceof TypeExpr) {
			TypeExpr node = (TypeExpr) optType;
			PscriptType baseType = getBaseType(node);
			if (node.getIsArray()) {
				int[] sizes = new int[1];
				return new PScriptTypeArray(baseType, sizes );
			} else {
				return baseType;
			}
		} else {
			return PScriptTypeVoid.instance();
		}
	}
	
	private PscriptType getBaseType(TypeExpr node) {	
		final String typename = node.getTypeName();
		final boolean isJassCode = Utils.isJassCode(node);
		TypeDef t = attr.typeDef.get(node);
		if (t == null) {
			PscriptType nativeType = NativeTypes.nativeType(typename, isJassCode);
			if (nativeType != null) {
				return nativeType;
			}
			attr.addError(node.getSource(), "Unknown type " + typename);
			return new PScriptTypeUnknown(typename);
		}
		PscriptType typ = t.match(new TypeDef.Matcher<PscriptType>() {

			@Override
			public PscriptType case_NativeType(NativeType term)
					 {
				PscriptType typ = NativeTypes.nativeType(term.getName(), isJassCode);
				if (typ != null) {
					// native type
					return typ;
				}
				if (term.getTyp() instanceof NoTypeExpr) {
					attr.addError(term.getSource(), "Unknown base type: " + term.getName());
					return PScriptTypeUnknown.instance();
				}
				PscriptType superType = get((TypeExpr) term.getTyp());
				return PscriptNativeType.instance(typename, superType);
			}

			@Override
			public PscriptType case_ClassDef(ClassDef term)
					 {
				return new PscriptTypeClass(term);
			}
		});
		
//		if (node.isArray()) {
//			int[] sizes = new int[node.sizes().size()];
//			for (int i=0; i<sizes.length; i++) {
//				sizes[i] = 0; // TODO sizes should store ILvariables which actually hold the sizes
//			}
//			typ = new PScriptTypeArray(typ, sizes );
//		}
		
		return typ;
	}




}

