/*******************************************************************************
 * Copyright (c) 2011 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package de.peeeq.pscript.scoping;

import org.eclipse.xtext.naming.DefaultDeclarativeQualifiedNameProvider;
import org.eclipse.xtext.naming.IQualifiedNameConverter;
import org.eclipse.xtext.naming.QualifiedName;

import com.google.inject.Inject;

import de.peeeq.pscript.pscript.FuncDef;


public class PscriptQualifiedNameProvider extends DefaultDeclarativeQualifiedNameProvider {

//	@Inject
//	private IQualifiedNameConverter converter;
//	
//	QualifiedName qualifiedName(FuncDef funcDef) {
//		System.out.println("name = " + funcDef.getName());
//		return converter.toQualifiedName(funcDef.getName() + "___" + funcDef.getParameters().size());
//	}
}
