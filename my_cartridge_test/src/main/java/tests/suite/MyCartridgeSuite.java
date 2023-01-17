package tests.suite;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import tests.embedded.DummyIntegrationTest;

@RunWith(Suite.class)
@SuiteClasses({ DummyIntegrationTest.class})
public class MyCartridgeSuite
{
}
