package com.atguigu.tingshu.user.login;

public enum LoginType {
    WX_LOGIN(1, "微信登录"),
    PHONE_LOGIN(2, "手机号登录"),
    ACCOUNT_LOGIN(3, "账号登录"),
    DY_LOGIN(4, "抖音登录"),
    ;

    public int type;
    public String desc;

    LoginType(int type, String desc) {
        this.type = type;
        this.desc = desc;
    }
}
