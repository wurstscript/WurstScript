package de.peeeq.wurstscript.utils;


import static org.testng.AssertJUnit.assertEquals;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.testng.annotations.Test;

import com.google.common.base.Function;

import de.peeeq.wurstscript.ast.*;

public class UtilsTest {

  @Test
  public void array() {
    Integer[] ar1 = Utils.<Integer>array(1, 2, 3);
    Integer[] ar2 = {1,2,3};
    assertArrayEquals(ar2, ar1);
  }

  @Test
  public void collect() {
	  WPos source = Ast.WPos("", 0, 0);
	CompilationUnit testProg = Ast.CompilationUnit(
			  Ast.WPackage(source.copy() , "test", Ast.WImports(), Ast.WEntities(
					  Ast.GlobalVarDef(source.copy(), Ast.VisibilityDefault(), false, Ast.NoTypeExpr(), "v1", Ast.ExprIntVal(source.copy(), 5)),
					  Ast.GlobalVarDef(source.copy(), Ast.VisibilityDefault(), false, Ast.NoTypeExpr(), "v2", Ast.ExprVarAccess(source.copy(), "r1")),
					  Ast.GlobalVarDef(source.copy(), Ast.VisibilityDefault(), false, Ast.NoTypeExpr(), "v3", Ast.ExprBinary(source.copy(), Ast.ExprIntVal(source.copy(), 3), Ast.OpPlus(), Ast.ExprVarAccess(source.copy(), "r2"))),
					  Ast.GlobalVarDef(source.copy(), Ast.VisibilityDefault(), false, Ast.NoTypeExpr(), "v4", Ast.ExprVarAccess(source.copy(), "r3"))
					  ))			  
			  );
	WPackage testPackage = (WPackage) testProg.get(0);
	GlobalVarDef testVarDef = (GlobalVarDef) testPackage.getElements().get(2);
	
    List<VarRef> varRefs = Utils.collect(VarRef.class, testVarDef.getInitialExpr());
    System.out.println(Utils.join(varRefs, ", "));
    assertEquals("v3", testVarDef.getName());
    assertEquals(1, varRefs.size());
    assertEquals("r2", varRefs.get(0).getVarName());
  }

  @Test
  public void filter() {
	  
    List<Integer> list = Utils.list(1,2,3,4,5,6,7,8);
	List<Integer> list2 = Utils.filter(list , new Function<Integer, Boolean>() {

		@Override
		public Boolean apply(Integer input) {
			return input % 3 == 0;
		}
    	
    });
	assertEquals(8, list.size());
	assertEquals(2, list2.size());
	assertArrayEquals(Utils.<Integer>array(3,6), list2.toArray());
  }
  
  @Test
  public void topSort_1() throws TopsortCycleException {
    List<Integer> toSort = Utils.list(2,1,3);
    List<Integer> sorted = Utils.topSort(toSort, new Function<Integer,Collection<Integer>>() {

		@Override
		public Collection<Integer> apply(Integer input) {
			Collection<Integer> result = new LinkedList<Integer>();
			for (int i = 1; i<=3; i++) {
				if (i > input) {
					result.add(i);
				}
			}
			return result ;
		}
    	
    });
    assertArrayEquals(Utils.<Object>array(3,2,1) , sorted.toArray());
  }
  
/* TODO utils unit tests
  @Test
  public void isJassCode() {
    throw new RuntimeException("Test not implemented");
  }

  @Test
  public void join() {
    throw new RuntimeException("Test not implemented");
  }

  @Test
  public void list() {
    throw new RuntimeException("Test not implemented");
  }

  @Test
  public void map() {
    throw new RuntimeException("Test not implemented");
  }

  @Test
  public void parseAsciiInt1() {
    throw new RuntimeException("Test not implemented");
  }

  @Test
  public void parseAsciiInt4() {
    throw new RuntimeException("Test not implemented");
  }

  @Test
  public void parseHexInt() {
    throw new RuntimeException("Test not implemented");
  }

  @Test
  public void parseInt() {
    throw new RuntimeException("Test not implemented");
  }

  @Test
  public void printIndent() {
    throw new RuntimeException("Test not implemented");
  }

  @Test
  public void printSepStringBuilderStringTFunctionTString() {
    throw new RuntimeException("Test not implemented");
  }

  @Test
  public void printSepStringBuilderStringT() {
    throw new RuntimeException("Test not implemented");
  }

  @Test
  public void printSepStringBuilderStringIterableTFunctionTString() {
    throw new RuntimeException("Test not implemented");
  }

  @Test
  public void printSepStringString() {
    throw new RuntimeException("Test not implemented");
  }

  @Test
  public void removeDuplicates() {
    throw new RuntimeException("Test not implemented");
  }

  @Test
  public void sleep() {
    throw new RuntimeException("Test not implemented");
  }

  */
}
