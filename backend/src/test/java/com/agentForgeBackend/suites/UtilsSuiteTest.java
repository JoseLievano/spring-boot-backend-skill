package com.agentForgeBackend.suites;

import org.junit.platform.suite.api.*;

@Suite
@ExcludePackages("com.agentForgeBackend.suites")
@ExcludeClassNamePatterns(".*Suite*")
@SelectPackages("com.agentForgeBackend")
@IncludeTags("utils")
public class UtilsSuiteTest {
}
