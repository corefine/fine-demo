package org.corefine.utils.net;

/**
 * @author Fe by 2022/12/9 11:08
 */
public class IpUtil {

    private static long intV(String s) {
        return Long.parseLong(s);
    }

    public static String toString(long ip) {
        return (ip >>> 24 & 0xff) + "." + (ip >>> 16 & 0xff) + "." + (ip >>> 8 & 0xff) + "." + (ip & 0xff);
    }

    public static long toLong(String ip) {
        if (ip == null) {
            return -1L;
        }
        String[] ss = ip.split("\\.");
        if (ss.length != 4) {
            return -1L;
        }
        long value;
        try {
            value = intV(ss[3]);
            value = value | intV(ss[2]) << 8;
            value = value | intV(ss[1]) << 16;
            value = value | intV(ss[0]) << 24;
        } catch (NumberFormatException e) {
            return -1L;
        }
        return value;
    }

    public static long[] toLongRange(String ipr) {
        if (ipr == null) {
            return new long[]{-1L, -1L};
        }
        int index = ipr.indexOf('/');
        if (index > ipr.length() - 2 && index < 1) {
            long ip = toLong(ipr);
            return new long[]{ip, ip};
        }
        long ip = toLong(ipr.substring(0, index));
        if (ip == -1) {
            return new long[]{-1L, -1L};
        }
        int r;
        try {
            r = Integer.parseInt(ipr.substring(index + 1));
        } catch (NumberFormatException e) {
            return new long[]{-1L, -1L};
        }
        long base = (1L << (32 - r)) - 1;
        return new long[]{ip & ~base, ip | base};
    }
}
