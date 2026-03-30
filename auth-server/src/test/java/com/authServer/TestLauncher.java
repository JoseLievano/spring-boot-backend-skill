package com.authServer;


import com.authServer.suites.E2ESuiteTest;
import com.authServer.suites.RepositorySuiteTest;
import com.authServer.suites.UtilsSuiteTest;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

//Test
@Suite
@SelectClasses({
        E2ESuiteTest.class,
        RepositorySuiteTest.class,
        UtilsSuiteTest.class
})
public class TestLauncher {
}
