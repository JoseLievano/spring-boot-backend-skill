# forbiden-terminal-actions.md

In this rule you will find a list of forbidden terminal actions. Please don not every try to do any of these actions in the terminal.

## Guidelines

- Is forbidden run tests directly in terminal, the only way you are allowed to run tests, is if you find a test.sh file in the root directory of the application, if you find that file, then you can run that file from the terminal to test.
- Is forbidden to modify the enviroment variables, so you cannot use "export" to modify any enviroment variable.
- If you find a test.sh file in the root directory, is forbidden to modify that file, you can read the content of the file, but never modify a test.sh file.
- Is forbidden to modify the Java version from the terminal, or inside the pom.xml of an application.
- Is forbidden to modify the Java version that maven use.
- Is forbidden to try to run the application in the terminal.
- Is forbidden to use the mvn command in the terminal.
