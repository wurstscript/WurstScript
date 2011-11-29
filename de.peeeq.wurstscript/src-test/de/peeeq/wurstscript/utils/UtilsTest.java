package de.peeeq.wurstscript.utils;



import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;


import com.google.common.base.Function;

import de.peeeq.wurstscript.ast.Ast;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.GlobalVarDef;
import de.peeeq.wurstscript.ast.NameRef;
import de.peeeq.wurstscript.ast.WPackage;
import de.peeeq.wurstscript.ast.WPos;

public class UtilsTest {

  @Test
  public void array() {
    int[] ar1 = Utils.array(1, 2, 3);
    int[] ar2 = {1,2,3};
    Assert.assertArrayEquals(ar2, ar1);
  }

  @Test
  public void collect() {
	  WPos source = Ast.WPos("", 0, 0);
	CompilationUnit testProg = Ast.CompilationUnit(
			  Ast.WPackage(source.copy() , "test", Ast.WImports(), Ast.WEntities(
					  Ast.GlobalVarDef(source.copy(), Ast.Modifiers(), false, Ast.NoTypeExpr(), "v1", Ast.ExprIntVal(source.copy(), 5)),
					  Ast.GlobalVarDef(source.copy(), Ast.Modifiers(), false, Ast.NoTypeExpr(), "v2", Ast.ExprVarAccess(source.copy(), "r1")),
					  Ast.GlobalVarDef(source.copy(), Ast.Modifiers(), false, Ast.NoTypeExpr(), "v3", Ast.ExprBinary(source.copy(), Ast.ExprIntVal(source.copy(), 3), Ast.OpPlus(), Ast.ExprVarAccess(source.copy(), "r2"))),
					  Ast.GlobalVarDef(source.copy(), Ast.Modifiers(), false, Ast.NoTypeExpr(), "v4", Ast.ExprVarAccess(source.copy(), "r3"))
					  ))			  
			  );
	WPackage testPackage = (WPackage) testProg.get(0);
	GlobalVarDef testVarDef = (GlobalVarDef) testPackage.getElements().get(2);
	
    List<NameRef> varRefs = Utils.collect(NameRef.class, testVarDef.getInitialExpr());
    System.out.println(Utils.join(varRefs, ", "));
    Assert.assertEquals("v3", testVarDef.getName());
    Assert.assertEquals(1, varRefs.size());
    Assert.assertEquals("r2", varRefs.get(0).getVarName());
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
	Assert.assertEquals(8, list.size());
	Assert.assertEquals(2, list2.size());
	Object[] ar = {3, 6};
	Assert.assertArrayEquals(ar, list2.toArray());
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
    
    Object[] ar = {3, 2, 1};
	Assert.assertArrayEquals(ar , sorted.toArray());
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
