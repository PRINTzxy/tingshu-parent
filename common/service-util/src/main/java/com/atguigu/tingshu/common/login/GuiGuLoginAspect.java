package com.atguigu.tingshu.common.login;

import com.atguigu.tingshu.common.config.redis.RedisConfig;
import com.atguigu.tingshu.common.constant.RedisConstant;
import com.atguigu.tingshu.common.execption.GuiguException;
import com.atguigu.tingshu.common.result.ResultCodeEnum;
import com.atguigu.tingshu.common.util.AuthContextHolder;
import com.atguigu.tingshu.vo.user.UserInfoVo;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.SneakyThrows;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class GuiGuLoginAspect {

    @Autowired
    private RedisTemplate redisTemplate;

    @Around("@annotation(guiGuLogin)") //只切带guiGuLogin的方法
    public Object around(ProceedingJoinPoint joinPoint, GuiGuLogin guiGuLogin) throws Throwable{
        //1.从Request的头信息中获取token
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();
        String token = request.getHeader("token");
        //2.判断token是否为空
        UserInfoVo userInfoVo = null;
        if(StringUtils.isNotBlank(token)){
            //3.查询redis中的登陆状态
            userInfoVo = (UserInfoVo)this.redisTemplate.opsForValue().get(RedisConstant.USER_LOGIN_KEY_PREFIX + token);
        }
        //4.判断是否已登陆，如果未登录并且是必须登录的情况下则抛出异常
        if (userInfoVo == null && guiGuLogin.required()){
            throw new GuiguException(ResultCodeEnum.LOGIN_AUTH);
        }
        //5.如果已登录则放入ThreadLocal
        if(userInfoVo != null){
            AuthContextHolder.setUserId(userInfoVo.getId());
            AuthContextHolder.setUsername(userInfoVo.getNickname());
        }
        //6.执行目标方法的业务
//        System.out.println("============前增强");
        Object result = joinPoint.proceed(joinPoint.getArgs());
//        System.out.println("============后增强");
        //7.释放ThreadLocal中的载荷，避免内存泄露
        AuthContextHolder.removeUserId();
        AuthContextHolder.removeUsername();
        return result;
    }

    @Pointcut("execution(* com.atguigu.tingshu.*.service.*.*(..))")
    public void point(){}

    @Before("point()")
    public void before(){}

    @After("point() ")
    public void after(){}

}
