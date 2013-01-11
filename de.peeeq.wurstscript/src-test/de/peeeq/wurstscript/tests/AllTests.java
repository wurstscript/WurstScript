package de.peeeq.wurstscript.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import de.peeeq.immutablecollections.ImmutableListTest;
import de.peeeq.wurstscript.utils.UtilsTest;

@RunWith(Suite.class)
@SuiteClasses({ 
	QuickTests.class,
	RealWorldExamples.class
})
public class AllTests {

}
