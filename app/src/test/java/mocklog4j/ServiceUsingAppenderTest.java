package mocklog4j;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;
import org.apache.logging.log4j.core.impl.Log4jContextFactory;
import org.apache.logging.log4j.core.test.appender.ListAppender;
import org.junit.Before;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class ServiceUsingAppenderTest {

    private CustomAppender appender;

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

    private List<LogEvent> events = new ArrayList<>();

    @BeforeEach
    public void setUp() {
        Logger logger = (Logger) LogManager.getLogger(Service.class); // this has to be the logger we expect
        appender = new CustomAppender(events);
        appender.start();
        logger.addAppender(appender);
        // set log level must be after appender add
        logger.setLevel(Level.INFO);
    }

    @AfterEach
    public void tearDown() {
        events.clear();
        Logger logger = (Logger) LogManager.getLogger(Service.class);
        logger.removeAppender(appender);
    }

    @Test
    public void testAddingAnAppenderWithoutReinitializingContextWithConfigBuilder() {
//        List<LogEvent> events = new ArrayList<>();
//        Logger logger = (Logger) LogManager.getLogger(Service.class); // this has to be the logger we expect
//        CustomAppender appender = new CustomAppender(events);
//        logger.addAppender(appender);
//        // set log level must be after appender add
//        logger.setLevel(Level.INFO);

        // Exercise
        Service sut = new Service();
        sut.action();

        // Verify
        assertEquals(1, events.size());
        LogEvent event = events.iterator().next();
        assertEquals("Action invoked", event.getMessage().getFormattedMessage());
//        LogManager.shutdown();
    }

    @Test
    public void testUsingAnotherMethod() {
        // Exercise
        Service sut = new Service();
        sut.secondAction();

        // Verify
        assertEquals(1, events.size());
        LogEvent event = events.iterator().next();
        assertEquals("Second action invoked", event.getMessage().getFormattedMessage());
    }
}