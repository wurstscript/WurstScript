package de.peeeq.wurstscript.types;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import de.peeeq.wurstscript.ast.ClassDef;
import de.peeeq.wurstscript.ast.ClassOrModule;
import de.peeeq.wurstscript.ast.InstanceDef;
import de.peeeq.wurstscript.ast.ModuleDef;
import de.peeeq.wurstscript.ast.TypeExpr;
import de.peeeq.wurstscript.ast.TypeExprSimple;
import de.peeeq.wurstscript.ast.TypeParamDef;
import de.peeeq.wurstscript.attributes.CompileError;

public class TypesHelper {

	public static PscriptType typeOf(ClassOrModule classOrModule, final boolean isStatic) {
		return classOrModule.match(new ClassOrModule.Matcher<PscriptType>() {

			@Override
			public PscriptType case_ClassDef(ClassDef classDef) {
				return new PscriptTypeClass(classDef, isStatic);
			}

			@Override
			public PscriptType case_ModuleDef(ModuleDef moduleDef) {
				return new PscriptTypeModule(moduleDef, isStatic);
			}
		});
	}

	public static boolean checkTypeArgs(InstanceDef iDef, List<PscriptType> classParams, List<PscriptType> interfaceParams) {
		if (classParams.size() == 0 && interfaceParams.size() == 0) {
			return true;
		}
		
		Map<TypeParamDef, PscriptType> typeParamBinding = Maps.newHashMap(); 
		
		PscriptTypeClass classType = (PscriptTypeClass) iDef.getClassTyp().attrTyp();
		int i = 0;
		for (PscriptType classTp : classType.getTypeParameters()) {
			PscriptTypeTypeParam classTp2 = (PscriptTypeTypeParam) classTp;
			TypeParamDef tpDef = classTp2.getDef();
			typeParamBinding.put(tpDef, classParams.get(i));
			i++;
		}
		
		ArrayList<PscriptType> actualInterfaceTypes = Lists.newArrayList();
		
		PscriptTypeInterface interfaceType = (PscriptTypeInterface) iDef.getImplementedTyp().attrTyp();
		for (PscriptType intTp : interfaceType.getTypeParameters()) {
			if (intTp instanceof PscriptTypeTypeParam) {
				PscriptTypeTypeParam intTp2 = (PscriptTypeTypeParam) intTp;
				actualInterfaceTypes.add(typeParamBinding.get(intTp2.getDef()));
			} else {
				actualInterfaceTypes.add(intTp);
			}
		}
		
		if (actualInterfaceTypes.size() != interfaceParams.size()) {
			throw new CompileError(iDef.getSource(), "sizes do not match: " + actualInterfaceTypes.size()+ " != " + interfaceParams.size());
		}
		
		i = 0;
		for (PscriptType iTp : interfaceParams) {
			if (!iTp.equalsType(actualInterfaceTypes.get(i), iDef)) {
				return false;
			}
			i++;
		}
		
		
		return true;
	}


}
