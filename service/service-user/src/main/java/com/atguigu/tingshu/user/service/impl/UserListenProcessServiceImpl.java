package com.atguigu.tingshu.user.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.tingshu.common.config.constant.RabbitConstant;
import com.atguigu.tingshu.common.constant.SystemConstant;
import com.atguigu.tingshu.common.util.AuthContextHolder;
import com.atguigu.tingshu.common.util.MongoUtil;
import com.atguigu.tingshu.model.user.UserListenProcess;
import com.atguigu.tingshu.user.service.UserListenProcessService;
import com.atguigu.tingshu.vo.album.TrackStatMqVo;
import com.atguigu.tingshu.vo.album.TrackStatVo;
import com.atguigu.tingshu.vo.user.UserListenProcessVo;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@SuppressWarnings({"unchecked", "rawtypes"})
public class UserListenProcessServiceImpl implements UserListenProcessService {

	@Autowired
	private MongoTemplate mongoTemplate;
    @Qualifier("redisTemplate")
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public BigDecimal getTrackBreakSecond(Long trackId) {
        Long userId = AuthContextHolder.getUserId();
        // 根据用户Id、声音Id获取播放进度对象
        Query query = Query.query(Criteria.where("userId").is(userId).and("trackId").is(trackId));
        // 根据用户id计算该用户的播放进度应该存放的集合（Collection）
        String collectionName = MongoUtil.getCollectionName(MongoUtil.MongoCollectionEnum.USER_LISTEN_PROCESS, userId);
        // 执行查询
        UserListenProcess userListenProcess = mongoTemplate.findOne(query, UserListenProcess.class, collectionName);
        // 获取到播放的跳出时间
        if (null != userListenProcess) return userListenProcess.getBreakSecond();
        // 如果该用户没有当前声音的播放进度，则从头开始播放
        return new BigDecimal("0");
    }

    @Override
    public void updateListenProcess(UserListenProcessVo userListenProcessVo) {
        Long userId = AuthContextHolder.getUserId();
        if (userId == null) return;
        // 获取播放进度应该存放的集合
        String collectionName = MongoUtil.getCollectionName(MongoUtil.MongoCollectionEnum.USER_LISTEN_PROCESS, userId);

        Long trackId = userListenProcessVo.getTrackId();
        // 根据用户id、声音id获取播放进度
        Query query = Query.query(Criteria.where("userId").is(userId).and("trackId").is(trackId));
        // 查询登录用户当前声音播放进度
        UserListenProcess userListenProcess = mongoTemplate.findOne(query, UserListenProcess.class, collectionName);
        if (userListenProcess != null) {
            // 更新播放进度
            userListenProcess.setBreakSecond(userListenProcessVo.getBreakSecond());
            userListenProcess.setUpdateTime(new Date());
        }else {
            // 初始化播放进度对象，保存播放进度
            userListenProcess = new UserListenProcess();
            BeanUtils.copyProperties(userListenProcessVo,userListenProcess);

            // 初始化数据
            userListenProcess.setId(IdWorker.getIdStr());
            userListenProcess.setUserId(userId);
            userListenProcess.setIsShow(1);
            userListenProcess.setCreateTime(new Date());
            userListenProcess.setUpdateTime(userListenProcess.getUpdateTime());
        }
        this.mongoTemplate.save(userListenProcess,collectionName);

        // 判断是否已经统计过：同一个用户同一个声音每天只记录一次播放量
        String key = "user:track:" + new DateTime().toString("yyyy-MM-dd") + ":" + userId;
        Boolean isExist = this.redisTemplate.opsForValue().getBit(key,trackId);

        // 如果不存在，则记录为已统计并发送消息异步统计
        if (isExist) return;
        // 标记为已统计
        this.redisTemplate.opsForValue().setBit(key,trackId,true);
        // 设置过期时间：只需要记录到凌晨即可，这里计算过期时间 = 明天凌晨 - 当前时间
        LocalDateTime nextDay = LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.MIN); // 明日凌晨
        long expireTime = ChronoUnit.SECONDS.between(LocalDateTime.now(), nextDay); // 计算过期时间
        this.redisTemplate.expire(key, expireTime, TimeUnit.SECONDS);

        // 组装消息
        TrackStatMqVo trackStatMqVo = new TrackStatMqVo();

        // 防止重复消费的唯一标识
        trackStatMqVo.setBusinessNo(IdWorker.getIdStr());
        trackStatMqVo.setAlbumId(userListenProcessVo.getAlbumId());
        trackStatMqVo.setTarckId(trackId);
        trackStatMqVo.setStatType(SystemConstant.TRACK_STAT_PLAY);
        trackStatMqVo.setCount(1);
        // 发送消息
        this.rabbitTemplate.convertAndSend(RabbitConstant.USER_EXCHANGE,RabbitConstant.USER_STAT_RK,JSON.toJSONString(trackStatMqVo));
    }

    @Override
    public Map<String, Object> getLatelyTrack() {
        Long userId = AuthContextHolder.getUserId();
        Query query = Query.query(Criteria.where("userId").is(userId));
        Sort sort = Sort.by(Sort.Direction.DESC, "updateTime");

        query.with(sort);

        UserListenProcess userListenProcess = mongoTemplate.findOne(query, UserListenProcess.class, MongoUtil.getCollectionName(MongoUtil.MongoCollectionEnum.USER_LISTEN_PROCESS, userId));
        if (null == userListenProcess) return null;
        Map<String,Object> map = new HashMap<>();
        map.put("albumId", userListenProcess.getAlbumId());
        map.put("trackId", userListenProcess.getTrackId());
        return map;
    }
}
