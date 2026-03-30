package com.authServer.suites;

import org.junit.platform.suite.api.*;

@Suite
@ExcludePackages("com.authServer.suites")
@ExcludeClassNamePatterns(".*Suite*")
@SelectPackages("com.authServer")
@IncludeTags("utils")
public class UtilsSuiteTest {
}
