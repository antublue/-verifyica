###  This README.md and other documentation is specific to a branch / release

---

[![Build](https://github.com/verifyica-team/verifyica/actions/workflows/build.yaml/badge.svg)](https://github.com/verifyica/actions/workflows/build.yaml) [![Codacy Badge](https://app.codacy.com/project/badge/Grade/0264117ec3e74d678551a03e67b4a6d2)](https://app.codacy.com/gh/verifyica-team/verifyica/dashboard?utm_source=gh&utm_medium=referral&utm_content=&utm_campaign=Badge_grade) ![JDK Compatibility](https://img.shields.io/badge/JDK%20compatibility-8+-blue.svg) ![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)

|              |   |
|--------------|---|
| API          | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.verifyica/api/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.verifyica/api) |
| Engine       | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.verifyica/engine/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.verifyica/engine)  |
| Maven Plugin | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.verifyica/maven-plugin/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.verifyica/maven-plugin)  |

![Verifyica](assets/verifyica.png)

A multithreaded Java test engine for parameterized / scenario / integration testing based on [JUnit 5](https://junit.org/junit5/) Platform.

# Purpose

### Parameterized Testing

Parameterized testing is traditionally test method oriented ...

- For each test method, execute the test method with the list of test arguments.

```
for (TestMethod testMethod : TestMethods) {
   for (TestArgument testArgument : TestArguments) {
      testMethod(testArgument)
   }   
}
```

Verifyica swaps the paradigm to be test argument oriented ...

- For each test argument, execute the list of test methods with the test argument.

```
for (TestArgument testArgument : TestArguments) {
   for (TestMethod testMethod : TestMethods) {
      testMethod(testArgument)
   }
}
```

### Scenario Testing

Unit testing is traditionally used where test methods in a test class are isolated and independent of each other ...

- for a test class
  - all test methods are executed 
  - i.e. a failure of one test method doesn't affect the testing of other test methods 

Verifyica uses a dependent test method paradigm ...

- for a test class
  - order the test methods
  - for a test argument 
    - execute the test methods sequentially
    - if a test method fails, remaining test methods are skipped

**Notes**

- default test method ordering is by test method name (or display name if defined), then by `@Verifyica.Order` annotations.

# Features

- Purpose built for integration testing using [testcontainers-java](https://java.testcontainers.org/)
  - i.e. for each test argument, execute a set of test methods
- Scenario based testing
  - if a test method fails, remaining test methods will be marked as skipped
  - default test method execution based on `@Verifyica.Order` annotation
- Multithreaded test class / test argument testing
  - configurable constraints
  - test arguments can be tested multithreaded
  - test methods are always tested sequentially for a test argument 
- Virtual thread support (Java 21+)
- Properties file driven configuration
- Test class filtering
  - class name
  - class tags
- Test class interceptors (extensions)
- Engine interceptors (extensions)
  - set up / tear down global / external resources
- Cleanup of `AutoClosable` test classes / test arguments
- Cleanup of `AutoClosable` objects in `Context` maps
- Object / resource sharing via contexts
  - [EngineContext](api/src/main/java/org/verifyica/api/EngineContext.java)
  - [ClassContext](api/src/main/java/org/verifyica/api/ClassContext.java)
  - [ArgumentContext](api/src/main/java/org/verifyica/api/ArgumentContext.java) 
- LockManager
  - locking semantics
- [IntelliJ](https://www.jetbrains.com/idea/) support
- [Maven](https://maven.apache.org/) support
  - via the Verifyica Maven Plugin
- [JUnit5 ConsoleLauncher](https://junit.org/junit5/docs/5.0.0-M5/user-guide/#running-tests-console-launcher) support

# High Level Design

```
for (TestClass testClass : TestClasses) {

    instantiate test class instance

    execute @Verifyica.Prepare methods (superclass then subclass)

    for (TestArgument testArgument : TestArguments) {
    
       execute @Verifyica.BeforeAll methods (superclass then subclass)
      
       for (test method : Test methods) {
       
          execute @Verifyica.BeforeEach methods (superclass then subclass)
          
              execute test method
          
          execute @Verifyica.AfterEach methods (subclass then superclass)
       }
       
       execute @Verifyica.AfterAll methods (subclass then superclass)
    }

    execute @Verificya.Conclude methods (subclass then superclass)
   
    destroy test class instance
}
```

# Test Containers Examples

Example tests using [testcontainers-java](https://java.testcontainers.org/)

- [KafkaTest.java](examples/src/test/java/org/verifyica/examples/testcontainers/KafkaTest.java)
- [MongoDBTest.java](examples/src/test/java/org/verifyica/examples/testcontainers/MongoDBTest.java)

Various other examples used for testing:

- [Tests](tests/src/test/java/org/verifyica/test/)

# Core Requirements

### Java

Verifyica is compiled against/targets Java 8, but Java 21+ is recommended for virtual thread support.

### Maven

When using Maven, the Verifyica Maven Plugin is required.

#### Plugin XML

Configure the Maven Surefire plugin to include standard JUnit tests (or exclude Verifyica tests) ...

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <version>3.5.0</version>
    <configuration>
        <includes>
            <include>%regex[.*org.junit.*]</include>
        </includes>
        <systemPropertyVariables>
            <junit.jupiter.extensions.autodetection.enabled>true
            </junit.jupiter.extensions.autodetection.enabled>
        </systemPropertyVariables>
    </configuration>
</plugin>
```

Add the Verifyica Maven plugin ...

```xml
<plugin>
    <groupId>org.verifyica</groupId>
    <artifactId>verifyica-maven-plugin</artifactId>
    <version>VERSION</version>
    <executions>
        <execution>
            <phase>test</phase>
            <goals>
                <goal>test</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

Add the Verifyica API and Engine artifacts ...

#### Dependency XML

```xml
<dependency>
    <groupId>org.verifyica</groupId>
    <artifactId>verifyica-api</artifactId>
    <version>VERSION</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.verifyica</groupId>
    <artifactId>verifyica-engine</artifactId>
    <version>VERSION</version>
    <scope>test</scope>
</dependency>
```

**Notes**

- Verifyica test classes **must** be excluded from the Maven Surefire plugin

# Ancillary Requirements

To perform integration testing, you need a test context/environment. Typically this is created using [testcontainers-java](https://java.testcontainers.org/) and [Docker](https://www.docker.com/) (or another compatible container environment.)

Alternatively, tests can be performed on an external environment/resource. Ideally the external environment/resource would have an API to initialize and destroy it.

An [EngineInterceptor](api/src/main/java/org/verifyica/api/engine/EngineInterceptor.java) can be used to initialize and destroy an external environment/resource before and after execution of tests.

# Documentation

- [Annotations](ANNOTATIONS.md)
- [API](API.md)
- [Interceptors](INTERCEPTORS.md)
- [Locking](LOCKING.md)
- [Configuration](CONFIGURATION.md)
- [Filters](FILTERS.md)
- [Wiki](https://github.com/verifyica/wiki)

**Notes**

- **Documentation is specific to a release**

# Contributing

See [Contributing](CONTRIBUTING.md) for details.

# License

Apache License 2.0, see [LICENSE](LICENSE).

# Code of Conduct

See [Code of Conduct](CODE_OF_CONDUCT.md) for details.

# DCO

See [DCO](DCO.md) for details.

# Support

![YourKit logo](https://www.yourkit.com/images/yklogo.png)

[YourKit](https://www.yourkit.com/) supports open source projects with innovative and intelligent tools for monitoring and profiling Java and .NET applications.

YourKit is the creator of <a href="https://www.yourkit.com/java/profiler/">YourKit Java Profiler</a>,
<a href="https://www.yourkit.com/dotnet-profiler/">YourKit .NET Profiler</a>,
and <a href="https://www.yourkit.com/youmonitor/">YourKit YouMonitor</a>.

---

Copyright (C) 2024 The Verifyica project authors
