package de.peeeq.wurstscript.attributes;

import java.util.Map;

import de.peeeq.wurstscript.ast.TypeDef;
import de.peeeq.wurstscript.ast.WEntity;
import de.peeeq.wurstscript.ast.WImport;
import de.peeeq.wurstscript.ast.WPackage;
import de.peeeq.wurstscript.utils.DefinitionMap;


/**
 * this attribute calculates the available types for a given WPackage 
 */
public class AttrScopeType extends Attribute<WPackage, Map<String, TypeDef>> {


	public AttrScopeType(Attributes attr) {
		super(attr);
	}

	@Override
	protected Map<String, TypeDef> calculate(WPackage node) {
		final Map<String, TypeDef> result = new DefinitionMap<String, TypeDef>() {

			@Override
			public void onElementRedefined(TypeDef firstDefinition, TypeDef secondDefinition, String name) {
				attr.addError(secondDefinition.getSource(), "Type " + name + " redefined. Already defined in " + firstDefinition.getSource());
			}
		};
		for (WImport i : node.getImports()) {
			WPackage importedPackage = attr.getImportedPackage(i);
			if (importedPackage == null) {
				continue;
			}
			result.putAll(attr.exportedTypes.get(importedPackage));
		}
		for (WEntity e : node.getElements()) {
			if (e instanceof TypeDef) {
				TypeDef t = (TypeDef) e;
				result.put(t.getName(), t);
			}
		}
		return result;
	}


}
