package com.agentForgeBackend;

import org.junit.platform.suite.api.ExcludeClassNamePatterns;
import org.junit.platform.suite.api.ExcludePackages;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectPackages("com.agentForgeBackend")
@ExcludePackages("com.agentForgeBackend.suites")
@ExcludeClassNamePatterns(".*TestLauncher")
public class TestLauncher {
}
