package aion.dashboard.service;

public interface VerifierService {


    static VerifierService getInstance() {
        return VerifierServiceImpl.INSTANCE;
    }
    enum Permission {

        INTERNAL_TRANSFER((byte)0b00000001);
        final byte permissionBit;

        Permission(byte permissionBit){
            this.permissionBit = permissionBit;
        }
    }

    boolean verify(String address, Permission permission);



}
