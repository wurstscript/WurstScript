package de.peeeq.pscript.scoping;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtext.naming.QualifiedName;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.scoping.IScope;

import de.peeeq.pscript.attributes.AttrTypeExprType;
import de.peeeq.pscript.attributes.infrastructure.AttributeManager;
import de.peeeq.pscript.pscript.FuncDef;
import de.peeeq.pscript.pscript.ParameterDef;
import de.peeeq.pscript.pscript.VarDef;
import de.peeeq.pscript.types.PScriptTypeBool;
import de.peeeq.pscript.types.PscriptType;
import de.peeeq.pscript.utils.NotNullList;

public class ClassMemberScope implements IScope {

	private IScope parentScope;
	private boolean complete;
	private PscriptType[] argumentTypes;
	private AttributeManager attributeManager;

	@Override
	public String toString() {
		String r = "FunctionScope for ";
		for (PscriptType t : argumentTypes) {
			r += t.toString() + ", ";
		}
		return r;
	}


	public ClassMemberScope(AttributeManager attributeManager, IScope parentScope, 
			boolean complete, PscriptType ... argumentTypes) {
		this.attributeManager = attributeManager;
		this.parentScope = parentScope;
		this.complete = complete;
		this.argumentTypes = argumentTypes;
	}

	@Override
	public IEObjectDescription getSingleElement(QualifiedName name) {
		//		Iterable<IEObjectDescription> elements = getAllElements();
		//		for (IEObjectDescription o : elements) {
		//			if (o.getEObjectOrProxy() instanceof FuncDef) {
		//				FuncDef f = (FuncDef) o.getEObjectOrProxy();
		//				if (f.getName().equals(name.toString())) {
		//					return o;
		//				}
		//			}
		//		}
		//		return null;
		return firstOrNull(getElements(name));
	}

	@Override
	public List<IEObjectDescription> getElements(QualifiedName name) {
		//		Iterable<IEObjectDescription> elements = getAllElements();
		//		LinkedList<IEObjectDescription> result = new LinkedList<IEObjectDescription>();
		//		for (IEObjectDescription o : elements) {
		//			if (o.getEObjectOrProxy() instanceof FuncDef) {
		//				FuncDef f = (FuncDef) o.getEObjectOrProxy();
		//				if (f.getName().equals(name.toString())) {
		//					result.add(o);
		//				}
		//			}
		//		}
		//		return result;
		return applyFilter(parentScope.getElements(name));
	}

	@Override
	public IEObjectDescription getSingleElement(EObject object) {
		//		Iterable<IEObjectDescription> elements = getAllElements();
		//		for (IEObjectDescription o : elements) {
		//			if (o.getEObjectOrProxy() instanceof FuncDef) {
		//				FuncDef f = (FuncDef) o.getEObjectOrProxy();
		//				if (f.equals(object)) {
		//					return o;
		//				}
		//			}
		//		}
		//		return null;
		return firstOrNull(getElements(object));
	}

	private IEObjectDescription firstOrNull(List<IEObjectDescription> elements) {
		if (elements.size() == 0) {
			return null;
		}
		return elements.get(0);
	}

	@Override
	public List<IEObjectDescription> getElements(EObject object) {
		//		Iterable<IEObjectDescription> elements = getAllElements();
		//		LinkedList<IEObjectDescription> result = new LinkedList<IEObjectDescription>();
		//		for (IEObjectDescription o : elements) {
		//			if (o.getEObjectOrProxy() instanceof FuncDef) {
		//				FuncDef f = (FuncDef) o.getEObjectOrProxy();
		//				if (f.equals(object)) {
		//					result.add(o);
		//				}
		//			}
		//		}
		//		return result;
		return applyFilter(parentScope.getElements(object));
	}

	@Override
	public Iterable<IEObjectDescription> getAllElements() {
		Iterable<IEObjectDescription> elements = parentScope.getAllElements();	
		return applyFilter(elements);
	}

	/**
	 * filter out all elements which
	 * 	- are no functions
	 *  - have other name
	 *  - have wrong parameter types
	 * @param elements
	 * @return
	 */
	private List<IEObjectDescription> applyFilter(Iterable<IEObjectDescription> elements) {
		List<IEObjectDescription> elements1 = getFunctionsAndVars(elements);

		List<IEObjectDescription> elements2 = filterByTypes(elements1);
		if (elements2.size() == 0) {
			// if no elements were found take the old values:
			//			elements2 = elements1;
		}



		return elements2;
	}




	private List<IEObjectDescription> filterByTypes(
			List<IEObjectDescription> elements) {
		LinkedList<IEObjectDescription> result = new NotNullList<IEObjectDescription>();
		for (IEObjectDescription o : elements) {
			EObject obj = o.getEObjectOrProxy();
			
			System.out.println("object = " + obj);
			
			if (obj instanceof FuncDef) {
				FuncDef f  = (FuncDef) obj;
				System.out.println("function = " + f.getName());
				if (f.getParameters() != null) {
					System.out.println("params: " + f.getParameters().size());
					for (VarDef p : f.getParameters()) {
						System.out.println("	param: " + p.getType() + p.getName());
					}
				}
				if (f.getParameters().size() >= argumentTypes.length) {
					if (!complete || f.getParameters().size() == argumentTypes.length) {
						boolean parametersMatch = true;
						for (int i=0; i < argumentTypes.length; i++) {
							ParameterDef param = (ParameterDef) f.getParameters().get(i);
							PscriptType paramType = attributeManager.getAttValue(AttrTypeExprType.class, param.getType());
							if (!argumentTypes[i].isSubtypeOf(paramType)) {
								parametersMatch = false;
								break;		
							}
						}
						if (parametersMatch) {
							System.out.println("  ok");
							result.add(o);
						}
					}
				}
			} else if (obj instanceof VarDef) {
				// TODO ...
			}
		}
		return result;
	}


	private List<IEObjectDescription> getFunctions(
			Iterable<IEObjectDescription> elements) {
		LinkedList<IEObjectDescription> result = new NotNullList<IEObjectDescription>();
		for (IEObjectDescription o : elements) {
			if (o.getEObjectOrProxy() instanceof FuncDef) {
				result.add(o);
			}
		}
		return result;
	}

	private List<IEObjectDescription> getFunctionsAndVars(
			Iterable<IEObjectDescription> elements) {
		LinkedList<IEObjectDescription> result = new NotNullList<IEObjectDescription>();
		for (IEObjectDescription o : elements) {
			EObject obj = o.getEObjectOrProxy();
			// TODO enrich object descriptions with user data
			System.out.println("obj = " + obj);
			if (obj.eIsProxy()) {
				ResourceSet res = null;
				obj = EcoreUtil.resolve(obj, res);
				System.out.println("resolved -> obj = " + obj);
			}
			
			if (obj instanceof FuncDef || obj instanceof VarDef) {
				result.add(o);
			}
		}
		return result;
	}

}
