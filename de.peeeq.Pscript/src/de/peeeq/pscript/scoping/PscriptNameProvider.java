package de.peeeq.pscript.scoping;

import java.util.Iterator;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.naming.IQualifiedNameProvider;

import de.peeeq.pscript.pscript.*;
import de.peeeq.pscript.pscript.util.PscriptSwitch;

public class PscriptNameProvider extends IQualifiedNameProvider.AbstractImpl {

	@Override
	public String getQualifiedName(EObject obj) {
		return new PscriptSwitch<String>() {
			@Override
			public String casePackageDeclaration(PackageDeclaration p) {
				return p.getName();
			}

			@Override
			public String caseNameDef(NameDef v) {
				StringBuilder sb = new StringBuilder();
				// recursively add name of my parent
				//sb.append(doSwitch(v.eContainer()));
				//sb.append(".");
				sb.append(v.getName());
				return sb.toString();
			}
			
			@Override
			public String caseVarDef(VarDef v) {
				StringBuilder sb = new StringBuilder();
				// recursively add name of my parent
				sb.append(doSwitch(v.eContainer()));
				sb.append(".");
				sb.append(v.getName());
				return sb.toString();
			}
			
			
			
//			@Override
//			public String caseFuncDef(FuncDef f) {
//				StringBuilder sb = new StringBuilder();
//				// recursively add name of my parent
//				sb.append(doSwitch(f.eContainer()));
//				sb.append(".");
//				sb.append(f.getName());
//				sb.append("(");
//				boolean first = true;
//				for (NameDef nd : f.getParameters()) {
//					if (!first) {
//						sb.append(",");
//						first = false;
//					}
//					ParameterDef pd = (ParameterDef) nd;
//					sb.append(pd.getType()); // TODO doSwitch on Type?
//				}
//				sb.append(")");
//				return sb.toString();
//			}
			
			
		}.doSwitch(obj);
	}

}
