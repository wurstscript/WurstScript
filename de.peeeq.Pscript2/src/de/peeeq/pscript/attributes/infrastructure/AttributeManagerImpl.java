package de.peeeq.pscript.attributes.infrastructure;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.ecore.EObject;


public class AttributeManagerImpl implements AttributeManager {

	private Map<Object, Map<Class<? extends AbstractAttribute<?,?>>, Object>> cache = new HashMap<Object, Map<Class<? extends AbstractAttribute<?,?>>,Object>>();

	
	public void reset(){
		// clear cache
		cache =  new HashMap<Object, Map<Class<? extends AbstractAttribute<?,?>>,Object>>();
	}
	
	public <N extends EObject,T> T getAttValue(Class<? extends AbstractAttribute<N,T>> attr, N node) {
		if (node == null) throw new IllegalArgumentException("node must not be null");
		
		// TODO cache
		T result = null;
		
		try {
			
			AttributeDependencies dependencies = new AttributeDependencies(new AttributeOfNode(node, attr));
			
			// check if attribute is already calculated
			if (cache.containsKey(node) && cache.get(node).containsKey(attr)) {
				result = (T) cache.get(node).get(attr);
			} else {
				// calculate value of attribute:
				AbstractAttribute<N, T> attrInstance = attr.newInstance();
				result  = attrInstance.calculate(this, dependencies, node);	
				addDependencies(dependencies);
				
				// insert into cache:
				if (!cache.containsKey(node)) {
					cache.put(node, new HashMap<Class<? extends AbstractAttribute<?,?>>, Object>());
				}
				cache.get(node).put(attr, result);
			}
			
			
			
			
		} catch (InstantiationException e) {
			throw new Error("Could not create Attribute " + attr.getName(), e);
		} catch (IllegalAccessException e) {
			throw new Error("Could not create Attribute " + attr.getName(), e);
		}
		return result;		
	}

	private void addDependencies(AttributeDependencies dependencies) {
		// TODO dependencies used for caching
	}
}
