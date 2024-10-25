package com.atguigu.tingshu.user.login;

import java.util.Map;

@LoginBean(LoginType.ACCOUNT_LOGIN)
public class AccountLogin implements ILogin{
    @Override
    public Map<String, Object> login(LoginAccount account) {
        return null;
    }
}
