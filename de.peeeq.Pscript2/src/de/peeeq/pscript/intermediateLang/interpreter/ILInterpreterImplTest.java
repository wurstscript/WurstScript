package de.peeeq.pscript.intermediateLang.interpreter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceImpl;
import org.eclipse.xtext.xtend2.lib.StringConcatenation;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;

import de.peeeq.pscript.PscriptRuntimeModule;
import de.peeeq.pscript.generator.Main;
import de.peeeq.pscript.intermediateLang.ILStatement;
import de.peeeq.pscript.intermediateLang.ILconst;
import de.peeeq.pscript.intermediateLang.ILconstInt;
import de.peeeq.pscript.intermediateLang.ILfunction;
import de.peeeq.pscript.intermediateLang.ILprog;
import de.peeeq.pscript.intermediateLang.ILvar;
import de.peeeq.pscript.intermediateLang.IntermediateCodeGenerator;
import de.peeeq.pscript.types.PScriptTypeBool;
import de.peeeq.pscript.types.PScriptTypeInt;
import de.peeeq.pscript.types.PscriptType;


public class ILInterpreterImplTest {
	
	@Inject 
	private Provider<ResourceSet> resourceSetProvider;
	
	@Inject 
	private IntermediateCodeGenerator iLconverter;
	
	static public void main(String ... args) throws IOException {
		
		Injector injector = new de.peeeq.pscript.PscriptStandaloneSetupGenerated().createInjectorAndDoEMFRegistration();
		ILInterpreterImplTest t = injector.getInstance(ILInterpreterImplTest.class);
		
		t.runTest("file://C:/Users/Frotty/Documents/PScript/de.peeeq.Pscript2/src/de/peeeq/pscript/intermediateLang/interpreter/test.pscript");
		
		
	}
	
	private void runTest(String uri) {
		ResourceSet set = resourceSetProvider.get();
		Resource resource = set.getResource(URI.createURI(uri), true);
		ILprog prog = iLconverter.translateProg(resource );
		
		
		ILInterpreter interpreter = new ILInterpreterImpl();
		// load the program:
		interpreter.LoadProgram(prog);
		// execute function test.foo
		ILconst result = interpreter.executeFunction("test_foo", new ILconstInt(4));
		
		System.out.println("result = " + result);
		
	}

	private static InputStream testProg1() {
		String prog = 
		"package test { \n" +
			"int foo(x: Int) { \n" +
				"if x < 3 { \n" +
					"return x*3 + 4 \n" +
				"} else { \n" +
					"return x-1 \n" +
				"} \n" +
			"} \n" +
		"}";
		
		
		return new ByteArrayInputStream(prog.getBytes());
	}

	// example function 
//	private static ILfunction functionFoo() {

//		
//		List<ILvar> params = new LinkedList<ILvar>();
//		// Parameter integer x
//		params.add(new ILvar("x", PScriptTypeInt.instance()));
//		// returns integer
//		PscriptType returnType = PScriptTypeInt.instance();
//		
//		List<ILvar> localsWithParams = new LinkedList<ILvar>();
//		localsWithParams.addAll(params);
//		
//		// local var integer y
//		localsWithParams.add(new ILvar("y", PScriptTypeInt.instance()));
//		// local var integer z
//		localsWithParams.add(new ILvar("z", PScriptTypeInt.instance()));
//		// local var boolean b
//		localsWithParams.add(new ILvar("b", PScriptTypeBool.instance()));
//		
//		List<ILStatement> body = new LinkedList<ILStatement>();
//		
//		List<ILStatement> thenBlock = new LinkedList<ILStatement>();
//		
//		List<ILStatement> eleBlock = new LinkedList<ILStatement>();
//		
//		
//		return new ILfunction("foo", params, returnType, localsWithParams, body);;
//	}
	
}