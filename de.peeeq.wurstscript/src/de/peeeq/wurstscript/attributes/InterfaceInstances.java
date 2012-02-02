package de.peeeq.wurstscript.attributes;

import java.util.Collection;
import java.util.Map;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import de.peeeq.wurstscript.ast.ClassDef;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.InterfaceDef;
import de.peeeq.wurstscript.ast.TypeExpr;
import de.peeeq.wurstscript.ast.TypeParamDef;
import de.peeeq.wurstscript.types.PscriptType;
import de.peeeq.wurstscript.types.PscriptTypeInterface;
import de.peeeq.wurstscript.utils.Utils;

public class InterfaceInstances {

	public static Multimap<InterfaceDef, ClassDef> getInterfaceInstances(CompilationUnit cu) {
		Multimap<InterfaceDef, ClassDef> result = HashMultimap.create();
		for (ClassDef c : cu.attrGetByType().classes) {
			for (PscriptTypeInterface i : c.attrImplementedInterfaces()) {
				result.put(i.getInterfaceDef(), c);
			}
		}
		return result;
	}

	public static Collection<PscriptTypeInterface> getImplementedInterfaces(ClassDef c) {
		Collection<PscriptTypeInterface> result = Lists.newArrayList();
		for (TypeExpr t : c.getImplementsList()) {
			addInterface(result, t);
		}
		return result;
	}

	public static Collection<PscriptTypeInterface> getExtendedInterfaces(InterfaceDef in) {
		Collection<PscriptTypeInterface> result = Lists.newArrayList();
		for (TypeExpr t : in.getExtendsList()) {
			addInterface(result, t);
		}
		return result;
	}
	
	private static void addInterface(Collection<PscriptTypeInterface> result, TypeExpr t) {
		if (t.attrTyp() instanceof PscriptTypeInterface) {
			PscriptTypeInterface i = (PscriptTypeInterface) t.attrTyp();
			result.add(i);
			Map<TypeParamDef, PscriptType> typeParamBounds = i.getTypeArgBinding();
			for (PscriptTypeInterface i2 : i.getInterfaceDef().attrExtendedInterfaces()) {
				result.add((PscriptTypeInterface) i2.setTypeArgs(typeParamBounds));
			}
		} else {
			attr.addError(t.getSource(), Utils.printElement(t) + " is not an interface.");
		}
	}

}
