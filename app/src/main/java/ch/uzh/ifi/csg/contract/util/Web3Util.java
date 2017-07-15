package ch.uzh.ifi.csg.contract.util;

import org.spongycastle.jcajce.provider.digest.Keccak;
import org.spongycastle.util.encoders.Hex;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Helper class to convert ethereum units
 */

public class Web3Util {

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

    public static boolean isAddress(String address)
    {
        if(!isMatch(address, "(?i)(0x)?[0-9a-f]{40}"))
        {
            // check if it has the basic requirements of an address
            return false;
        }else if(isMatch(address, "(0x)?[0-9a-f]{40}") || isMatch(address, "(0x)?[0-9A-F]{40}"))
        {
            // If it's all small caps or all all caps, return true
            return true;
        }else
        {
            // Otherwise check each case
            return isChecksumAddress(address);
        }
    }

    private static boolean isMatch(String inputstring, String regex)
    {
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(inputstring);
        if (m.find())
            return true;

        return false;
    }

    private static boolean isChecksumAddress(String address)
    {
        address = address.replace("0x","");
        Keccak.Digest256 sha3Digest = new Keccak.Digest256();
        byte[] digest = sha3Digest.digest(address.getBytes());
        String addressHash = Hex.toHexString(digest);

        for (int i = 0; i < 40; i++ ) {
            // the nth letter should be uppercase if the nth digit of casemap is 1
            if ((Integer.parseInt(String.valueOf(addressHash.charAt(i)), 16) > 7 &&
                    !String.valueOf(address.charAt(i)).toUpperCase().equals(String.valueOf(address.charAt(i))))
                    || ((Integer.parseInt(String.valueOf(addressHash.charAt(i)), 16) <= 7
                    && !String.valueOf(address.charAt(i)).toLowerCase().equals(String.valueOf(address.charAt(i))))))
            {
                return false;
            }
        }

        return true;

    }
}
