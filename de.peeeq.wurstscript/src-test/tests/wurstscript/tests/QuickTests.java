package tests.wurstscript.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import tests.immutablecollections.ImmutableListTest;
import tests.immutablecollections.IntRangeTests;
import tests.wurstscript.utils.UtilsTest;


@RunWith(Suite.class)
@SuiteClasses({ 
	SimpleStatementTests.class, 
	SimpleFunctionTests.class,
	ImmutableListTest.class,
	IntRangeTests.class,
	UtilsTest.class,
	ArrayTests.class,
	ExpressionTests.class,
	ClassesTests.class,
	ClassesExtTests.class,
	ModuleTests.class,
	PackageTests.class,
	OptimizerTests.class,
	InterfaceTests.class,
	InterfaceExtendedTests.class,
	GenericsTests.class,
	TupleTests.class,
	BugTests.class,
	ScopingTests.class,
	OpOverloading.class,
	FlowAnalysisTests.class,
	NewFeatureTests.class
})
public class QuickTests {

}
