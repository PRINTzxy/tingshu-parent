package com.atguigu.tingshu.user.login;

import java.util.Map;

@LoginBean(LoginType.PHONE_LOGIN)
public class PhoneLogin implements ILogin{
    @Override
    public Map<String, Object> login(LoginAccount account) {
        return null;
    }
}
