package com.atguigu.tingshu.user.login;

import java.util.Map;

public interface ILogin {
    Map<String, Object> login(LoginAccount account) throws Exception;
}
