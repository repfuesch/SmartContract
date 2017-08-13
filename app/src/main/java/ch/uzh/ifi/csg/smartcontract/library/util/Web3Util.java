package ch.uzh.ifi.csg.smartcontract.library.util;

import org.spongycastle.util.encoders.Hex;
import org.web3j.utils.Numeric;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Helper class to convert ethereum units and addresses
 */
public class Web3Util {

    private static final String etherInWei = "1000000000000000000";

    /**
     * Converts wei to ether
     *
     * @param amountWei
     * @return
     */
    public static BigDecimal toEther(BigInteger amountWei)
    {
        BigDecimal amountWeiDec = new BigDecimal(amountWei);
        return amountWeiDec.divide(new BigDecimal(etherInWei));
    }

    /**
     * Converts ether to wei
     *
     * @param amountEther
     * @return
     */
    public static BigInteger toWei(BigDecimal amountEther)
    {
        return amountEther.multiply(new BigDecimal(etherInWei)).toBigIntegerExact();
    }

    /**
     * Normalizes an Ethereum address.
     * Adapted from: https://github.com/ethereum/pyethereum/blob/782842758e219e40739531a5e56fff6e63ca567b/ethereum/utils.py
     *
     * @param address
     * @return
     */
    public static byte[] normalizeAddress(String address)
    {
        address = Numeric.cleanHexPrefix(address);
        byte[] decodedAddress = Hex.decode(address);
        byte[] result = new byte[20];
        for(int i=0; i<20; i++)
        {
            result[i] = decodedAddress[i];
        }
        return result;
    }
}
