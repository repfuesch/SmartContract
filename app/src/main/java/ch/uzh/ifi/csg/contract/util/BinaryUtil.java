package ch.uzh.ifi.csg.contract.util;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class that provides helper methods for working with binary strings and byte lists
 */
public final class BinaryUtil {

    public static char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};

    /**
     * Converts the provided hexadecimal string to a byte array
     *
     * @param s
     * @return
     */
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len/2];

        for(int i = 0; i < len; i+=2){
            data[i/2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i+1), 16));
        }

        return data;
    }


    /**
     * Generates and returns a hexadecimal string from a byte array
     *
     * @param bytes
     * @return
     */
    public static String byteArrayToHexString(byte[] bytes) {
        char[] hexChars = new char[bytes.length*2];
        int v;

        for(int j=0; j < bytes.length; j++) {
            v = bytes[j] & 0xFF;
            hexChars[j*2] = hexArray[v>>>4];
            hexChars[j*2 + 1] = hexArray[v & 0x0F];
        }

        return new String(hexChars);
    }

    public static String bin2hex(byte[] data) {
        return String.format("%0" + (data.length*2) + "X", new BigInteger(1, data));
    }

    /**
     * Returns a byte array from list of Bytes
     *
     * @param byteList
     * @return
     */
    public static byte[] toByteArray(List<Byte> byteList)
    {
        byte[] bytes = new byte[byteList.size()];
        int i=0;
        for(Byte b: byteList)
            bytes[i++] = b.byteValue();

        return bytes;
    }

    /**
     * Returns a list of Byte from a byte array
     * @param bytes
     * @return
     */
    public static List<Byte> toByteList(byte[] bytes)
    {
        List<Byte> byteList = new ArrayList<>();
        for(byte b : bytes)
        {
            byteList.add(b);
        }
        return byteList;
    }
 }
