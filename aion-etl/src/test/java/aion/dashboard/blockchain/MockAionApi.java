package aion.dashboard.blockchain;


import org.aion.api.*;
import org.aion.api.type.ApiMsg;

/**
 * This class mocks all the interactions between the API and the ETL
 */

public class MockAionApi implements IAionAPI {


    /**
     * Mocked classes return invalid data
     */
    private IChain chain;
    private IAdmin admin;


    public MockAionApi(MockAdmin.MockAdminBuilder builder) {
        try {
            admin = builder.build();
        } catch (Exception e) {
            e.printStackTrace();
            throw new Error("Failed to mock Aion admin. Ending test");
        }
        chain = new MockChain(((MockAdmin) admin).blkDetails);

    }

    @Override
    public ApiMsg destroyApi() {
        return new ApiMsg(0, true, ApiMsg.cast.BOOLEAN);
    }

    @Override
    public ApiMsg connect(String s) {
        return new ApiMsg(0, true, ApiMsg.cast.BOOLEAN);
    }

    @Override
    public ApiMsg connect(String s, boolean b) {
        return new ApiMsg(0, true, ApiMsg.cast.BOOLEAN);
    }

    @Override
    public ApiMsg connect(String s, boolean b, String s1) {
        return new ApiMsg(0, true, ApiMsg.cast.BOOLEAN);
    }

    @Override
    public ApiMsg connect(String s, int i, String s1) {
        return new ApiMsg(0, true, ApiMsg.cast.BOOLEAN);
    }

    @Override
    public ApiMsg connect(String s, boolean b, int i, String s1) {
        return new ApiMsg(0, true, ApiMsg.cast.BOOLEAN);
    }

    @Override
    public ApiMsg connect(String s, boolean b, int i, int i1, String s1) {
        return new ApiMsg(0, true, ApiMsg.cast.BOOLEAN);
    }

    @Override
    public boolean isConnected() {
        return true;
    }

    @Override
    public INet getNet() {
        throw new UnsupportedOperationException("Call to unsupported method in mocking class");
    }


    /**
     *
     * @return
     */
    @Override
    public IChain getChain() {
        return chain;
    }

    @Override
    public IMine getMine() {
        throw new UnsupportedOperationException("Call to unsupported method in mocking class");
    }

    @Override
    public ITx getTx() {
        throw new UnsupportedOperationException("Call to unsupported method in mocking class");
    }

    @Override
    public IWallet getWallet() {
        throw new UnsupportedOperationException("Call to unsupported method in mocking class");
    }

    @Override
    public IUtils getUtils() {
        throw new UnsupportedOperationException("Call to unsupported method in mocking class");
    }

    @Override
    public IAccount getAccount() {
        throw new UnsupportedOperationException("Call to unsupported method in mocking class");
    }

    @Override
    public IContractController getContractController() {
        throw new UnsupportedOperationException("Call to unsupported method in mocking class");
    }

    @Override
    public IAdmin getAdmin() {
        return admin;
    }

    public void update() throws Exception {
        ((MockAdmin)admin).update();}
}