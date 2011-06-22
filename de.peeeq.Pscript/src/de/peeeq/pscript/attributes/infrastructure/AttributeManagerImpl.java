package de.peeeq.pscript.attributes.infrastructure;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.impl.AdapterImpl;
import org.eclipse.emf.ecore.EObject;


public class AttributeManagerImpl implements AttributeManager {

	private Map<Object, Map<Class<? extends AbstractAttribute>, Object>> cache = new HashMap<Object, Map<Class<? extends AbstractAttribute>,Object>>();

	
	public void reset(){
		// clear cache
		cache =  new HashMap<Object, Map<Class<? extends AbstractAttribute>,Object>>();
	}
	
	public <N extends EObject,T> T getAttValue(Class<? extends AbstractAttribute<N,T>> attr, N node) {
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
					cache.put(node, new HashMap<Class<? extends AbstractAttribute>, Object>());
				}
				cache.get(node).put(attr, result);
			}
			
			
			
			
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;		
	}

	private void addDependencies(AttributeDependencies dependencies) {
		// TODO dependencies used for caching
	}
}
