package mocklog4j;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;
//import org.apache.logging.log4j.test.appender.ListAppender; // up to 2.19.0
import org.apache.logging.log4j.core.test.appender.ListAppender; // from version 2.20.0
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ServiceTest {

    private Configuration configuration;

    @BeforeEach
    void setUp() {
        configuration = createConfiguration();
    }

    private Configuration createConfiguration() {
        ConfigurationBuilder<BuiltConfiguration> configBuilder =
                ConfigurationBuilderFactory.newConfigurationBuilder();
        return configBuilder
                .add(
                        configBuilder
                                .newAppender("LOG_SPY", "List")
                                .add(configBuilder.newLayout("PatternLayout").addAttribute("pattern", "%-5p [%t]: %m%n"))
                )
                .add(
                        configBuilder
                                .newRootLogger(Level.INFO)
                                .add(configBuilder.newAppenderRef("LOG_SPY")))
                .build(false);
    }

    @Test
    public void givenServiceInstancedInfoIsLogged() {
        try (LoggerContext logCtx = Configurator.initialize(configuration)) {
            // setup
            // final Configuration config = logCtx.getConfiguration();
            // ListAppender appender = config.getAppender("LOG_SPY");
            // or more directly
            ListAppender appender = ListAppender.getListAppender("LOG_SPY");
            assertNotNull(appender, "Can't find logs spy appender");

            // exercise
            Service sut = new Service();
            sut.action();

            // verify
            List<String> messages = appender.getMessages();
            assertNotNull(messages);
            String message = messages.iterator().next();
            assertTrue(message.contains("Action invoked"));
        }
    }

    @Test
    public void givenServiceInstancedInfoIsLoggedOnAnotherMethod() {
        try (LoggerContext logCtx = Configurator.initialize(configuration)) {
            // setup
            final Configuration config = logCtx.getConfiguration();
            ListAppender appender = config.getAppender("LOG_SPY");
            assertNotNull(appender, "Can't find logs spy appender");

            // exercise
            Service sut = new Service();
            sut.secondAction();

            // verify
            List<String> messages = appender.getMessages();
            assertNotNull(messages);
            String message = messages.iterator().next();
            assertTrue(message.contains("Second action invoked"));
        }
    }
}