package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.ast.ClassDef;
import de.peeeq.wurstscript.ast.ClassOrModule;
import de.peeeq.wurstscript.ast.ModuleDef;
import de.peeeq.wurstscript.ast.NativeType;
import de.peeeq.wurstscript.ast.NoTypeExpr;
import de.peeeq.wurstscript.ast.OptTypeExpr;
import de.peeeq.wurstscript.ast.TypeDef;
import de.peeeq.wurstscript.ast.TypeExpr;
import de.peeeq.wurstscript.ast.TypeExprSimple;
import de.peeeq.wurstscript.ast.TypeExprThis;
import de.peeeq.wurstscript.types.NativeTypes;
import de.peeeq.wurstscript.types.PScriptTypeArray;
import de.peeeq.wurstscript.types.PScriptTypeThis;
import de.peeeq.wurstscript.types.PScriptTypeUnknown;
import de.peeeq.wurstscript.types.PScriptTypeVoid;
import de.peeeq.wurstscript.types.PscriptNativeType;
import de.peeeq.wurstscript.types.PscriptType;
import de.peeeq.wurstscript.types.PscriptTypeClass;
import de.peeeq.wurstscript.types.PscriptTypeModule;
import de.peeeq.wurstscript.utils.Utils;


/**
 * this attribute gives you the type for a type expr
 *
 */
public class AttrTypeExprType {
	
	public static  PscriptType calculate(OptTypeExpr optType) {
		return optType.match(new OptTypeExpr.Matcher<PscriptType>() {

			@Override
			public PscriptType case_TypeExprSimple(TypeExprSimple node) {
				PscriptType baseType = getBaseType(node);
				if (node.getIsArray()) {
					int[] sizes = new int[1];
					return new PScriptTypeArray(baseType, sizes );
				} else {
					return baseType;
				}
			}

			@Override
			public PscriptType case_TypeExprThis(final TypeExprThis node) {
				ClassOrModule classOrModule = node.attrNearestClassOrModule();
				return node.attrNearestClassOrModule().match(new ClassOrModule.Matcher<PscriptType>() {

					@Override
					public PscriptType case_ClassDef(ClassDef classDef) {
						attr.addError(node.getSource(), "The type 'thistype' can only be used inside modules.");
						return new PscriptTypeClass(classDef);
					}

					@Override
					public PscriptType case_ModuleDef(ModuleDef moduleDef) {
						return new PscriptTypeModule(moduleDef);
					}
				});
			}

			@Override
			public PscriptType case_NoTypeExpr(NoTypeExpr node) {
				return PScriptTypeVoid.instance();
			}
		});
	}
	
	private static PscriptType getBaseType(TypeExprSimple node) {	
		final String typename = node.getTypeName();
		final boolean isJassCode = Utils.isJassCode(node);
		TypeDef t = node.attrTypeDef();
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
				PscriptType superType = ((TypeExpr) term.getTyp()).attrTyp();
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

