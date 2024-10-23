package mocklog4j;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Service {
    private final Logger LOGGER = LogManager.getLogger(Service.class);

    public void action() {
        LOGGER.info("Action invoked");
    }
}
