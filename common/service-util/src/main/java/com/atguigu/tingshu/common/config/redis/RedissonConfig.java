package com.atguigu.tingshu.common.config.redis;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {
    @Bean
    public RedissonClient redissonClient(){
        // 初始化配置对象
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://192.168.75.132:6379");
        return Redisson.create(config);
    }
}
