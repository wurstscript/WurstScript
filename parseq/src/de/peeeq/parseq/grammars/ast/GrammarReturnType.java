package de.peeeq.parseq.grammars.ast;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.google.common.collect.Lists;

import de.peeeq.parseq.asts.ast.AstEntityDefinition;

public class GrammarReturnType {
	
	public static class NamedType {
		public final String name;
		public final AstEntityDefinition entity;
		
		public NamedType(String name, AstEntityDefinition entity) {
			this.name = name;
			this.entity = entity;
		}
	}
	
	private final List<NamedType> returnedTypes;
	
	public GrammarReturnType(String name, AstEntityDefinition entity) {
		returnedTypes = Collections.singletonList(new NamedType(name, entity));
	}
	

	public GrammarReturnType(List<NamedType> returnedTypes) {
		this.returnedTypes = returnedTypes;
	}

	public static GrammarReturnType or(List<GrammarReturnType> ts) {
		List<NamedType> r = Lists.newArrayList();
		for (int i=0; i<ts.get(0).getReturnedTypes().size(); i++) {
		}
		return new GrammarReturnType(r);
	}
	
	public static GrammarReturnType sequence(List<GrammarReturnType> ts) {
		List<NamedType> r = Lists.newArrayList();
		return new GrammarReturnType(r);
	}

	public List<NamedType> getReturnedTypes() {
		return returnedTypes;
	}
	
}
