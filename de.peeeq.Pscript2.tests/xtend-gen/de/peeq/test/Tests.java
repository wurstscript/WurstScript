package de.peeq.test;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import de.peeeq.pscript.PscriptInjectorProvider;
import de.peeeq.pscript.PscriptRuntimeModule;
import de.peeeq.pscript.intermediateLang.IntermediateCodeGenerator;
import de.peeeq.pscript.pscript.Program;
import de.peeeq.pscript.utils.Utils;
import java.util.List;
import junit.framework.Assert;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtext.junit.util.ParseHelper;
import org.eclipse.xtext.junit4.InjectWith;
import org.eclipse.xtext.junit4.XtextRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@SuppressWarnings("all")
@InjectWith(PscriptInjectorProvider.class)
@RunWith(XtextRunner.class)
public class Tests {
  
  @Inject
  private ParseHelper<Program> parser;
  
  @Test
  public void parseDomainmodel() throws Exception {
    {
      PscriptRuntimeModule _pscriptRuntimeModule = new PscriptRuntimeModule();
      Injector _createInjector = Guice.createInjector(_pscriptRuntimeModule);
      final Injector injector = _createInjector;
      IntermediateCodeGenerator _instance = injector.<IntermediateCodeGenerator>getInstance(de.peeeq.pscript.intermediateLang.IntermediateCodeGenerator.class);
      final IntermediateCodeGenerator iLconverter = _instance;
      Program _parse = this.parser.parse("package blub {\n\t\t\t\tval x = 5\n\t\t\t\tfunction foo(x:Int, y:Int):Int {\n\t\t\t\t\treturn 1+2\n\t\t\t\t}\n\t\t\t}");
      final Program model = _parse;
      EcoreUtil.resolveAll(model);
      List<Program> _list = Utils.<Program>list(model);
      iLconverter.translatePrograms(_list);
      Assert.assertNotNull(model);
    }
  }
}