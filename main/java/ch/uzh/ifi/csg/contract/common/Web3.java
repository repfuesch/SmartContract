package ch.uzh.ifi.csg.contract.common;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Helper class to convert ethereum units
 */

public class Web3 {

    private static final String etherInWei = "1000000000000000000";

    public static BigDecimal toEther(BigInteger amountWei)
    {
        BigDecimal amountWeiDec = new BigDecimal(amountWei);
        return amountWeiDec.divide(new BigDecimal(etherInWei));
    }

    public static BigInteger toWei(BigDecimal amountEther)
    {
        return amountEther.multiply(new BigDecimal(etherInWei)).toBigIntegerExact();
    }
}
