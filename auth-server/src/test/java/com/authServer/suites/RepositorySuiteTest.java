package com.authServer.suites;

import org.junit.platform.suite.api.*;

@Suite
@ExcludePackages("com.authServer.suites")
@ExcludeClassNamePatterns(".*Suite*")
@SelectPackages("com.authServer")
@IncludeTags("repository")
public class RepositorySuiteTest {
}
