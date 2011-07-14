package de.peeeq.switchgenerator;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.mwe2.runtime.workflow.IWorkflowComponent;
import org.eclipse.emf.mwe2.runtime.workflow.IWorkflowContext;
import org.eclipse.xtext.generator.JavaIoFileSystemAccess;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterators;
import com.google.common.collect.Multimap;

import de.peeeq.pscript.utils.NotNullList;



/**
 * generate switch classes for all types in the ecore model 
 */
public class SwitchGenerator implements IWorkflowComponent  {
	
	private String uri;
	private String targetPackage = "de.peeeq.pscript.pscript.util";
	private String importPackage = "de.peeeq.pscript.pscript.*";
	
	
	private JavaIoFileSystemAccess fileAccess;
	
	
	public SwitchGenerator() {
		fileAccess = new JavaIoFileSystemAccess();
		fileAccess.setOutputPath("src-gen/");
	}
	
	public void setUri(String uri) {
	    this.uri = uri;
	  }
	  public String getUri() {
	    return uri;
	  }
	
	public void generate() {
		ResourceSet resourceSet = new ResourceSetImpl();
	    URI fileURI = URI.createFileURI(uri); 
	    
	    Resource resource = resourceSet.getResource(fileURI, true);
//	    EcoreUtil.resolveAll(resource);
	    Iterator<Object> contents = EcoreUtil.getAllContents(resource, true);
	    
	    Iterator<EClass> iter = Iterators.filter(contents, EClass.class);
	    
	    List<EClass> classes = new NotNullList<EClass>();
	    Map<String, EClass> nameToClass = new HashMap<String, EClass>();
	    while(iter.hasNext()) {
	    	EClass c = iter.next();
	    	System.out.println(c.getName());
	    	classes.add(c);
	    	nameToClass.put(c.getName(), c);
	    }
	    
	    Multimap<EClass, EClass> subtypes = HashMultimap.create();
	    for (EClass c1 : classes) {
	    	for (EClass c2 : c1.getEAllSuperTypes()) {
	    		
	    		// hack (do not why this does not work normally, used to work before but now it cannot resolve supertypes)
	    		String c2Name = c2.toString();
	    		int startPos = c2Name.indexOf("#//");
	    		c2Name = c2Name.substring(startPos+3, c2Name.length() -1);
	    		
	    		c2 = nameToClass.get(c2Name);
	    		
	    		subtypes.put(c2, c1);
	    	}
	    }
	    
	    for (EClass c : classes) {
	    	generateSwitchClass(c, subtypes.get(c));
	    }
	    
	}

		
	private void generateSwitchClass(EClass eclass, Collection<EClass> subClassesSet) {
		List<EClass> subClasses = new NotNullList<EClass>();
		subClasses.addAll(subClassesSet);
		subClasses.remove(eclass); // remove self from subclasses
		
		// sort functions by hierarchy (mandatory to get the match right)
		Collections.sort(subClasses, new Comparator<EClass>() {

			@Override
			public int compare(EClass arg0, EClass arg1) {
				// TODO probably does not work (-> isSuperTypeOf)
				if (arg0.isSuperTypeOf(arg1)) return 1;
				if (arg1.isSuperTypeOf(arg0)) return -1;
				return 0;
			}
			
		});
		
		if (subClasses.size() == 0) {
			// we do not need a switch if we have no case
			return;
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append("package " + targetPackage + ";\n");
		sb.append("import " + importPackage  + ";\n");
		sb.append("public abstract class ");
		sb.append(eclass.getName());
		sb.append("Switch <T> {\n");
		for (EClass subtype : subClasses) {
			sb.append("	abstract public T case");
			sb.append(subtype.getName());
			sb.append("(");
			sb.append(subtype.getName());
			sb.append(" ");
			sb.append(toFirstLower(subtype.getName()));
			sb.append(");\n");
		}
		
		sb.append("	public T doSwitch(");
		sb.append(eclass.getName());
		sb.append(" ");
		String argumentName = toFirstLower(eclass.getName());
		sb.append(argumentName);
		sb.append(") {\n");
		for (EClass subtype: subClasses) {
			sb.append("		if (");
			sb.append(argumentName);
			sb.append(" instanceof ");
			sb.append(subtype.getName());
			sb.append(") return case");
			sb.append(subtype.getName());
			sb.append("((");
			sb.append(subtype.getName());
			sb.append(")");
			sb.append(argumentName);
			sb.append(");\n");
		}
		sb.append("		throw new Error(\"Switch did not match any case.\");\n");
		sb.append("	}\n");
		sb.append("}\n\n");
		System.out.println(sb);
		fileAccess.generateFile(toPath(targetPackage) + eclass.getName()+"Switch.java", sb);
		
		
		
		// another class for type void:
		sb = new StringBuilder();
		sb.append("package " + targetPackage + ";\n");
		sb.append("import " + importPackage  + ";\n");
		sb.append("public abstract class ");
		sb.append(eclass.getName());
		sb.append("SwitchVoid {\n");
		for (EClass subtype : subClasses) {
			sb.append("	abstract public void case");
			sb.append(subtype.getName());
			sb.append("(");
			sb.append(subtype.getName());
			sb.append(" ");
			sb.append(toFirstLower(subtype.getName()));
			sb.append(");\n");
		}
		
		sb.append("	public void doSwitch(");
		sb.append(eclass.getName());
		sb.append(" ");
		argumentName = toFirstLower(eclass.getName());
		sb.append(argumentName);
		sb.append(") {\n");
		for (EClass subtype: subClasses) {
			sb.append("		if (");
			sb.append(argumentName);
			sb.append(" instanceof ");
			sb.append(subtype.getName());
			sb.append(") { case");
			sb.append(subtype.getName());
			sb.append("((");
			sb.append(subtype.getName());
			sb.append(")");
			sb.append(argumentName);
			sb.append("); return; }\n");
		}
		sb.append("		throw new Error(\"Switch did not match any case.\");\n");
		sb.append("	}\n");
		sb.append("}\n\n");
		System.out.println(sb);
		fileAccess.generateFile(toPath(targetPackage) + eclass.getName()+"SwitchVoid.java", sb);
		
	}
	private String toPath(String packageName) {
		return packageName.replace(".", "/") + "/";
	}

	private String toFirstLower(String name) {
		return name.substring(0,1).toLowerCase() + name.substring(1);
	}
	@Override
	public void invoke(IWorkflowContext arg0) {
		generate();
		
	}
	@Override
	public void postInvoke() {
	}
	@Override
	public void preInvoke() {
	}
	

}
