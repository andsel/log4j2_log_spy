package mocklog4j;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.impl.Log4jContextFactory;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;
//import org.apache.logging.log4j.test.appender.ListAppender; // up to 2.19.0
import org.apache.logging.log4j.core.test.appender.ListAppender; // from version 2.20.0
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

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

    @Test
    public void testLikeInLogstash() {
        LogManager.setFactory(new Log4jContextFactory());
        LoggerContext logCtx = Configurator.initialize(configuration);

        Configuration config = logCtx.getConfiguration();
        ListAppender appender = config.getAppender("LOG_SPY");
        assertNotNull(appender, "Can't find logs spy appender");

        // exercise
        Service sut = new Service();
        sut.secondAction();

        // verify
        List<String> messages = appender.getMessages();
        assertNotNull(messages);
        String message = messages.iterator().next();
        assertThat(message).contains("Second action invoked");

        logCtx.close(); // clean appenders controls

        // second run
//        LogManager.setFactory(new Log4jContextFactory());
        // IMPORTANT: create a fresh new configuration on each initialize call, because the config is not
        // static, and on ctx.close is stopped, so on next reuse it won't start any Appender.
        logCtx = Configurator.initialize(createConfiguration());

        config = logCtx.getConfiguration();
        appender = config.getAppender("LOG_SPY");
        assertNotNull(appender, "Can't find logs spy appender");

        // exercise
        sut = new Service();
        sut.action();

        // verify
        messages = appender.getMessages();
        assertNotNull(messages);
        assertEquals(1,  messages.size());
        message = messages.iterator().next();
        assertThat(message).contains("Action invoked");

        logCtx.close();
    }

    @Test
    public void testAddingAnAppenderWithoutReinitializingContextWithConfigBuilder() {
        List<LogEvent> events = new ArrayList<>();
        Logger logger = (Logger) LogManager.getLogger(Service.class); // this has to be the logger we expect
        logger.addAppender(new CustomAppender(events));
        logger.setLevel(Level.INFO);

        // Exercise
        Service sut = new Service();
        sut.action();

        // Verify
        assertEquals(1, events.size());
        LogEvent event = events.iterator().next();
        assertEquals("Action invoked", event.getMessage().getFormattedMessage());
    }

    static class CustomAppender extends AbstractAppender {

        private final List<LogEvent> events;

        public CustomAppender(List<LogEvent> events) {
            super("CustomCaptorAppender", null, null, true, Property.EMPTY_ARRAY);
            this.events = events;
        }

        @Override
        public void append(LogEvent event) {
            events.add(event);
        }
    }
}