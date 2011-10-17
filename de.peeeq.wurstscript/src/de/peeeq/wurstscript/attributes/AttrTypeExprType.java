package de.peeeq.wurstscript.attributes;

import katja.common.NE;
import de.peeeq.wurstscript.ast.ClassDefPos;
import de.peeeq.wurstscript.ast.NativeTypePos;
import de.peeeq.wurstscript.ast.NoTypeExprPos;
import de.peeeq.wurstscript.ast.OptTypeExprPos;
import de.peeeq.wurstscript.ast.TypeDefPos;
import de.peeeq.wurstscript.ast.TypeExprPos;
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
public class AttrTypeExprType extends Attribute<OptTypeExprPos, PscriptType> {


	public AttrTypeExprType(Attributes attr) {
		super(attr);
	}

	@Override
	protected PscriptType calculate(OptTypeExprPos optType) {
		if (optType instanceof TypeExprPos) {
			TypeExprPos node = (TypeExprPos) optType;
			PscriptType baseType = getBaseType(node);
			if (node.isArray().term()) {
				int[] sizes = new int[1];
				return new PScriptTypeArray(baseType, sizes );
			} else {
				return baseType;
			}
		} else {
			return PScriptTypeVoid.instance();
		}
	}
	
	private PscriptType getBaseType(TypeExprPos node) {	
		final String typename = node.typeName().term();
		final boolean isJassCode = Utils.isJassCode(node);
		TypeDefPos t = attr.typeDef.get(node);
		if (t == null) {
			PscriptType nativeType = NativeTypes.nativeType(typename, isJassCode);
			if (nativeType != null) {
				return nativeType;
			}
			attr.addError(node.source(), "Unknown type " + typename);
			return new PScriptTypeUnknown(typename);
		}
		PscriptType typ = t.Switch(new TypeDefPos.Switch<PscriptType, NE>() {

			@Override
			public PscriptType CaseNativeTypePos(NativeTypePos term)
					throws NE {
				PscriptType typ = NativeTypes.nativeType(term.name().term(), isJassCode);
				if (typ != null) {
					// native type
					return typ;
				}
				if (term.typ() instanceof NoTypeExprPos) {
					attr.addError(term.source(), "Unknown base type: " + term.name().term());
					return PScriptTypeUnknown.instance();
				}
				PscriptType superType = get((TypeExprPos) term.typ());
				return PscriptNativeType.instance(typename, superType);
			}

			@Override
			public PscriptType CaseClassDefPos(ClassDefPos term)
					throws NE {
				return new PscriptTypeClass(term);
			}
		});
		
//		if (node.isArray().term()) {
//			int[] sizes = new int[node.sizes().size()];
//			for (int i=0; i<sizes.length; i++) {
//				sizes[i] = 0; // TODO sizes should store ILvariables which actually hold the sizes
//			}
//			typ = new PScriptTypeArray(typ, sizes );
//		}
		
		return typ;
	}




}

