package de.peeeq.wurstscript.attributes;

import java.util.Map;

import de.peeeq.wurstscript.ast.TypeDefPos;
import de.peeeq.wurstscript.ast.WEntityPos;
import de.peeeq.wurstscript.ast.WImportPos;
import de.peeeq.wurstscript.ast.WPackagePos;
import de.peeeq.wurstscript.utils.DefinitionMap;


/**
 * this attribute calculates the available types for a given WPackage 
 */
public class AttrScopeType extends Attribute<WPackagePos, Map<String, TypeDefPos>> {


	public AttrScopeType(Attributes attr) {
		super(attr);
	}

	@Override
	protected Map<String, TypeDefPos> calculate(WPackagePos node) {
		final Map<String, TypeDefPos> result = new DefinitionMap<String, TypeDefPos>() {

			@Override
			public void onElementRedefined(TypeDefPos firstDefinition, TypeDefPos secondDefinition, String name) {
				attr.addError(secondDefinition.source(), "Type " + name + " redefined. Already defined in " + firstDefinition.source().term());
			}
		};
		for (WImportPos i : node.imports()) {
			WPackagePos importedPackage = attr.getImportedPackage(i);
			if (importedPackage == null) {
				continue;
			}
			result.putAll(attr.exportedTypes.get(importedPackage));
		}
		for (WEntityPos e : node.elements()) {
			if (e instanceof TypeDefPos) {
				TypeDefPos t = (TypeDefPos) e;
				result.put(t.name().term(), t);
			}
		}
		return result;
	}


}
