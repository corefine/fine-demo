package org.corefine.test.ldap;

public class UserInfo {
    private String loginName;
    private String userName;
    private String mail;

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    @Override
    public String toString() {
        return "UserInfo{" +
                "loginName='" + loginName + '\'' +
                ", userName='" + userName + '\'' +
                ", mail='" + mail + '\'' +
                '}';
    }
}