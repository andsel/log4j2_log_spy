## Introduction

Log lines produced by applications are effectively output of the application itself, and time to time they need to be tested.
This small project leverage the capabilities of Log4j2 to spy the log lines produced by a Service under test.

### Idea
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