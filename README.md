## Introduction

Log lines produced by applications are effectively output of the application itself, and time to time they need to be tested.
This small project leverage the capabilities of Log4j2 to spy the log lines produced by a Service under test.

### Idea 1 - use a programmatically built Log4j configuration
The idea is to leverage Log4j ListAppender to capture the log lines produced and then verify that contains the expected messages.

During the setup phase of the test a new Log$j Configuration is created programmatically, defining an appender (named `LOG_SPY`)
and a Logger.

The test code is surrounded by the creation of a new LoggerContext that uses the codec configuration.
From the context the `ListAppender` is searched and used.

### Additional notes
The class `ListAppender` is part of the artifacts `org.apache.logging.log4j:log4j-core` with `test` classifier till the version `2.19.0`,
because was generated from the `test` package of the same artifact.
Starting from Log4j2 `2.20.0` those test utilities were extracted in its own library: `org.apache.logging.log4j:log4j-core-test`.
Check the commit https://github.com/apache/logging-log4j2/commit/d4a81ec0530a207b49931d0c92b34b58a3d91e8c.

The class was also moved from `org.apache.logging.log4j.test.appender.ListAppender` to `org.apache.logging.log4j.core.test.appender.ListAppender`.

Check https://github.com/andsel/log4j2_log_spy/blob/85790eeb7f1987b4107be0e7e2ccf39fc6f82b79/app/src/test/java/mocklog4j/ServiceTest.java#L46-L64 
for a test example.

### Note on built-in configuration
Every call to `Configurator.initialize(config here)` has to use a fresh instance of the `Configuration`, especially after
a `context.close()` call. In close the all appender references is cleared, so it cut the link between the Appender and
the context, plus if the configuration result already as STOPPED is not reinitialized, so it needs to be a fresh and unused 
instance on each configurator initialization.

### Idea 2 - add a customer appender to the Logger to collect the messages
In this case the Log4j is not reconfigured but a custom spy appender is added the Logger used by the SUT:
```java
List<LogEvent> events = new ArrayList<>();
Logger logger = (Logger) LogManager.getLogger(Service.class); // this has to be the logger we expect
logger.addAppender(new CustomAppender(events));
logger.setLevel(Level.INFO);

// use SUT (Service) which uses the logger

// examine the logged messages
LogEvent event = events.iterator().next();
assertEquals("Something expected", event.getMessage().getFormattedMessage());
```
Be warned of a pair of things:
- `logger.setLevel` has to be called **after** the `addAppender` else the logger set its level to error. 
- appenders are cached by class name inside the Logger, so a second `addAppender` with another instance of same appender class, doesn't take effect. 
  After each test is has to be cleared with `logger.removeAppender(appender)`.