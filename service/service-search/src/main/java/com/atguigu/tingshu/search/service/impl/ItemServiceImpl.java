package com.atguigu.tingshu.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.tingshu.album.client.AlbumInfoFeignClient;
import com.atguigu.tingshu.album.client.CategoryFeignClient;
import com.atguigu.tingshu.album.client.TrackInfoFeignClient;
import com.atguigu.tingshu.common.cache.GuiGuCache;
import com.atguigu.tingshu.common.constant.RedisConstant;
import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.model.album.AlbumInfo;
import com.atguigu.tingshu.model.album.BaseCategoryView;
import com.atguigu.tingshu.search.service.ItemService;
import com.atguigu.tingshu.user.client.UserInfoFeignClient;
import com.atguigu.tingshu.vo.album.AlbumStatVo;
import com.atguigu.tingshu.vo.search.AlbumItemVo;
import com.atguigu.tingshu.vo.user.UserInfoVo;
import com.baomidou.mybatisplus.core.toolkit.Assert;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@SuppressWarnings({"unchecked", "rawtypes"})
public class ItemServiceImpl implements ItemService {

    @Resource
    private AlbumInfoFeignClient albumInfoFeignClient;
    @Resource
    private TrackInfoFeignClient trackInfoFeignClient;
    @Resource
    private CategoryFeignClient categoryFeignClient;
    @Resource
    private UserInfoFeignClient userInfoFeignClient;
    @Resource
    private ExecutorService executorService;
    @Qualifier("redisTemplate")
    @Autowired
    private RedisTemplate redisTemplate;
    @Resource
    private RedissonClient redissonClient;

    /*@GuiGuCache(prefix = RedisConstant.ALBUM_INFO_PREFIX,timeout = RedisConstant.ALBUM_TIMEOUT,
            random = RedisConstant.ALBUM_TEMPORARY_TIMEOUT,bloomFilter = RedisConstant.ALBUM_BLOOM_FILTER)*/
    @Override
    public Map<String, Object> loadItem(Long albumId) {

        // 1.查询缓存，命中直接返回
        String key = RedisConstant.ALBUM_INFO_PREFIX + albumId;
        Map<String,Object> cacheResult = (Map<String, Object>)redisTemplate.opsForValue().get(key);
        if (!CollectionUtils.isEmpty(cacheResult)) return cacheResult;


        //为了防止缓存被击穿，添加分布式锁
        RLock lock = this.redissonClient.getLock(RedisConstant.ALBUM_INFO_PREFIX + albumId + RedisConstant.CACHE_LOCK_SUFFIX);
        lock.lock();

        try {

            // 在等待获取锁的过程中，可能有其他请求提前获取了锁，并把数据放入了缓存。所以为了提高性能再次查询缓存，如果命中则直接返回
            Map<String,Object> cacheResult2 = (Map<String, Object>)redisTemplate.opsForValue().get(key);
            if (!CollectionUtils.isEmpty(cacheResult2)) return cacheResult2;

            // 2.查询数据库，放入缓存
            Map<String, Object> result = new HashMap<>();

            //根据专辑id查询专辑
            CompletableFuture<AlbumInfo> albumCompletableFuture = CompletableFuture.supplyAsync(()->{
                Result<AlbumInfo> albumInfoResult = albumInfoFeignClient.getAlbumInfoById(albumId);
                Assert.notNull(albumInfoResult,"专辑为空！");
                AlbumInfo albumInfo = albumInfoResult.getData();
                //保存专辑
                result.put("albumInfo", albumInfo);
                log.info("albumInfo:{}", JSON.toJSONString(albumInfo));
                return albumInfo;
            },executorService);

            //根据三级分类id查询一二三级分类信息
            CompletableFuture<Void> baseCategoryCompletableFuture = albumCompletableFuture.thenAcceptAsync(albumInfo->{
                Result<BaseCategoryView> baseCategoryViewResult = categoryFeignClient.findBaseCategoryViewByCategory3Id(albumInfo.getCategory3Id());
                BaseCategoryView baseCategoryView = baseCategoryViewResult.getData();
                result.put("baseCategoryView", baseCategoryView);
                log.info("baseCategoryView:{}", JSON.toJSONString(baseCategoryView));
            },executorService);

            //根据专辑id查询统计信息
            CompletableFuture<Void> albumStatCompletableFuture = CompletableFuture.runAsync(()->{
                Result<AlbumStatVo> albumStatVoResult = albumInfoFeignClient.getAlbumStateByAlbumId(albumId);
                AlbumStatVo albumStatVo = albumStatVoResult.getData();
                result.put("albumStatVo", albumStatVo);
                log.info("albumStatVo:{}", JSON.toJSONString(albumStatVo));
            },executorService);

            //根据主播id查询主播
            CompletableFuture<Void> announcerCompletableFuture = albumCompletableFuture.thenAcceptAsync(albumInfo->{
                Result<UserInfoVo> userInfoVoResult = userInfoFeignClient.getUserInfoById(albumInfo.getUserId());
                UserInfoVo userInfoVo = userInfoVoResult.getData();
                result.put("announcer", userInfoVo);
                log.info("announcer:{}", JSON.toJSONString(userInfoVo));
            },executorService);

            CompletableFuture.allOf(albumCompletableFuture,albumStatCompletableFuture,baseCategoryCompletableFuture,announcerCompletableFuture).join();
            if (CollectionUtils.isEmpty(result)){
                // 为了防止缓存穿透，结果集即使为null也缓存，只是缓存时间较短
                this.redisTemplate.opsForValue().set(key,result,300, TimeUnit.SECONDS);

            }else {
                // 为了防止缓存雪崩，给缓存时间添加随机值
                this.redisTemplate.opsForValue().set(key,result,RedisConstant.ALBUM_TEMPORARY_TIMEOUT+new Random().nextInt(600), TimeUnit.SECONDS);
            }
            return result;
        }finally {
            lock.unlock();
        }


    }
}
