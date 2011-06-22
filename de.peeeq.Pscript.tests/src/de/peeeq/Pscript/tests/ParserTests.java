package de.peeeq.Pscript.tests;



import java.io.FileInputStream;
import java.io.InputStream;

import org.eclipse.xtext.junit.AbstractXtextTests;
import org.junit.Test;

import de.peeeq.pscript.PscriptStandaloneSetup;
import de.peeeq.pscript.pscript.Expr;
import de.peeeq.pscript.pscript.ExprAdditive;
import de.peeeq.pscript.pscript.FuncDef;
import de.peeeq.pscript.pscript.NameDef;
import de.peeeq.pscript.pscript.PackageDeclaration;
import de.peeeq.pscript.pscript.Program;
import de.peeeq.pscript.pscript.Statement;
import de.peeeq.pscript.pscript.Statements;
import de.peeeq.pscript.pscript.StmtReturn;


public class ParserTests extends AbstractXtextTests {
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        with(new PscriptStandaloneSetup());
    }
    
    @Test
    public void testSimple() throws Exception {
    	Program prog = parse(new FileInputStream("scripts/valid/simple1.pscript"));
    	// has one package names "test"
    	PackageDeclaration pack = prog.getPackages().get(0); 
    	assertEquals(pack.getName(), "test");
    	// has one function:
    	NameDef e = pack.getElements().get(0);
    	
    	if (e instanceof FuncDef) {
    		FuncDef f = (FuncDef) e;
    		Statements statements = f.getBody();
    		Statement first = statements.getStatements().get(0);
    		if (first instanceof StmtReturn) {
    			StmtReturn r = (StmtReturn) first;
    			Expr expr = r.getE();
    			if (expr instanceof ExprAdditive) {
    				
    			} else {
    				fail("expression wrong: " + expr);
    			}
    		} else {
    			fail("first statement should be a return statement but was " + first );
    		}    			
    	} else {
    		fail("first entitiy should be a function but was " + e);
    	}
    }
    

    protected Program parse(InputStream code) throws Exception {
    	Program model = (Program) getModel(code);
    	return model;
    }

}

