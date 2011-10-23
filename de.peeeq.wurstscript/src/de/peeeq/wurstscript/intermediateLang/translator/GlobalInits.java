package de.peeeq.wurstscript.intermediateLang.translator;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.common.base.Function;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

import de.peeeq.wurstscript.ast.Expr;
import de.peeeq.wurstscript.intermediateLang.ILvar;
import de.peeeq.wurstscript.utils.NotNullList;
import de.peeeq.wurstscript.utils.TopsortCycleException;
import de.peeeq.wurstscript.utils.Utils;

public class GlobalInits {
	
	
	
	List<GlobalInit> inits = new NotNullList<GlobalInit>();
	Map<ILvar, GlobalInit> initsMap = new HashMap<ILvar, GlobalInit>();
	SetMultimap<ILvar, ILvar> dependsOn = HashMultimap.create();
	
	public void addDependency(ILvar v, ILvar dependsOn2) {
		if (v == null) throw new IllegalArgumentException();
		if (dependsOn2 == null) throw new IllegalArgumentException();
		dependsOn.put(v, dependsOn2);
	}

	public void add(ILvar v, Expr initialExpr) {
		GlobalInit g = new GlobalInit(v, initialExpr);
		inits.add(g);
		initsMap.put(v, g);
	}
	
	public List<GlobalInit> getSortedGlobalInits() throws TopsortCycleException {
		List<GlobalInit> sorted = Utils.topSort(inits, new Function<GlobalInit, Collection<GlobalInit>>() {

			@Override
			public Collection<GlobalInit> apply(GlobalInit input) {
				if (input == null) throw new IllegalArgumentException();
				Collection<GlobalInit> result = new LinkedList<GlobalInit>();
				for (ILvar var : dependsOn.get(input.v)) {
					GlobalInit r = initsMap.get(var);
					if (r != null) {
						result.add(r);
					} else {
						// do we have a dependecy to an unitialized variable?
						throw new Error("Dependecy to uninitialized variable " + var + " from variable " + input);
					}
				}
				return result;
			}
			
			
		});
		
		return sorted;
	}
	
	
}
