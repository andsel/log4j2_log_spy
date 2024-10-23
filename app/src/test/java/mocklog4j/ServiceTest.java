package mocklog4j;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ServiceTest {

    @Test
    public void givenServiceInstancedInfoIsLogged() {
        Service sut = new Service();
        sut.action();
    }
}