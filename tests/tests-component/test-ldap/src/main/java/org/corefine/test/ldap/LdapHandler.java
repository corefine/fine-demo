package org.corefine.test.ldap;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import java.util.Properties;

/**
 *  David.Wu
 *  2020/4/8 10:16
 */
public class LdapHandler {
    private String ldapUrl;
    private Properties properties;

    /**
     * LDAP认证测试
     * @return boolean 认证结果
     */
    public boolean checkLdap() {
        try {
            new InitialLdapContext(properties, null).close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public UserInfo checkLdap(String username, String password) {
        return this.checkLdap(username, password, properties);
    }

    public UserInfo checkLdap(String username, String password, Properties env) {
        try {
            LdapContext context = new InitialLdapContext(env, null);
            SearchControls constraints = new SearchControls();
            constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
            constraints.setReturningAttributes(new String[]  {"cn", "mail", "uid"});
            //获取指定用户的信息
            NamingEnumeration<SearchResult> en = context.search("", "(|(uid=" + username + ")(cn=" + username + ")(mail=" + username + "))", constraints);
            if (en == null || !en.hasMoreElements()) {
                return null;
            }
            StringBuilder userDn = new StringBuilder();
            UserInfo info = new UserInfo();
            while (en.hasMoreElements()) {
                SearchResult obj = en.nextElement();
                if (obj != null) {
                    userDn.append(obj.getName());
                }
                info.setLoginName(obj.getAttributes().get("uid").get().toString());
                info.setUserName(obj.getAttributes().get("cn").get().toString());
                info.setMail(obj.getAttributes().get("mail").get().toString());
            }
            //验证用户
            context.addToEnvironment(Context.SECURITY_PRINCIPAL, userDn.toString());
            context.addToEnvironment(Context.SECURITY_CREDENTIALS, password);
            context.reconnect(null);
            context.close();
            return info;
        } catch (Exception e) {
            return null;
        }
    }

    public void init() {
        properties = new Properties();
        properties.put("java.naming.factory.initial", "com.sun.jndi.ldap.LdapCtxFactory");
        properties.put("java.naming.security.authentication", "simple");
        properties.put("java.naming.provider.url", ldapUrl);
    }
}
