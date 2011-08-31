package de.peeeq.wurstscript.attributes;

import katja.common.NE;
import de.peeeq.wurstscript.ast.ClassDefPos;
import de.peeeq.wurstscript.ast.NativeTypePos;
import de.peeeq.wurstscript.ast.NoTypeExprPos;
import de.peeeq.wurstscript.ast.TypeDefPos;
import de.peeeq.wurstscript.ast.TypeExprPos;
import de.peeeq.wurstscript.types.NativeTypes;
import de.peeeq.wurstscript.types.PScriptTypeArray;
import de.peeeq.wurstscript.types.PScriptTypeUnknown;
import de.peeeq.wurstscript.types.PscriptNativeType;
import de.peeeq.wurstscript.types.PscriptType;
import de.peeeq.wurstscript.types.PscriptTypeClass;


/**
 * this attribute gives you the type for a type expr
 *
 */
public class AttrTypeExprType extends Attribute<TypeExprPos, PscriptType> {


	public AttrTypeExprType(Attributes attr) {
		super(attr);
	}

	@Override
	protected PscriptType calculate(TypeExprPos node) {
		PscriptType baseType = getBaseType(node);
		if (node.isArray().term()) {
			int[] sizes = new int[1];
			return new PScriptTypeArray(baseType, sizes );
		} else {
			return baseType;
		}
	}
	
	private PscriptType getBaseType(TypeExprPos node) {	
		final String typename = node.typeName().term();
		TypeDefPos t = attr.typeDef.get(node);
		if (t == null) {
			PscriptType nativeType = NativeTypes.nativeType(typename);
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
				PscriptType typ = NativeTypes.nativeType(term.name().term());
				if (typ != null) {
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

