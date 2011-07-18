package de.peeeq.pscript.intermediateLang.interpreter;

import java.io.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.xtext.util.CancelIndicator;
import org.eclipse.xtext.validation.CheckMode;
import org.eclipse.xtext.validation.IResourceValidator;
import org.eclipse.xtext.validation.Issue;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;

import de.peeeq.pscript.intermediateLang.ILconst;
import de.peeeq.pscript.intermediateLang.ILconstInt;
import de.peeeq.pscript.intermediateLang.ILprog;
import de.peeeq.pscript.intermediateLang.IntermediateCodeGenerator;


public class ILInterpreterImplTest {
	
	@Inject 
	private Provider<ResourceSet> resourceSetProvider;
	
	@Inject 
	private IntermediateCodeGenerator iLconverter;
	
	@Inject
	private IResourceValidator validator;
	
	private final static String PSCRIPT_ENDING = ".pscript"; 
	
	static public void main(String ... args) throws IOException {
		
		Injector injector = new de.peeeq.pscript.PscriptStandaloneSetupGenerated().createInjectorAndDoEMFRegistration();
		ILInterpreterImplTest t = injector.getInstance(ILInterpreterImplTest.class);
		
		File dir = new File("./src/de/peeeq/pscript/intermediateLang/interpreter");
		
		boolean exists = dir.exists();
		if (exists) {
			System.out.println("Directory exists!");
		} else {
			System.out.println("Directory could not be found!");	
		}
		
		
		File[] fileList = dir.listFiles();
		File[] pscriptFiles = new File[50];
		
		int files = 0;
		if ( fileList != null ) {
			for(File f : fileList) {
				String name = f.getName().toLowerCase();
				if (name.endsWith(PSCRIPT_ENDING)) {
					pscriptFiles[files] = f;
					System.out.println("File: " + name + " added.");
					files++;
				}
	
			}
		}
		System.out.println( "Found Files: " + files );
		try {
			for ( int i = 0; i < files; i++) {
				t.runTest(pscriptFiles[i].getPath());
			}
		}catch (Error e) {
			System.out.println("Failed.");
		}
		
		//t.runTest("platform:/resource/de.peeeq.Pscript2/src/de/peeeq/pscript/intermediateLang/interpreter/test.pscript");
		
		
	}
	
	
	private void runTest(String uri) {
		new org.eclipse.emf.mwe.utils.StandaloneSetup().setPlatformUri("../");
		
		ResourceSet set = resourceSetProvider.get();
		Resource resource = set.getResource(URI.createURI(uri), true);
		
		// validate the resource
		List<Issue> list = validator.validate(resource, CheckMode.ALL, CancelIndicator.NullImpl);
		if (!list.isEmpty()) {
			for (Issue issue : list) {
				System.err.println(issue);
			}
			return;
		}
		
		
		ILprog prog = iLconverter.translateProg(resource );
		
		
		ILInterpreter interpreter = new ILInterpreterImpl();
		// load the program:
		interpreter.LoadProgram(prog);
		// execute function test.foo
//		ILconst result = interpreter.executeFunction("test_foo", new ILconstInt(4),new ILconstInt(0));
//
//		System.out.println("result = " + result);
		
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