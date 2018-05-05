package ru.i_osipov.sfb.integration.positive;

import io.restassured.http.ContentType;
import org.hamcrest.CustomTypeSafeMatcher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import ru.i_osipov.sfb.Server;
import ru.i_osipov.sfb.data.model.Account;
import ru.i_osipov.sfb.integration.SparkFrameworkExtension;

import static io.restassured.RestAssured.when;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static ru.i_osipov.sfb.api.ParamsProcessingUtils.parseUuid;

@ExtendWith(SparkFrameworkExtension.class)
public class AccountApiTests {

    @Test
    public void shouldCreateAccount(Server server) {
        when()
                .post("/api/account/")
                .then()
                .statusCode(201)
                .contentType(ContentType.JSON)
                .body("data.accountId", is(new CustomTypeSafeMatcher<String>("a created account's id") {
                            @Override
                            protected boolean matchesSafely(String item) {
                                return server.getDataStore().exists(parseUuid(item));
                            }
                        }),
                        "data.balance", equalTo(0.0));
    }

    @Test
    public void shouldDeleteAccount(Server server) {
        Account account = server.getDataStore().createAccount(0);

        when()
                .delete("/api/account/{id}", account.getId())
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("data", equalTo(true));
    }

    @Test
    public void shouldReplenishAccountBalance(Server server) {
        Account account = server.getDataStore().createAccount(1);
        when()
                .post("/api/account/{id}/replenishment/{amount}", account.getId(), 42.5)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("data.accountId", equalTo(account.getId().toString()),
                        "data.balance", equalTo(43.5));

        assertThat(server.getDataStore().getAccountBalance(account.getId()), equalTo(43.5));
    }

    @Test
    public void shouldWithdrawalMoneyFromBalance(Server server) {
        Account account = server.getDataStore().createAccount(200);

        when()
                .post("/api/account/{id}/withdrawal/{amount}", account.getId(), 150.5)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("data.accountId", equalTo(account.getId().toString()),
                        "data.balance", equalTo(49.5));

        assertThat(server.getDataStore().getAccountBalance(account.getId()), equalTo(49.5));
    }

    @Test
    public void shouldDoTransaction(Server server) {
        Account fromAccount = server.getDataStore().createAccount(111.4);
        Account toAccount = server.getDataStore().createAccount(88.6);

        when()
                .post("/api/account/transfer/{fromAccount}/to/{toAccount}/{amount}", fromAccount.getId(), toAccount.getId(), 11.4)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("data.from.accountId", equalTo(fromAccount.getId().toString()),
                        "data.from.balance", equalTo(100.0),
                        "data.to.accountId", equalTo(toAccount.getId().toString()),
                        "data.to.balance", equalTo(100.0));

        assertThat(server.getDataStore().getAccountBalance(fromAccount.getId()), equalTo(100.0));
        assertThat(server.getDataStore().getAccountBalance(toAccount.getId()), equalTo(100.0));
    }
}
