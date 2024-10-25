package com.atguigu.tingshu.user.login;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LoginClient implements ApplicationContextAware {

    private static final Map<Integer, ILogin> MAP = new ConcurrentHashMap<>();

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        // 根据注解获取登录的具体策略类对象
        Map<String, Object> beanMap = applicationContext.getBeansWithAnnotation(LoginBean.class);
        if (CollectionUtils.isEmpty(beanMap)){
            return;
        }
        // 把登录具体策略类对象放入map中
        Collection<Object> beans = beanMap.values();
        beans.forEach(bean -> {
            // 获取bean类上的注解
            LoginBean loginBean = bean.getClass().getAnnotation(LoginBean.class);
            // 获取注解中的value（loginType对象）
            LoginType loginType = loginBean.value();
            // 放入map中
            MAP.put(loginType.type, (ILogin) bean);
        });
    }

    public Map<String, Object> login(Integer type, LoginAccount account) throws Exception {
        return MAP.get(type).login(account);
    }
}
