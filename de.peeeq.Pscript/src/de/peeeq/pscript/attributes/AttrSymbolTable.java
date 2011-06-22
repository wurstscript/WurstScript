package de.peeeq.pscript.attributes;

import org.eclipse.emf.ecore.EObject;

import de.peeeq.pscript.attributes.infrastructure.AbstractAttribute;
import de.peeeq.pscript.attributes.infrastructure.AttributeDependencies;
import de.peeeq.pscript.attributes.infrastructure.AttributeManager;
import de.peeeq.pscript.pscript.ClassDef;
import de.peeeq.pscript.pscript.FuncDef;
import de.peeeq.pscript.pscript.NameDef;
import de.peeeq.pscript.pscript.PackageDeclaration;
import de.peeeq.pscript.pscript.util.PscriptSwitch;

/**
 * This class defines an Attribute SymbolTable which attaches a SymbolTable  
 * to every node in the AST. The symbol table can be used to look up variables,
 * classes, functions, etc.
 */
public class AttrSymbolTable extends AbstractAttribute<EObject, SymbolTable> {

	// TODO error, when duplicate name
	
	
	@Override
	public
	SymbolTable calculate(final AttributeManager attributeManager,
			AttributeDependencies dependencies, EObject node) {
		return new PscriptSwitch<SymbolTable>() {

			@Override
			public SymbolTable caseClassDef(ClassDef c) {
				SymbolTable table = defaultCase(c);
				for (NameDef x : c.getMembers()) {
					table = table.insert(x.getName(), Declaration.of(x));
				}
				return table;
			}
			
			@Override
			public SymbolTable casePackageDeclaration(PackageDeclaration c) {
				SymbolTable table = defaultCase(c);
				for (NameDef x : c.getElements()) {
					table = table.insert(x.getName(), Declaration.of(x));
				}
				return table;
			}
			
			@Override
			public SymbolTable caseFuncDef(FuncDef c) {
				SymbolTable table = defaultCase(c);
				// TODO collect all local variables and parameters
				return table;
			}
			
			
			
			
			@Override
			public SymbolTable defaultCase(EObject object) {
				EObject parent = object.eContainer();
				if (parent != null) {
					// get symbol table from parent
					return attributeManager.getAttValue(AttrSymbolTable.class, parent);
				}
				return SymbolTableImpl.empty();
			}

		}.doSwitch(node);
	}

}
