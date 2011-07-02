package de.peeeq.pscript.scoping;

import java.util.Iterator;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.naming.IQualifiedNameConverter;
import org.eclipse.xtext.naming.IQualifiedNameProvider;
import org.eclipse.xtext.naming.QualifiedName;

import com.google.inject.Inject;

import de.peeeq.pscript.pscript.*;
import de.peeeq.pscript.pscript.util.PscriptSwitch;

public class PscriptNameProvider extends IQualifiedNameProvider.AbstractImpl {

	@Inject 
	IQualifiedNameConverter qNameProvider;
	

	@Override
	public QualifiedName getFullyQualifiedName(EObject obj) {
		return new PscriptSwitch<QualifiedName>() {
			@Override
			public QualifiedName casePackageDeclaration(PackageDeclaration p) {
				return qNameProvider.toQualifiedName(p.getName());
			}

			@Override
			public QualifiedName caseNameDef(NameDef v) {
				return qNameProvider.toQualifiedName(v.getName());
			}
			
			@Override
			public QualifiedName caseVarDef(VarDef v) {
				StringBuilder sb = new StringBuilder();
				// recursively add name of my parent
				sb.append(doSwitch(v.eContainer()));
				sb.append(".");
				sb.append(v.getName());
				return qNameProvider.toQualifiedName(sb.toString());
			}
		}.doSwitch(obj);
	}
}
