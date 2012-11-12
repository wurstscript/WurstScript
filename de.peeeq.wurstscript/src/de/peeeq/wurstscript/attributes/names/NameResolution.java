package de.peeeq.wurstscript.attributes.names;

import java.util.Collection;
import java.util.List;

import com.google.common.collect.Lists;

import de.peeeq.wurstscript.ast.Annotation;
import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.ast.ModuleInstanciation;
import de.peeeq.wurstscript.ast.NameDef;
import de.peeeq.wurstscript.ast.TypeDef;
import de.peeeq.wurstscript.ast.WPackage;
import de.peeeq.wurstscript.ast.WScope;
import de.peeeq.wurstscript.types.WurstType;
import de.peeeq.wurstscript.utils.Utils;

public class NameResolution {

	public static Collection<NameLink> lookupFuncs(AstElement node, String name, boolean showErrors) {
		if (node.attrNearestStructureDef() != null) {
			// inside a class one can write foo instead of this.foo()
			// so the receiver type is implicitly given by the enclosing class
			WurstType receiverType = node.attrNearestStructureDef().attrTyp();
			Collection<NameLink> funcs = node.lookupMemberFuncs(receiverType, name, showErrors);
			if (!funcs.isEmpty()) {
				return funcs;
			}
		}
		
		
		List<NameLink> result = Lists.newArrayList();
		WScope scope = node.attrNearestScope();
		while (scope != null) {
			for (NameLink n : scope.attrNameLinks().get(name)) {
				if (n.getType() == NameLinkType.FUNCTION
						&& n.getReceiverType() == null) {
					if (!result.contains(n)) {
						result.add(n);
					}
				}
			}
			scope = nextScope(scope);
		}
		return removeDuplicates(result);
	}

	private static Collection<NameLink> removeDuplicates(List<NameLink> nameLinks) {
		List<NameLink> result = Lists.newArrayList();
		nextLink: for (NameLink nl : nameLinks) {
			for (NameLink other : result) {
				if (other.getNameDef() == nl.getNameDef()) {
					continue nextLink;
				}
			}
			result.add(nl);
		}
		return result;
	}

	private static WScope nextScope(WScope scope) {
		AstElement parent = scope.getParent();
		if (parent == null) {
			return null;
		}
		if (scope instanceof ModuleInstanciation) {
			ModuleInstanciation moduleInstanciation = (ModuleInstanciation) scope;
			// for module instanciations the next scope is the package in which
			// the module was defined
			return nextScope(moduleInstanciation.attrModuleOrigin());
		}
		scope = parent.attrNearestScope();
		return scope;
	}

	public static Collection<NameLink> lookupMemberFuncs(AstElement node, WurstType receiverType, String name, boolean showErrors) {
		List<NameLink> result = Lists.newArrayList();
		WScope scope = node.attrNearestScope();
		while (scope != null) {
			for (NameLink n : scope.attrNameLinks().get(name)) {
				if (n.getType() == NameLinkType.FUNCTION
						&& n.getReceiverType() != null
						&& n.getReceiverType().isSupertypeOf(receiverType, node)) {
					result.add(n);
				}
			}
			scope = nextScope(scope);
		}
		return removeDuplicates(result);
	}
	
	public static NameDef lookupVar(AstElement node, String name, boolean showErrors) {
		WurstType receiverType;
		if (node.attrNearestStructureDef() != null) {
			// inside a class one can write bar instead of this.bar
			// so the receiver type is implicitly given by the enclosing class
			receiverType = node.attrNearestStructureDef().attrTyp();
		} else {
			receiverType = null;
		}
		
		NameLink privateCandidate = null;
		List<NameLink> candidates = Lists.newArrayList();
		
		
		
		WScope scope = node.attrNearestScope();
		while (scope != null) {
//			System.out.println("searching " + receiverType + "." + name + " in scope " + Utils.printElement(scope));
//			System.out.println("		" + scope.attrNameLinks());
			
			for (NameLink n : scope.attrNameLinks().get(name)) {
				if (n.getType() == NameLinkType.VAR
						&& (n.getReceiverType() == null
						|| (receiverType != null && receiverType.isSubtypeOf(n.getReceiverType(), node))
						)) {
					
					if (n.getVisibility() != Visibility.PRIVATE_OTHER 
						&& n.getVisibility() != Visibility.PROTECTED_OTHER) {
						candidates.add(n);
					} else if (privateCandidate == null) {
						privateCandidate = n;
					}
					 
				}
			}
			if (candidates.size() > 0) {
				if (showErrors && candidates.size() > 1) {
					node.addError("Reference to variable " + name + " is ambiguous. Alternatives are:\n" 
							+ Utils.printAlternatives(candidates));
				}
				return candidates.get(0).getNameDef();
			}
			scope = nextScope(scope);
		}
		if (showErrors) {
			if (privateCandidate == null) {
				node.addError("Could not find variable " + name + ".");
			} else {
				node.addError(Utils.printElementWithSource(privateCandidate.getNameDef()) + " is not visible here." +
						" Try to make it public.");
				return privateCandidate.getNameDef();
			}
		}
		return null;
	}
	
