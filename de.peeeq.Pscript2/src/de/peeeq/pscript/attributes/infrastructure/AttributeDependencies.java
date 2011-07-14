package de.peeeq.pscript.attributes.infrastructure;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.emf.ecore.EObject;






/**
 * dependencies of an attribute 
 */
public class AttributeDependencies {
	private AttributeOfNode a;
	private Set<AttributeOfNode> dependsOn = new HashSet<AttributeOfNode>();
	
	public AttributeDependencies(AttributeOfNode a) {
		if (a == null) throw new IllegalArgumentException();
		this.a = a;
	}

	public Iterable<AttributeOfNode> get() {
		return Collections.unmodifiableSet(dependsOn);
	}
	
	
	
}


class AttributeOfNode {
	private EObject node;
	private Class<? extends AbstractAttribute<? extends EObject, ?>> attr;
	
	public AttributeOfNode(EObject node, Class<? extends AbstractAttribute<? extends EObject, ?>> attr) {
		if (node == null) throw new IllegalArgumentException("node must not be null");
		if (attr == null) throw new IllegalArgumentException("attr must not be null");
		this.node = node;
		this.attr = attr;
	}

	public EObject getNode() {
		return node;
	}

	public Class<? extends AbstractAttribute<? extends EObject, ?>> getAttr() {
		return attr;
	}
	
	@Override
	public boolean equals(Object other) {
		if (other == null)
			return false;
		if (this == other)
			return true;
		if (this.getClass().equals(other.getClass())) {
			AttributeOfNode o = (AttributeOfNode) other;
			return node.equals(o.node) && attr.equals(o.attr);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return node == null ? 0 : node.hashCode() + 17 * (attr == null ? 0 : attr.hashCode());
	}
	
}