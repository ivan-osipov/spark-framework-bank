package ru.i_osipov.sfb.integration.negative;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import ru.i_osipov.sfb.Server;
import ru.i_osipov.sfb.data.model.Account;
import ru.i_osipov.sfb.integration.SparkFrameworkExtension;

import java.util.UUID;

import static io.restassured.RestAssured.when;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

@ExtendWith(SparkFrameworkExtension.class)
public class AccountApiTests {

    @Test
    public void shouldInterruptWhenIdIsNotUUID() {
        when()
                .get("/api/account/{id}/balance", "someId")
                .then()
                .statusCode(400)
                .contentType(ContentType.JSON)
                .body("message", equalTo("The request contains an incorrect UUID value: someId"));
    }

    @Test
    public void shouldInterruptBalanceGettingWhenAccountIsNotFound() {
        UUID accountId = UUID.randomUUID();
        when()
                .get("/api/account/{id}/balance", accountId)
                .then()
                .statusCode(404)
                .contentType(ContentType.JSON)
                .body("message", equalTo(String.format("Account with id %s does not exist", accountId.toString())));
    }

    @Test
    public void shouldDeleteAccount() {
        when()
                .delete("/api/account/{id}", UUID.randomUUID())
                .then()
                .statusCode(404)
                .contentType(ContentType.JSON)
                .body("data", equalTo(false));
    }

    @Test
    public void shouldInterruptReplenishmentWhenNegativeAmount(Server server) {
        Account account = server.getDataStore().createAccount(10);
        when()
                .post("/api/account/{id}/replenishment/{amount}", account.getId(), -5.0)
                .then()
                .statusCode(400)
                .contentType(ContentType.JSON)
                .body("message", equalTo("Incorrect money value"));

        assertThat(server.getDataStore().getAccountBalance(account.getId()), equalTo(10.0));
    }

    @Test
    public void shouldInterruptReplenishmentWhenZeroAmount(Server server) {
        Account account = server.getDataStore().createAccount(10);
        when()
                .post("/api/account/{id}/replenishment/{amount}", account.getId(), 0.0)
                .then()
                .statusCode(400)
                .contentType(ContentType.JSON)
                .body("message", equalTo("Incorrect money value"));

        assertThat(server.getDataStore().getAccountBalance(account.getId()), equalTo(10.0));
    }

    @Test
    public void shouldInterruptWithdrawalBecauseMoneyIsNotEnough(Server server) {
        Account account = server.getDataStore().createAccount(200.0);

        when()
                .post("/api/account/{id}/withdrawal/{amount}", account.getId(), 200.01)
                .then()
                .statusCode(400)
                .contentType(ContentType.JSON)
                .body("message", equalTo("Incorrect money amount or account id"));

        assertThat(server.getDataStore().getAccountBalance(account.getId()), equalTo(200.0));
    }

    @Test
    public void shouldInterruptTransferBecauseMoneyIsNotEnough(Server server) {
        Account fromAccount = server.getDataStore().createAccount(10.0);
        Account toAccount = server.getDataStore().createAccount(10.0);

        when()
                .post("/api/account/transfer/{fromAccount}/to/{toAccount}/{amount}", fromAccount.getId(), toAccount.getId(), 10.01)
                .then()
                .statusCode(400)
                .contentType(ContentType.JSON)
                .body("message", equalTo("Incorrect money amount or account id"));

        assertThat(server.getDataStore().getAccountBalance(fromAccount.getId()), equalTo(10.0));
        assertThat(server.getDataStore().getAccountBalance(toAccount.getId()), equalTo(10.0));
    }

    @Test
    public void shouldInterruptReplenishmentForReasonOfPrecision(Server server) {
        Account account = server.getDataStore().createAccount(0);
        when()
                .post("/api/account/{id}/replenishment/{amount}", account.getId(), 0.001)
                .then()
                .statusCode(400)
                .contentType(ContentType.JSON)
                .body("message", equalTo("Information loss. Processing of value 0.001 with precision 2 can loss information"));

        assertThat(server.getDataStore().getAccountBalance(account.getId()), equalTo(0.0));
    }

    @Test
    public void shouldInterruptWithdrawalForReasonOfPrecision(Server server) {
        Account account = server.getDataStore().createAccount(200.0);

        when()
                .post("/api/account/{id}/withdrawal/{amount}", account.getId(), 1.001)
                .then()
                .statusCode(400)
                .contentType(ContentType.JSON)
                .body("message", equalTo("Information loss. Processing of value 1.001 with precision 2 can loss information"));

        assertThat(server.getDataStore().getAccountBalance(account.getId()), equalTo(200.0));
    }

    @Test
    public void shouldInterruptTransferForReasonOfPrecision(Server server) {
        Account fromAccount = server.getDataStore().createAccount(10.0);
        Account toAccount = server.getDataStore().createAccount(10.0);

        when()
                .post("/api/account/transfer/{fromAccount}/to/{toAccount}/{amount}", fromAccount.getId(), toAccount.getId(), 1.001)
                .then()
                .statusCode(400)
                .contentType(ContentType.JSON)
                .body("message", equalTo("Information loss. Processing of value 1.001 with precision 2 can loss information"));

        assertThat(server.getDataStore().getAccountBalance(fromAccount.getId()), equalTo(10.0));
        assertThat(server.getDataStore().getAccountBalance(toAccount.getId()), equalTo(10.0));
    }
}
