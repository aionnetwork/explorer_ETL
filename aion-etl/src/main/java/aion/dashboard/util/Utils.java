package aion.dashboard.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;

public class Utils {



    private static BigDecimal WeiRate = BigDecimal.valueOf(10).pow(18);
    private Utils(){
        throw new UnsupportedOperationException("Cannot create an instance of utils");
    }

    public static BigDecimal fromWei(BigInteger weiValue){
        return new BigDecimal(weiValue).divide(WeiRate, MathContext.DECIMAL128);

    }

    public static BigDecimal toWei(BigInteger aionValue){
        return new BigDecimal(aionValue).multiply(WeiRate);
    }



    public static boolean trySleep(long millis){
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            return false;
        }
        return true;
    }

    private static final int STR_MAX_LENGTH=65535;

    public static String truncate(String str){
        return truncate(str, STR_MAX_LENGTH);
    }

    public static String truncate(String str, int length){
        if (str.length() <= length) return str;
        else return str.substring(0, length);
    }
}
