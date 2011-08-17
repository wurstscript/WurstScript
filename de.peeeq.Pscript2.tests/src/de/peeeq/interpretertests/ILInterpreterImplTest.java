package de.peeeq.interpretertests;

import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.Resource.Diagnostic;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.xtext.generator.IFileSystemAccess;
import org.eclipse.xtext.generator.IGenerator;
import org.eclipse.xtext.generator.JavaIoFileSystemAccess;
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
import de.peeeq.pscript.intermediateLang.interpreter.*;



public class ILInterpreterImplTest {
	
	@Inject 
	private Provider<ResourceSet> resourceSetProvider;
	
	@Inject 
	private IntermediateCodeGenerator iLconverter;
	
	@Inject
	private IResourceValidator validator;
	
	@Inject
	private IGenerator generator;
	
	@Inject 
	private JavaIoFileSystemAccess fileAccess;
	
	private final static String PSCRIPT_ENDING = ".pscript"; 
	
	static public void main(String ... args) throws IOException {
		
		Injector injector = new de.peeeq.pscript.PscriptStandaloneSetupGenerated().createInjectorAndDoEMFRegistration();
		ILInterpreterImplTest t = injector.getInstance(ILInterpreterImplTest.class);
		
		File dir = new File("./testscripts");
		
		boolean exists = dir.exists();
		if (exists) {
			System.out.println("Directory " + dir + " exists!");
		} else {
			System.out.println("Directory " + dir + " could not be found!");	
		}
		
		
		File[] fileList = dir.listFiles();
		List<File> pscriptFiles = new LinkedList<File>();
		
		int files = 0;
		if ( fileList != null ) {
			for(File f : fileList) {
				String name = f.getName().toLowerCase();
				if (name.endsWith(PSCRIPT_ENDING)) {
					pscriptFiles.add(f);
					System.out.println("File: " + name + " added.");
					files++;
				}
	
			}
		}
		
		int testsFailed = 0;
		int testCount = 0;
		System.out.println( "Found Files: " + files );
		for ( File file : pscriptFiles) {
			
			System.out.println( "----------------------------------------------");
			System.out.println( "Testing file: " + file );
			try {
				testCount++;
				t.runTest(file.getPath());
				System.out.println("Test did not succeed");
				testsFailed++;
			} catch (TestFailException e) {
				testsFailed++;
				System.out.println("Failed: " + e.getVal());
			} catch (TestSuccessException e) {
				System.out.println("Ok");
			} catch (Throwable e) {
				testsFailed++;
				System.err.println(file + " failed with exception.");
				e.printStackTrace();
			}
			System.out.println();
		}
		
		System.out.println(testsFailed + " of " + testCount + " Tests failed.");
		if (testsFailed == 0) {
			System.out.println("Everything went better than expected :)");
		}
		
	}
	
	
	private void runTest(String uri) {
		new org.eclipse.emf.mwe.utils.StandaloneSetup().setPlatformUri("../");
		
		
		System.out.println("parsing script ...");
		ResourceSet set = resourceSetProvider.get();
		Resource resource = set.getResource(URI.createURI(uri), true);
		
		if (resource.getErrors().size() > 0) {
			for (Diagnostic d : resource.getErrors()) {
				System.err.println(d);
			}
			throw new TestFailException("Found syntax errors.");
		}
		
		System.out.println("validating script ...");
		
		// validate the resource
		List<Issue> list = validator.validate(resource, CheckMode.ALL, CancelIndicator.NullImpl);
		if (!list.isEmpty()) {
			for (Issue issue : list) {
				System.out.println(issue);
			}
			throw new TestFailException("Script is not valid.");
		}
		
		System.out.println("translating script ...");
		ILprog prog = iLconverter.translateProg(resource );
		
		
		Pattern p = Pattern.compile("([a-zA-Z0-9_]+)\\.pscript$");
		Matcher m = p.matcher(uri);
		String filename = "unknown";
		if (m.find()) {
			filename = m.group(1);
		}


		// write to gen folder:
		fileAccess.setOutputPath("./testscripts/gen/"+filename +"/");
		generator.doGenerate(resource, fileAccess);
		
		
		ILInterpreter interpreter = new ILInterpreterImpl();
		// load the program:
		interpreter.LoadProgram(prog);
		//execute function test.foo
		ILconst result = interpreter.executeFunction("test_runTest");
		
	}

	
}