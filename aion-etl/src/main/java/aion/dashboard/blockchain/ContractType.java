package aion.dashboard.blockchain;

import org.aion.util.bytes.ByteUtil;

public enum  ContractType {
    DEFAULT("DEFAULT",(byte)1),// The default value
    AVM("AVM",(byte)2),// The deployed contract is run by the DEFAULT
    UNKNOWN("", (byte)0);//In case the type is undefined
    public final String type;
    public final byte byteType;

    ContractType(String type, byte byteType){
        this.type = type;
        this.byteType = byteType;
    }


    byte getType(){
        return (byteType);
    }

    @Override
    public String toString() {
        return super.toString();
    }

    public static ContractType fromType(String type){
        switch (type){
            case "AVM": return AVM;
            case "DEFAULT": return DEFAULT;
            default: return UNKNOWN;
        }
    }



    public static ContractType fromBytes(byte[] type){
        var parsedType = Integer.parseInt(ByteUtil.toHexString(type).replace("0x",""),10);
        switch (parsedType){
            case 1: return DEFAULT;
            case 2: return AVM;
            default: return UNKNOWN;
        }
    }

    public static ContractType fromByte(byte type){
        switch (type){
            case 1: return DEFAULT;
            case 2: return AVM;
            default: return UNKNOWN;
        }
    }
}
