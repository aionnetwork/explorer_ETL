package aion.dashboard.service;

import aion.dashboard.db.DbConnectionPool;
import aion.dashboard.db.DbQuery;
import aion.dashboard.domainobject.Contract;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

public class ContractServiceImpl implements ContractService {

    private static final ContractServiceImpl Instance = new ContractServiceImpl();
    private static final Logger GENERAL = LoggerFactory.getLogger("logger_general");


    private ContractServiceImpl() {
        if (Instance != null) throw new IllegalStateException("Instance already exists");
    }

    public static ContractService getInstance() {
        return Instance;
    }

    @Override
    public boolean save(Contract contract) {

        return save(Collections.singletonList(contract));
    }

    @Override
    public boolean save(List<Contract> contractList) {

        try (Connection con = DbConnectionPool.getConnection();
             PreparedStatement ps = prepare(con, contractList)) {

            try {
                ps.executeBatch();
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }

        } catch (SQLException e) {
            GENERAL.debug("Threw an exception in save: ", e);

            return false;
        }
        return true;
    }

    @Override
    public Contract selectContractsByContractAddr(String contractAddr) throws SQLException {


        try (var con = DbConnectionPool.getConnection();
            var ps = con.prepareStatement(DbQuery.CONTRACT_SELECT)) {


            ps.setString(1, contractAddr);

            try (ResultSet rs = ps.executeQuery()) {

                Contract.ContractBuilder builder = new Contract.ContractBuilder();
                while (rs.next()) {
                    builder.setBlockNumber(rs.getLong("block_number"))
                            .setTimestamp(rs.getLong("deploy_timestamp"))
                            .setContractAddr(rs.getString("contract_addr"))
                            .setContractCreatorAddr(rs.getString("contract_creator_addr"))
                            .setContractTxHash(rs.getString("contract_tx_hash"))
                            .setContractName(rs.getString("contract_name"));

                    return builder.build();

                }

            }


        }


        return null;
    }

    @Override
    public PreparedStatement prepare(Connection con, List<Contract> contracts) throws SQLException {
        PreparedStatement ps = con.prepareStatement(DbQuery.CONTRACT_INSERT);
        for (var contract : contracts) {
            Contract comp = selectContractsByContractAddr(contract.getContractAddr());

            if (comp == null || !comp.getContractAddr().equals(contract.getContractAddr())) {

                ps.setString(1, contract.getContractAddr());
                ps.setString(2, contract.getContractName());
                ps.setString(3, contract.getContractCreatorAddr());
                ps.setString(4, contract.getContractTxHash());
                ps.setLong(5, contract.getBlockNumber());
                ps.setLong(6, contract.getTimestamp());
                ps.addBatch();
            }
        }

        return ps;
    }


}
