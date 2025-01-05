package org.spring.authenticationservice;

import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AuthenticationServiceApplicationTests {

    @BeforeAll
    static void setup() {
        // Load the environment variables from the .env file
        Dotenv dotenv = Dotenv.configure().load();

        // Set system properties to inject them into Spring context
        System.setProperty("APPLICATION_PORT", dotenv.get("APPLICATION_PORT"));
        System.setProperty("DATABASE_PASSWORD", dotenv.get("DATABASE_PASSWORD"));
        System.setProperty("DATABASE_URL", dotenv.get("DATABASE_URL"));
        System.setProperty("DATABASE_USERNAME", dotenv.get("DATABASE_USERNAME"));
        System.setProperty("EMAIL_SERVICE_NAME", dotenv.get("EMAIL_SERVICE_NAME"));
        System.setProperty("EUREKA_ENABLED", dotenv.get("EUREKA_ENABLED"));
    }

    @Test
    void contextLoads() {
    }

}
