package de.peeeq.pscript.attributes;


import de.peeeq.pscript.attributes.infrastructure.AbstractAttribute;
import de.peeeq.pscript.attributes.infrastructure.AttributeDependencies;
import de.peeeq.pscript.attributes.infrastructure.AttributeManager;
import de.peeeq.pscript.pscript.ClassDef;
import de.peeeq.pscript.pscript.NativeType;
import de.peeeq.pscript.pscript.TypeDef;
import de.peeeq.pscript.pscript.TypeExpr;
import de.peeeq.pscript.pscript.TypeExprBuildin;
import de.peeeq.pscript.pscript.TypeExprRef;
import de.peeeq.pscript.pscript.impl.TypeDefImpl;
import de.peeeq.pscript.pscript.util.TypeDefSwitch;
import de.peeeq.pscript.pscript.util.TypeExprSwitch;
import de.peeeq.pscript.types.PScriptTypeBool;
import de.peeeq.pscript.types.PScriptTypeCode;
import de.peeeq.pscript.types.PScriptTypeHandle;
import de.peeeq.pscript.types.PScriptTypeInt;
import de.peeeq.pscript.types.PScriptTypeReal;
import de.peeeq.pscript.types.PScriptTypeString;
import de.peeeq.pscript.types.PScriptTypeUnknown;
import de.peeeq.pscript.types.PScriptTypeVoid;
import de.peeeq.pscript.types.PscriptNativeType;
import de.peeeq.pscript.types.PscriptType;
import de.peeeq.pscript.types.PscriptTypeError;

/**
 * 
 * This attribute determines the type of a type expression
 *
 */
public class AttrTypeExprType extends AbstractAttribute<TypeExpr, PscriptType> {


	@Override
	public
	PscriptType calculate(final AttributeManager attributeManager,
			AttributeDependencies dependencies, TypeExpr node) {
		return new TypeExprSwitch<PscriptType>() {

			@Override
			public PscriptType caseTypeExprBuildin(
					TypeExprBuildin typeExprBuildin) {
				String name = typeExprBuildin.getName();
				if (name.equals("integer")) {
					return PScriptTypeInt.instance();
				}
				if (name.equals("real")) {
					return PScriptTypeReal.instance();
				}
				if (name.equals("string")) {
					return PScriptTypeString.instance();
				}
				if (name.equals("boolean")) {
					return PScriptTypeBool.instance();
				}
				if (name.equals("handle")) {
					return PScriptTypeHandle.instance();
				}
				if (name.equals("code")) {
					return PScriptTypeCode.instance();
				}				
				throw new Error("Unknown buildin type: " + name);
			}

			@Override
			public PscriptType caseTypeExprRef(TypeExprRef typeExprRef) {
				TypeDef typeDef = typeExprRef.getName();
				if (typeDef == null || typeDef.getClass().equals(TypeDefImpl.class)) {
					return new PscriptTypeError("Unknown type");
				}
				PscriptType result = getType(attributeManager, typeDef);	
				return result;
			}
		}.doSwitch(node);
	}

	private PscriptType getType(final AttributeManager attributeManager,final TypeDef typeDef) {
		return new TypeDefSwitch<PscriptType>() {

			@Override
			public PscriptType caseClassDef(ClassDef classDef) {
				return getType_ClassDef(attributeManager, (ClassDef) typeDef);
			}

			@Override
			public PscriptType caseNativeType(NativeType nativeType) {
				return getType_NativeType(attributeManager, (NativeType) typeDef);
			}

		}.doSwitch(typeDef);
		
//		return new PscriptTypeError("unexpected typeDef type: " + typeDef.getName() + " ( " + typeDef + " )");
	}

	private PscriptType getType_NativeType(final AttributeManager attributeManager,NativeType typeDef) {
		if (typeDef.getName().equals("Int")) {
			return PScriptTypeInt.instance();
		}
		if (typeDef.getName().equals("Bool")) {
			return PScriptTypeBool.instance();
		}
		if (typeDef.getName().equals("Real")) {
			return PScriptTypeReal.instance();
		}
		if (typeDef.getName().equals("String")) {
			return PScriptTypeString.instance();
		}
		if (typeDef.getName().equals("Code")) {
			return PScriptTypeCode.instance();
		}
		if (typeDef.getName().equals("Handle")) {
			return PScriptTypeHandle.instance();
		}
		
		PscriptType superType;
		if (typeDef.getSuperName() != null) {
			superType = attributeManager.getAttValue(AttrTypeExprType.class, typeDef.getSuperName());
		} else {
			superType = PScriptTypeVoid.instance();
		}
		return PscriptNativeType.instance(typeDef.getName(), superType);
	}

	private PscriptType getType_ClassDef(final AttributeManager attributeManager,ClassDef typeDef) {
		// TODO class types
		return PScriptTypeUnknown.instance();
	}

	

}
