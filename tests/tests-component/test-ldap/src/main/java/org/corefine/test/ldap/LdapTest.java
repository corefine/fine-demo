package org.corefine.test.ldap;

import java.io.*;
import java.util.Properties;

/**
 * @author Fe by 2021/12/15 14:41
 */
public class LdapTest {

    public static void main(String[] args) throws IOException {
        LdapHandler handler = new LdapHandler();
        Properties properties = new Properties();
        properties.put("java.naming.factory.initial", "com.sun.jndi.ldap.LdapCtxFactory");
        properties.put("java.naming.security.authentication", "simple");
        properties.put("java.naming.provider.url", "ldap://172.37.66.112:10389");
//        System.out.println(handler.checkLdap(properties));
        while (true) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            System.out.print("请输入用户名(英文名、中文名或邮箱)：");
            String username = reader.readLine();
            System.out.print("请输入密码：");
            String password = reader.readLine();
            System.out.println("login: " + username + "@" + password);
            System.out.println(handler.checkLdap(username, password, properties));
        }
    }
}
