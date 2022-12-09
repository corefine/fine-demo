package org.corefine.utils.net.test;


import org.corefine.utils.net.IpUtil;

/**
 * @author Fe by 2022/12/9 14:21
 */
public class IpUtilTest {

    private static void print(String ipr) {
        long[] value = IpUtil.toLongRange(ipr);
        System.out.println(ipr + ":\t" + IpUtil.toString(value[0])+ "->" + IpUtil.toString(value[1]));

    }

    public static void main(String[] args) {
        print("192.168.1.0/24");
        print("192.168.1.0/16");
        print("192.168.1.0/8");
        print("192.168.1.0/12");
        print("10.8.8.0/24");
        print("10.8.0.0/16");
        print("10.0.0.0/8");
        print("172.16.0.0/12");
        print("0.0.0.0/8");
        print("0.0.0.0/0");

        System.out.println(IpUtil.toString(IpUtil.toLong("192.168.1.234")));
        System.out.println(IpUtil.toString(IpUtil.toLong("255.168.1.0")));
        System.out.println(IpUtil.toString(IpUtil.toLong("10.168.1.234")));
        System.out.println(IpUtil.toString(IpUtil.toLong("0.0.0.0")));
        System.out.println(IpUtil.toString(-1));
        System.out.println(IpUtil.toString(0));

        System.out.println(Long.toHexString(IpUtil.toLong("192.168.1.234")));
        System.out.println(Long.toHexString(IpUtil.toLong("255.168.1.0")));
        System.out.println(" " + Long.toHexString(IpUtil.toLong("10.168.1.234")));
        System.out.println(" " + Long.toHexString(IpUtil.toLong("10.168.1.234")));
    }
}
