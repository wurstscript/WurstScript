package de.peeq.test


import org.eclipse.xtext.junit4.InjectWith
import org.junit.runner.RunWith
import de.peeeq.pscript.PscriptInjectorProvider
import org.eclipse.xtext.junit4.XtextRunner
import com.google.inject.Inject
import org.eclipse.xtext.junit.util.ParseHelper
import org.junit.Test
import de.peeeq.pscript.pscript.Program
import com.google.inject.Guice
import static junit.framework.Assert.*
import de.peeeq.pscript.PscriptRuntimeModule
import de.peeeq.pscript.intermediateLang.IntermediateCodeGenerator
import de.peeeq.pscript.utils.Utils
import org.eclipse.emf.ecore.util.EcoreUtil

@InjectWith(typeof(PscriptInjectorProvider))
@RunWith(typeof(XtextRunner))
class Tests {
	
	
	@Inject
	ParseHelper<Program> parser

	@Test 
	def void parseDomainmodel() {
		val injector = Guice::createInjector(new PscriptRuntimeModule());		
		val iLconverter = injector.getInstance(typeof(IntermediateCodeGenerator));
		
    	val model = parser.parse(
	        "package blub {
				val x = 5
				function foo(x:Int, y:Int):Int {
					return 1+2
				}
			}")
			
			
			EcoreUtil::resolveAll(model);
			iLconverter.translatePrograms(Utils::list(model))
	      assertNotNull(model)
	} 
	
	
} 