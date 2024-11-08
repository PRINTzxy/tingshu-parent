package com.atguigu.tingshu.search.config;

import com.atguigu.tingshu.common.constant.RedisConstant;
import com.atguigu.tingshu.model.album.AlbumInfo;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RCountDownLatch;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class BloomFilterConfig {
    @Autowired
    private RedissonClient redissonClient;

    @PostConstruct
    public void init() throws Throwable{
        RBloomFilter<Object> bloomFilter = this.redissonClient.getBloomFilter(RedisConstant.ALBUM_BLOOM_FILTER);
        //期望插入数据数量：100w 精确度：97%
        bloomFilter.tryInit(1000000,0.03);

    }
}
