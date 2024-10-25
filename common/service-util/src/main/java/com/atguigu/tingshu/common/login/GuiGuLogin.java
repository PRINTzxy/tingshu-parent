package com.atguigu.tingshu.common.login;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface GuiGuLogin {
    /**
     * 是否必须要登陆
     * 如果为false，不登陆也可以访问，如果登陆了，则可以获取用户信息
     * @Return
     */

    boolean required() default true;
}
