package com.atguigu.tingshu.user.login;

import lombok.Data;

@Data
public class LoginAccount {
    private String code; // 微信登录 和 手机验证码
    private String username; // 账户登录
    private String password; // 账户登录
    private String phone; // 手机验证码登录
}
