package de.peeeq.wurstscript.types;

import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;

import com.google.common.collect.Multimap;

import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.ast.ClassDef;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.InstanceDef;
import de.peeeq.wurstscript.ast.InterfaceDef;
import de.peeeq.wurstscript.ast.NamedScope;
import de.peeeq.wurstscript.ast.PackageOrGlobal;
import de.peeeq.wurstscript.ast.WEntity;
import de.peeeq.wurstscript.ast.WPackage;
import de.peeeq.wurstscript.utils.Utils;


public class PscriptTypeClass extends PscriptTypeNamedScope {

	ClassDef classDef;

	public PscriptTypeClass(ClassDef classDef, boolean staticRef) {
		super(staticRef);
		this.classDef = classDef;
	}

	public PscriptTypeClass(ClassDef classDef2, List<PscriptType> newTypes) {
		super(newTypes);
		this.classDef = classDef2;
	}

	@Override
	public boolean isSubtypeOf(PscriptType obj, AstElement location) {
		if (super.isSubtypeOf(obj, location)) {
			return true;
		}
		if (obj instanceof PscriptTypeInterface) {
			PscriptTypeInterface pti = (PscriptTypeInterface) obj;
			// TODO check if this class is a subtype of the interface
			PackageOrGlobal pack = location.attrNearestPackage();
			
			Collection<InstanceDef> instanceDefs = pack.attrInstanceDefs().get(pti.getInterfaceDef());
			for (WEntity e : ((WPackage)pack).getElements()) {
				System.out.println(Utils.printElement(e));
			}
			for (Entry<InterfaceDef, InstanceDef> e : pack.attrInstanceDefs().entries()) {
				System.out.println(Utils.printElement(e.getKey()) + " => " + e.getValue());
			}
			for (InstanceDef iDef: instanceDefs) {
				if (classDef == iDef.getClassTyp().attrTypeDef()) {
					return true;
				}
			}
		}
		return false;
	}
	
	@Override
	public NamedScope getDef() {
		return classDef;
	}

	public ClassDef getClassDef() {
		return classDef;
	}
	
	@Override
	public String getName() {
		return getDef().getName() + printTypeParams() + " (class)";
	}
	
	@Override
	public PscriptType dynamic() {
		if (isStaticRef()) {
			return new PscriptTypeClass(getClassDef(), false);
		}
		return this;
	}

	@Override
	public PscriptType replaceTypeVars(List<PscriptType> newTypes) {
		return new PscriptTypeClass(classDef, newTypes);
	}
	
}
