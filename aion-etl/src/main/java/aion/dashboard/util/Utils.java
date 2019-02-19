package aion.dashboard.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.lang.Math.log10;
import static java.lang.Math.min;

public class Utils {

    private Utils(){
        throw new UnsupportedOperationException("Cannot create an instance of " + Utils.class.getName());
    }

    private static final BigDecimal WEI_RATE = BigDecimal.valueOf(10).pow(18);
    private static final ZoneId UTC_ZONE_ID = ZoneId.of("UTC");

    public static ZonedDateTime getZDT(long timestamp){
        if (timestamp < 0 ){
            throw new IllegalArgumentException("Timestamp is negative");
        }
        else {
            return Instant.ofEpochSecond(timestamp).atZone(UTC_ZONE_ID);
        }
    }

    public static BigDecimal toAion(BigInteger weiValue){
        return new BigDecimal(weiValue).divide(WEI_RATE, MathContext.DECIMAL64);
    }
    public static BigDecimal toAion(BigDecimal weiValue){
        return weiValue.divide(WEI_RATE, MathContext.DECIMAL64);
    }
    public static BigDecimal toWei(BigInteger aionValue){
        return new BigDecimal(aionValue).multiply(WEI_RATE);
    }


    public static double approximate(BigDecimal bd, int factor){
        return bd.scaleByPowerOfTen(-1 * factor).doubleValue();
    }

    public static double approximate(BigInteger bi , int factor){
        return new BigDecimal(bi).scaleByPowerOfTen(-1 * factor).doubleValue();
    }


    public static <S,T> Collection<Tuple2<S,T>> zip(Collection<S> sCollection, Collection<T> tCollection){
        if (sCollection.size() == tCollection.size()){
            S[] sArr = (S[])sCollection.toArray();
            T[] tArr = (T[])tCollection.toArray();
            return IntStream.range(0, sCollection.size()).mapToObj(i -> new Tuple2<>(sArr[i], tArr[i])).collect(Collectors.toList());
        }
        else {
            return Collections.emptyList();
        }

    }

    public static Optional<String> getValidAddress(String address){
        if (address.length() >= 64){
            String s;
            if (address.startsWith("0x")){
                s = address.substring(2);
            }
            else {
                s = address;
            }


            return s.startsWith("a0") && s.length() == 64 ? Optional.of(s) : Optional.empty();

        }
        else {
            return Optional.empty();
        }

    }


    public static int granularityToTknDec(BigInteger granularity){
        if (granularity.longValue() <= 9 ){
            return 18;
        } else if (granularity.gcd(BigInteger.TEN).equals(BigInteger.TEN)){//check if the number is a multiple of 10
            return min(18, 18 - (int) log10(granularity.longValue()));
        } else {
            int logVal = (int) (log10(granularity.longValue()));
            return min(18, 18 - ((logVal) - 1));
        }
    }


    public static BigDecimal scaleTokenValue(BigInteger value, int decimal){
        if (decimal<0) throw new IllegalArgumentException("Decimal must be positive");
        else if (decimal == 0) return new BigDecimal(value);
        else return new BigDecimal(value).scaleByPowerOfTen(-decimal);
    }



    public static boolean trySleep(long sleepLength){
        try {
            Thread.sleep(sleepLength);
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    private static final int STR_MAX_LENGTH=65535;

    public static String truncate(String str){
        return truncate(str, STR_MAX_LENGTH);
    }

    public static String truncate(String str, int length) {
        if (str.length() <= length) return str;
        else return str.substring(0, length);
    }
    /**
     * Spin wait while waiting for the result of some operation
     * @param tSupplier the operation to perform
     * @param resultPredicate the predicate used to compute whether the operation completed
     * @param <T> the result type
     * @return The expected resulut
     * @throws InterruptedException If the thread is interrupted while waiting the response
     */
    public static <T> T awaitResult(Supplier<T> tSupplier, Predicate<T> resultPredicate) throws InterruptedException {
        T res;
        boolean firstRun = true;
        do {
            if (firstRun){
                firstRun = false;
            }
            else {
                Thread.sleep(50);
            }

            res = tSupplier.get();
        } while (resultPredicate.negate().test(res));
        return res;

    }
}
