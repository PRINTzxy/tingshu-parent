package com.atguigu.tingshu.common.cache;

import com.alibaba.fastjson.JSON;
import com.atguigu.tingshu.common.constant.RedisConstant;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.sql.Time;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Component
@Aspect
public class GuiGuCacheAspect {
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private RedissonClient redissonClient;

    @Around("@annotation(GuiGuCache)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable{
        //获取签名
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();

        //获取目标方法对象
        Method method = signature.getMethod();

        //获取注解对象
        GuiGuCache guiGuCache = method.getAnnotation(GuiGuCache.class);

        //获取注解中的前缀
        String prefix = guiGuCache.prefix();
        //获取方法的参数
        Object[] args = joinPoint.getArgs();
        String param = StringUtils.join(args, ",");
        //组装key
        String key = prefix + param;

        //1.查询缓存
        Object object = redisTemplate.opsForValue().get(key);

        //判断缓存中的数据是否为空
        if(object != null) return JSON.parseObject(object.toString(),signature.getReturnType());

        //添加布隆过滤器解决缓存穿透
        RBloomFilter<Object> bloomFilter = redissonClient.getBloomFilter(guiGuCache.bloomFilter());
        if (!bloomFilter.contains(key)) return null;

        //2.为了防止缓存击穿，添加分布式锁
        RLock lock = redissonClient.getLock(key + RedisConstant.CACHE_LOCK_SUFFIX);
        lock.lock();

        try {
            //3.判断缓存中有没有，有直接返回(加锁的过程中，别的请求可能已经把数据放入缓存)
            Object obj2 = this.redisTemplate.opsForValue().get(key);

            //判断缓存中的数据是否为空
            if (obj2 != null) return obj2;

            //4.执行目标方法
            Object result = joinPoint.proceed(joinPoint.getArgs());

            //5.放入缓存 释放分布锁
            long timeout = guiGuCache.timeout() + new Random().nextInt((int) guiGuCache.random());
            this.redisTemplate.opsForValue().set(prefix + param,JSON.toJSONString(result),timeout, TimeUnit.SECONDS);
            return result;
        }finally {
            lock.unlock();
        }

    }


}
