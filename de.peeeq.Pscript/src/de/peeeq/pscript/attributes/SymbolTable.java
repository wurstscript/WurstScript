package de.peeeq.pscript.attributes;

import de.peeeq.immutablecollections.ImmutableList;



public interface SymbolTable {
	ImmutableList<Declaration> lookup(String name);
	SymbolTable insert(String name, Declaration decl);
	Iterable<Declaration> getDeclarations();	
}
