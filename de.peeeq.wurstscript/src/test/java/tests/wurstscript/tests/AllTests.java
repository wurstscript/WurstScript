package tests.wurstscript.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ 
	QuickTests.class,
	RealWorldExamples.class,
	ModelManagerTests.class
})
public class AllTests {

}
