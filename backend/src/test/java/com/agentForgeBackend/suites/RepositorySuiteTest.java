package com.agentForgeBackend.suites;

import org.junit.platform.suite.api.*;

@Suite
@ExcludePackages("com.agentForgeBackend.suites")
@ExcludeClassNamePatterns(".*Suite*")
@SelectPackages("com.agentForgeBackend")
@IncludeTags("repository")
public class RepositorySuiteTest {
}
