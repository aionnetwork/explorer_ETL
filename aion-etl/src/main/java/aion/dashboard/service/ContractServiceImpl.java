package aion.dashboard.service;

import aion.dashboard.cache.CacheManager;
import aion.dashboard.db.DbConnectionPool;
import aion.dashboard.db.DbQuery;
import aion.dashboard.domainobject.Contract;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ContractServiceImpl implements ContractService {

    private static final ContractServiceImpl Instance = new ContractServiceImpl();
    private static final Logger GENERAL = LoggerFactory.getLogger("logger_general");
    private static final CacheManager<String,Contract> CONTRACT_CACHE = CacheManager.getManager(CacheManager.Cache.CONTRACT);
    private static final CacheManager<String,Boolean> CONTRACT_EXISTS_CACHE = CacheManager.getManager(CacheManager.Cache.CONTRACT_EXISTS);

    private ContractServiceImpl() {
        if (Instance != null) throw new IllegalStateException("Instance already exists");
    }

    public static ContractService getInstance() {
        return Instance;
    }

    @Override
    public boolean save(Contract contract) {

        try (Connection con = DbConnectionPool.getConnection()) {

            try (PreparedStatement ps = con.prepareStatement(DbQuery.ContractInsert)) {
                Contract comp = selectContractsByContractAddr(contract.getContractAddr());


                if (comp == null || !comp.getContractAddr().equals(contract.getContractAddr())) {//check whether the contract is within the db

                    ps.setString(1, contract.getContractAddr());
                    ps.setString(2, contract.getContractName());
                    ps.setString(3, contract.getContractCreatorAddr());
                    ps.setString(4, contract.getContractTxHash());
                    ps.setLong(5, contract.getBlockNumber());
                    ps.setLong(6, contract.getTimestamp());
                    ps.setInt(7, contract.getBlockYear());
                    ps.setInt(8, contract.getBlockMonth());
                    ps.setInt(9, contract.getBlockDay());
                    ps.setString(10, contract.getType());
                    ps.execute();

                    con.commit();
                }
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
    public boolean save(List<Contract> contractList) {

        try (Connection con = DbConnectionPool.getConnection()) {

            try (PreparedStatement ps = con.prepareStatement(DbQuery.ContractInsert)) {

                for (var contract : contractList) {
                    Contract comp = selectContractsByContractAddr(contract.getContractAddr());

                    if (comp == null || !comp.getContractAddr().equals(contract.getContractAddr())) {

                        ps.setString(1, contract.getContractAddr());
                        ps.setString(2, contract.getContractName());
                        ps.setString(3, contract.getContractCreatorAddr());
                        ps.setString(4, contract.getContractTxHash());
                        ps.setLong(5, contract.getBlockNumber());
                        ps.setLong(6, contract.getTimestamp());
                        ps.setInt(7, contract.getBlockYear());
                        ps.setInt(8, contract.getBlockMonth());
                        ps.setInt(9, contract.getBlockDay());
                        ps.setString(10, contract.getType());

                        ps.execute();
                    }
                }
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
        Connection con = null;


        try {
            con = DbConnectionPool.getConnection();

            try (PreparedStatement ps = con.prepareStatement(DbQuery.ContractSelect)) {
                ps.setString(1, contractAddr);

                try (ResultSet rs = ps.executeQuery()) {

                    Contract.ContractBuilder builder = new Contract.ContractBuilder();
                    while (rs.next()) {
                        builder.setBlockNumber(rs.getLong("block_number"))
                                .setTimestamp(rs.getLong("deploy_timestamp"))
                                .setContractAddr(rs.getString("contract_addr"))
                                .setContractCreatorAddr(rs.getString("contract_creator_addr"))
                                .setContractTxHash(rs.getString("contract_tx_hash"))
                                .setContractName(rs.getString("contract_name"))
                                .setType(rs.getString("type"));

                        return builder.build();
                    }
                }
            }
        } finally {
            try {
                Objects.requireNonNull(con).close();
            } catch (SQLException | NullPointerException ignored) {

            }
        }


        return null;
    }

    @Override
    public PreparedStatement prepare(Connection con, List<Contract> contracts) throws SQLException {
        PreparedStatement ps = con.prepareStatement(DbQuery.ContractInsert);
        for (var contract : contracts) {
            Contract comp = selectContractsByContractAddr(contract.getContractAddr());

            if (comp == null || !comp.getContractAddr().equals(contract.getContractAddr())) {

                ps.setString(1, contract.getContractAddr());
                ps.setString(2, contract.getContractName());
                ps.setString(3, contract.getContractCreatorAddr());
                ps.setString(4, contract.getContractTxHash());
                ps.setLong(5, contract.getBlockNumber());
                ps.setLong(6, contract.getTimestamp());
                ps.setInt(7, contract.getBlockYear());
                ps.setInt(8, contract.getBlockMonth());
                ps.setInt(9, contract.getBlockDay());
                ps.setString(10, contract.getType());

                ps.addBatch();
            }
        }

        return ps;
    }

    public Optional<Contract> findContract(String contractAddress) {
        if (CONTRACT_CACHE.contains(contractAddress)) return Optional.of(CONTRACT_CACHE.getIfPresent(contractAddress));
        else {
            try {
                Contract contract = Objects.requireNonNull(this.selectContractsByContractAddr(contractAddress));
                CONTRACT_CACHE.putIfAbsent(contractAddress, contract);
                return Optional.of(contract);
            } catch (Exception e) {
                return Optional.empty();
            }
        }
    }
    public boolean contractExists(String contractAddr){
        if (CONTRACT_EXISTS_CACHE.contains(contractAddr)){
            return CONTRACT_EXISTS_CACHE.getIfPresent(contractAddr);
        }
        else {
            boolean ret = findContract(contractAddr).isPresent();
            CONTRACT_EXISTS_CACHE.putIfAbsent(contractAddr, ret);
            return ret;

        }
    }
}
