package com.atguigu.tingshu.album.listener;

import com.alibaba.fastjson.JSON;
import com.atguigu.tingshu.album.mapper.AlbumStatMapper;
import com.atguigu.tingshu.album.mapper.TrackStatMapper;
import com.atguigu.tingshu.common.config.constant.RabbitConstant;
import com.atguigu.tingshu.common.constant.RedisConstant;
import com.atguigu.tingshu.common.constant.SystemConstant;
import com.atguigu.tingshu.model.album.AlbumStat;
import com.atguigu.tingshu.model.album.TrackStat;
import com.atguigu.tingshu.vo.album.TrackStatMqVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Component
public class TrackListener {
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private AlbumStatMapper albumStatMapper;

    @Autowired
    private TrackStatMapper trackStatMapper;

    @Transactional
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(RabbitConstant.USER_STAT_QUEUE),
            exchange = @Exchange(value = RabbitConstant.USER_EXCHANGE,type = ExchangeTypes.TOPIC,ignoreDeclarationExceptions = "true"),
            key = RabbitConstant.USER_STAT_RK))
    public void trackStat(String json, Channel channel, Message message) throws IOException {
        if (StringUtils.isBlank(json)){
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
            return;
        }

        // 反序列化对象
        TrackStatMqVo statMqVo = JSON.parseObject(json, TrackStatMqVo.class);
        if (statMqVo == null){
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            return;
        }

        //确定消息是否重复消费
        if(!this.redisTemplate.opsForValue().setIfAbsent(RedisConstant.TRACK_STAT_PREFIX + statMqVo.getBusinessNo(),"",RedisConstant.CACHE_TIMEOUT, TimeUnit.SECONDS)){
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
            return;
        }

        try {
            // 给对应的声音的统计信息进行累加
            TrackStat trackStat = this.trackStatMapper.selectOne(new LambdaQueryWrapper<TrackStat>().eq(TrackStat::getTrackId, statMqVo.getTarckId()).eq(TrackStat::getStatType, statMqVo.getStatType()));
            trackStat.setStatNum(trackStat.getStatNum() + statMqVo.getCount());
            this.trackStatMapper.updateById(trackStat);

            // 给对应的专辑进行累加：播放量 和 评论数
            if (StringUtils.equals(SystemConstant.TRACK_STAT_PLAY, statMqVo.getStatType())){
                // 对应专辑的播放量也要累加
                AlbumStat albumStat = this.albumStatMapper.selectOne(new LambdaQueryWrapper<AlbumStat>().eq(AlbumStat::getAlbumId, statMqVo.getAlbumId()).eq(AlbumStat::getStatType, SystemConstant.ALBUM_STAT_PLAY));
                albumStat.setStatNum(albumStat.getStatNum() + statMqVo.getCount());
                this.albumStatMapper.updateById(albumStat);
            }
            if (StringUtils.equals(SystemConstant.TRACK_STAT_COMMENT, statMqVo.getStatType())){
                // 对应专辑的评论数也要累加
                AlbumStat albumStat = this.albumStatMapper.selectOne(new LambdaQueryWrapper<AlbumStat>().eq(AlbumStat::getAlbumId, statMqVo.getAlbumId()).eq(AlbumStat::getStatType, SystemConstant.ALBUM_STAT_COMMENT));
                albumStat.setStatNum(albumStat.getStatNum() + statMqVo.getCount());
                this.albumStatMapper.updateById(albumStat);
            }

            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);

        }catch (Exception e){
            // 重试之前要先删除幂等性key
            this.redisTemplate.delete(RedisConstant.TRACK_STAT_PREFIX + statMqVo.getBusinessNo());
            e.printStackTrace();
        }




    }



}
