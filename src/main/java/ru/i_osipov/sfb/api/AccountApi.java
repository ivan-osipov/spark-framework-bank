package ru.i_osipov.sfb.api;

import org.eclipse.jetty.http.HttpStatus;
import ru.i_osipov.sfb.api.dto.AccountBalanceDto;
import ru.i_osipov.sfb.api.dto.DataDto;
import ru.i_osipov.sfb.api.dto.ErrorDto;
import ru.i_osipov.sfb.api.dto.TransferResultDto;
import ru.i_osipov.sfb.services.AccountService;
import spark.Request;
import spark.Response;

import java.util.UUID;

import static ru.i_osipov.sfb.api.ParamsProcessingUtils.parseUuid;

public class AccountApi {

    private AccountService accountService;

    public AccountApi(AccountService accountService) {
        this.accountService = accountService;
    }

    public Object create(Request request, Response response) {
        UUID accountId = accountService.create();

        response.status(HttpStatus.CREATED_201);
        return DataDto.create(AccountBalanceDto.create(accountId, accountService.getBalance(accountId)));
    }

    public Object delete(Request request, Response response) {
        UUID accountId = parseUuid(request.params("id"));

        boolean deleted = accountService.delete(accountId);

        response.status(deleted ? HttpStatus.OK_200 : HttpStatus.NOT_FOUND_404);
        return DataDto.create(deleted);
    }

    public Object replenishment(Request request, Response response) {
        UUID accountId = parseUuid(request.params("id"));
        double moneyAmount = Double.valueOf(request.params("amount"));

        boolean successful = accountService.replenish(accountId, moneyAmount);

        if(successful) {
            response.status(HttpStatus.OK_200);
            return DataDto.create(AccountBalanceDto.create(accountId, accountService.getBalance(accountId)));
        } else {
            response.status(HttpStatus.BAD_REQUEST_400);
            return ErrorDto.create("Incorrect money value");
        }
    }

    public Object withdrawal(Request request, Response response) {
        UUID accountId = parseUuid(request.params("id"));
        double moneyAmount = Double.valueOf(request.params("amount"));

        boolean successful = accountService.withdrawal(accountId, moneyAmount);

        if(successful) {
            response.status(HttpStatus.OK_200);
            return DataDto.create(AccountBalanceDto.create(accountId, accountService.getBalance(accountId)));
        } else {
            response.status(HttpStatus.BAD_REQUEST_400);
            return ErrorDto.create("Incorrect money amount or account id");
        }
    }


    public Object transfer(Request request, Response response) {
        UUID fromAccountId = parseUuid(request.params("from"));
        UUID toAccountId = parseUuid(request.params("to"));
        double moneyAmount = Double.valueOf(request.params("amount"));

        boolean successful = accountService.transfer(fromAccountId, toAccountId, moneyAmount);

        if(successful) {
            response.status(HttpStatus.OK_200);
            return DataDto.create(TransferResultDto.create(
                    AccountBalanceDto.create(fromAccountId, accountService.getBalance(fromAccountId)),
                    AccountBalanceDto.create(toAccountId, accountService.getBalance(toAccountId))
            ));
        } else {
            response.status(HttpStatus.BAD_REQUEST_400);
            return ErrorDto.create("Incorrect money amount or account id");
        }
    }

    public Object getBalance(Request request, Response response) {
        UUID accountId = parseUuid(request.params("id"));

        if(accountService.exists(accountId)) {
            response.status(HttpStatus.OK_200);
            return DataDto.create(AccountBalanceDto.create(accountId, accountService.getBalance(accountId)));
        } else {
            response.status(HttpStatus.NOT_FOUND_404);
            return ErrorDto.create(String.format("Account with id %s does not exist", accountId));
        }
    }

}
