package de.peeeq.wurstscript.attributes;

import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableCollection.Builder;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;

import de.peeeq.wurstscript.ast.ClassDef;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.InterfaceDef;
import de.peeeq.wurstscript.ast.TypeExpr;
import de.peeeq.wurstscript.ast.TypeParamDef;
import de.peeeq.wurstscript.types.WurstType;
import de.peeeq.wurstscript.types.WurstTypeBoundTypeParam;
import de.peeeq.wurstscript.types.WurstTypeInterface;
import de.peeeq.wurstscript.utils.Utils;

public class InterfaceInstances {

	

	public static ImmutableCollection<WurstTypeInterface> getImplementedInterfaces(ClassDef c) {
		ImmutableCollection.Builder<WurstTypeInterface> result = ImmutableList.builder();
		for (TypeExpr t : c.getImplementsList()) {
			addInterface(result, t, null);
		}
		return result.build();
	}

	public static ImmutableCollection<WurstTypeInterface> getExtendedInterfaces(InterfaceDef in) {
		ImmutableCollection.Builder<WurstTypeInterface> result = ImmutableList.builder();
		for (TypeExpr t : in.getExtendsList()) {
			addInterface(result, t, in);
		}
		return result.build();
	}
	
	private static void addInterface(Builder<WurstTypeInterface> result, TypeExpr t, @Nullable InterfaceDef in) {
		if (t.attrTyp() instanceof WurstTypeInterface) {
			WurstTypeInterface i = (WurstTypeInterface) t.attrTyp();
			if (i.getDef() == in) {
				t.addError("Interfaces must not extend themselves.");
				return;
			}
			result.add(i);
			Map<TypeParamDef, WurstTypeBoundTypeParam> typeParamBounds = i.getTypeArgBinding();
			for (WurstTypeInterface i2 : i.getInterfaceDef().attrExtendedInterfaces()) {
				result.add((WurstTypeInterface) i2.setTypeArgs(typeParamBounds));
			}
			
		} else {
			t.addError(Utils.printElement(t) + " is not an interface.");
		}
	}

}