	public static NameDef lookupMemberVar(AstElement node, WurstType receiverType, String name, boolean showErrors) {
//		System.out.println("lookupMemberVar " + receiverType+"."+name);
		WScope scope = node.attrNearestScope();
		while (scope != null) {
			for (NameLink n : scope.attrNameLinks().get(name)) {
//				System.out.println("	- " + n);
				if (n.getType() == NameLinkType.VAR
						&& n.getReceiverType() != null
						&& n.getReceiverType().isSupertypeOf(receiverType, node)) {
					return n.getNameDef(); 
				}
			}
			scope = nextScope(scope);
		}
		return null;
	}
	
	public static TypeDef lookupType(AstElement node, String name, boolean showErrors) {
		
		NameLink privateCandidate = null;
		List<NameLink> candidates = Lists.newArrayList();
		
		WScope scope = node.attrNearestScope();
		while (scope != null) {
			for (NameLink n : scope.attrTypeNameLinks().get(name)) {
				if (n.getNameDef() instanceof TypeDef) {
					TypeDef typeDef = (TypeDef) n.getNameDef();
					if (n.getVisibility() != Visibility.PRIVATE_OTHER
							&& n.getVisibility() != Visibility.PROTECTED_OTHER) {
						candidates.add(n);
					} else if (privateCandidate != null) {
						privateCandidate = n;
					}
				}
			}
			if (candidates.size() > 0) {
				if (showErrors && candidates.size() > 1) {
					node.addError("Reference to type " + name + " is ambiguous. Alternatives are:\n" 
							+ Utils.printAlternatives(candidates));
				}
				return (TypeDef) candidates.get(0).getNameDef();
			}
			scope = nextScope(scope);
		}
		if (showErrors) {
			if (privateCandidate == null) {
				node.addError("Could not find type " + name + ".");
			} else {
				node.addError(Utils.printElementWithSource(privateCandidate.getNameDef()) + " is not visible here." +
						" Try to make it public.");
				return (TypeDef) privateCandidate.getNameDef();
			}
		}
		return null;
	}

	public static WPackage lookupPackage(AstElement node, String name, boolean showErrors) {
		WScope scope = node.attrNearestScope();
		while (scope != null) {
			for (NameLink n : scope.attrTypeNameLinks().get(name)) {
				if (n.getNameDef() instanceof WPackage) {
					return (WPackage) n.getNameDef();
				}
			}
			scope = nextScope(scope);
		}
		return null;
	}

	public static Collection<NameLink> lookupFuncsShort(AstElement elem, String name) {
		return lookupFuncs(elem, name, true);
	}

	public static Collection<NameLink> lookupMemberFuncsShort(AstElement elem, WurstType receiverType, String name) {
		return lookupMemberFuncs(elem, receiverType, name, true);
	}
	
	public static NameDef lookupVarShort(AstElement node, String name) {
		return lookupVar(node, name, true);
	}
	
	public static NameDef lookupMemberVarShort(AstElement node, WurstType receiverType, String name) {
		return lookupMemberVar(node, receiverType, name, true);
	}
	
	public static TypeDef lookupTypeShort(AstElement node, String name) {
		return lookupType(node, name, true);
	}

	public static WPackage lookupPackageShort(AstElement node, String name) {
		return lookupPackage(node, name, true);
	}
}
