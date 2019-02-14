package aion.dashboard.service;

import aion.dashboard.domainobject.Contract;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public interface ContractService {

    boolean save(Contract contract);

    boolean save(List<Contract> contractList);



    /**
     * Returns all the contracts that have the same contract addr
     * Use case called before an insert into the contract table
     *
     * @return
     */
    Contract selectContractsByContractAddr(String contractAddr) throws SQLException;

    PreparedStatement prepare(Connection con, List<Contract> contracts) throws SQLException;


}
