package aion.dashboard.consumer;

import aion.dashboard.db.DbConnectionPool;
import aion.dashboard.parser.type.AccountBatch;
import aion.dashboard.service.AccountService;
import aion.dashboard.service.AccountServiceImpl;
import aion.dashboard.service.ParserStateService;
import aion.dashboard.service.ParserStateServiceImpl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;

public class AccountWriter extends WriteTask<AccountBatch> {
    ParserStateService parserStateService;
    AccountService accountService;

    public AccountWriter(ParserStateService parserStateService, AccountService accountService) {
        super("Account-Writer");
        this.parserStateService = parserStateService;
        this.accountService = accountService;
    }

    public AccountWriter() {
        super("Account-Writer");
        parserStateService = ParserStateServiceImpl.getInstance();
        accountService= AccountServiceImpl.getInstance();

    }

    @Override
    public void write(AccountBatch records) throws Exception {

        try(Connection connection = DbConnectionPool.getConnection();
            PreparedStatement accPs = accountService.prepare(connection,records.getAccounts())
        ){
            try{
                accPs.executeBatch();
                connection.commit();
            }catch (Exception e){
                connection.commit();
                throw e;
            }
        }
    }
}
