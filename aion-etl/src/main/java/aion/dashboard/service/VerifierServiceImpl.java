package aion.dashboard.service;

import aion.dashboard.config.Config;
import aion.dashboard.db.DbConnectionPool;
import aion.dashboard.db.DbQuery;
import aion.dashboard.util.Utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class VerifierServiceImpl implements VerifierService {

    static final VerifierService INSTANCE=new VerifierServiceImpl();
    boolean enabledVerifier = Config.getInstance().isVerifierEnabled();
    @Override
    public boolean verify(String address, Permission permission) {
        if (!enabledVerifier){// determine if the configuration file allows this action
            return true;
        }else {
            return doVerification(address, permission);// then attempt the verification
        }
    }

    private boolean doVerification(String address, Permission permission) {
        try(Connection con = DbConnectionPool.getConnection();
            PreparedStatement ps = con.prepareStatement(DbQuery.SelectFromVerifiedContract)
        ){
            ps.setString(1, Utils.sanitizeHex(address));//get the permissions attached to the contract
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    byte b = (byte) rs.getInt(1);
                    return (b & permission.permissionBit) == permission.permissionBit;//find whether the permissions allow the requested action
                }else return false;
            }



        } catch (Exception e) {
            return false;
        }
    }
}
