package aion.dashboard.service;

import aion.dashboard.domainobject.Token;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public interface TokenService {
    boolean save(Token token) ;
    boolean save(List<Token> tokens) ;

    Token getByContractAddr(String contractAddr) throws SQLException;

    void delete(String contractAddr) throws SQLException;

    PreparedStatement prepare(Connection con, List<Token> tokens) throws SQLException;

}
