package ru.i_osipov.sfb.integration;

import io.restassured.path.json.config.JsonPathConfig;
import org.junit.jupiter.api.extension.*;
import ru.i_osipov.sfb.Server;

import java.util.Random;

import static io.restassured.RestAssured.config;
import static io.restassured.RestAssured.port;
import static io.restassured.config.JsonConfig.jsonConfig;
import static io.restassured.config.RestAssuredConfig.newConfig;

public class SparkFrameworkExtension implements BeforeAllCallback, AfterEachCallback, ParameterResolver {

    private static Server server;
    private static volatile boolean running = false;

    static {
        port = 30000 + new Random().nextInt(3000);
        config = newConfig()
                .jsonConfig(jsonConfig().numberReturnType(JsonPathConfig.NumberReturnType.DOUBLE));

        server = new Server(port, 2);
    }

    @Override
    public void beforeAll(ExtensionContext context) {
        if(!running) {
            server.run();
            running = true;
        }
    }

    @Override
    public void afterEach(ExtensionContext context) {
        server.getDataStore().removeAllAccounts();
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.getParameter().getType() == Server.class;
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return server;
    }
}
