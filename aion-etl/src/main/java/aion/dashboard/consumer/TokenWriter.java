package aion.dashboard.consumer;

import aion.dashboard.db.DbConnectionPool;
import aion.dashboard.parser.type.TokenBatch;
import aion.dashboard.service.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;

public class TokenWriter extends WriteTask<TokenBatch> {
    TokenService tokenService;
    TokenHoldersService tokenHoldersService;
    TokenTransfersService transfersService;
    ParserStateServiceImpl parserStateService;

    public TokenWriter(TokenService tokenService, TokenHoldersService tokenHoldersService, TokenTransfersService transfersService, ParserStateServiceImpl parserStateService) {
        super("Token-Writer");
        this.tokenService = tokenService;
        this.tokenHoldersService = tokenHoldersService;
        this.transfersService = transfersService;
        this.parserStateService = parserStateService;
    }


    public TokenWriter(){
        super("Token Writer");
        tokenService = TokenServiceImpl.getInstance();
        tokenHoldersService = TokenHoldersServiceImpl.getInstance();
        transfersService = TokenTransfersServiceImpl.getInstance();
        parserStateService = ParserStateServiceImpl.getInstance();
    }
    @Override
    public void write(TokenBatch records) throws Exception {

        try(Connection con= DbConnectionPool.getConnection();
            PreparedStatement psToken= tokenService.prepare(con,records.getTokens());
            PreparedStatement psHolders = tokenHoldersService.prepare(con, records.getTokenHolders());
            PreparedStatement psTransfer = transfersService.prepare(con,records.getTransfers())){
            try{
                psToken.executeBatch();
                psHolders.executeBatch();
                psTransfer.executeBatch();
                con.commit();
            }catch (Exception e){
                con.rollback();
                e.printStackTrace();
                throw e;
            }
        }

    }
}
