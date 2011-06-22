package de.peeeq.pscript.attributes;

import java.util.LinkedList;
import java.util.List;

import de.peeeq.immutablecollections.ImmutableList;
import de.peeeq.immutablecollections.ImmutableMap;


public class SymbolTableImpl implements SymbolTable {
	private static final SymbolTable emptySymbolTable = new SymbolTableImpl(ImmutableMap.emptyMap());
	
	/**
	 * A map from name to a list of declarations with the specified name.
	 * In the list the elements are ordered by their scope. At the beginning
	 * of the list are declarations from the global scopes and at the end
	 * declarations from the innermost scope.
	 */
	ImmutableMap<String, ImmutableList<Declaration>> nameToDeclarations;

	
	
	private SymbolTableImpl(ImmutableMap<String, ImmutableList<Declaration>> nameToDeclarations) {
		this.nameToDeclarations = nameToDeclarations;
	}
	
	static SymbolTable empty() {
		return emptySymbolTable; 
	}
	
	@Override
	public ImmutableList<Declaration> lookup(String name) {
		ImmutableList<Declaration> result = nameToDeclarations.get(name);
		if (result == null) {
			// return empty list
			result = ImmutableList.emptyList(); 
		}
		return result;
	}

	@Override
	public SymbolTable insert(String name, Declaration decl) {
		ImmutableList<Declaration> currentDecls = lookup(name);
		currentDecls = currentDecls.appFront(decl);
		ImmutableMap<String, ImmutableList<Declaration>> newMap = nameToDeclarations.put(name, currentDecls);
		return new SymbolTableImpl(newMap);
	}

	@Override
	public Iterable<Declaration> getDeclarations() {
		Iterable<ImmutableList<Declaration>> lists = nameToDeclarations.values();
		List<Declaration> result = new LinkedList<Declaration>();
		for (ImmutableList<Declaration> list : lists) {
			if (!list.isEmpty()) {
				result.add(list.head());
			}
		}		
		return result;
	}
}
