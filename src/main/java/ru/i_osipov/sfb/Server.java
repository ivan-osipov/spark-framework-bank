package ru.i_osipov.sfb;

import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.http.MimeTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.i_osipov.sfb.api.AccountApi;
import ru.i_osipov.sfb.api.JsonTransformer;
import ru.i_osipov.sfb.api.MoneyConverter;
import ru.i_osipov.sfb.api.dto.ErrorDto;
import ru.i_osipov.sfb.api.exceptions.IncorrectUuid;
import ru.i_osipov.sfb.api.exceptions.InformationLossException;
import ru.i_osipov.sfb.data.DataStore;
import ru.i_osipov.sfb.services.AccountService;

import static spark.Spark.*;

public class Server {

    private static final Logger logger = LoggerFactory.getLogger(Server.class);

    private DataStore dataStore;
    private AccountService accountService;

    public Server(int port) {
        this(port, 2);
    }

    public Server(int port, int precision) {
        port(port);
        dataStore = new DataStore(new MoneyConverter(precision));
        accountService = new AccountService(dataStore);
    }

    public void run() {
        before(((request, response) -> response.type(MimeTypes.Type.APPLICATION_JSON.asString())));

        path("/api/account", () -> {

            AccountApi api = new AccountApi(accountService);

            post("/", api::create, JsonTransformer.get());
            delete("/:id", api::delete, JsonTransformer.get());

            post("/:id/replenishment/:amount", api::replenishment, JsonTransformer.get());
            post("/:id/withdrawal/:amount", api::withdrawal, JsonTransformer.get());
            post("/transfer/:from/to/:to/:amount", api::transfer, JsonTransformer.get());
            get("/account/:id/balance", api::getBalance, JsonTransformer.get());

            exception(IncorrectUuid.class, (exception, request, response) -> {
                logger.warn("Bad request. Incorrect UUID value", exception);

                response.status(HttpStatus.BAD_REQUEST_400);
                String message = String.format("The request contains an incorrect UUID value: %s", exception.getIncorrectUuid());
                response.body(JsonTransformer.get().safeRender(ErrorDto.create(message)));
            });
            exception(NumberFormatException.class, (exception, request, response) -> {
                logger.warn("Bad request. Incorrect number value.", exception);

                response.status(HttpStatus.BAD_REQUEST_400);
                String message = "Incorrect number value";
                response.body(JsonTransformer.get().safeRender(ErrorDto.create(message)));
            });
            exception(InformationLossException.class, (exception, request, response) -> {
                logger.warn("The request with unsupported precision happened", exception);

                response.status(HttpStatus.BAD_REQUEST_400);
                String message = exception.getMessage();
                response.body(JsonTransformer.get().safeRender(ErrorDto.create(message)));
            });
        });
    }

    public DataStore getDataStore() {
        return dataStore;
    }

    public static void main(String[] args) {
        new Server(8080).run();
    }

}
