package com.atguigu.tingshu.live.service.impl;

import com.atguigu.tingshu.common.execption.GuiguException;
import com.atguigu.tingshu.common.result.ResultCodeEnum;
import com.atguigu.tingshu.common.util.AuthContextHolder;
import com.atguigu.tingshu.live.mapper.LiveRoomMapper;
import com.atguigu.tingshu.live.service.LiveRoomService;
import com.atguigu.tingshu.live.utils.LiveAddressGenerator;
import com.atguigu.tingshu.live.utils.LiveUtil;
import com.atguigu.tingshu.model.live.LiveRoom;
import com.atguigu.tingshu.vo.live.LiveRoomVo;
import com.atguigu.tingshu.vo.live.TencentLiveAddressVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.Authenticator;
import java.util.Date;

@Service
@SuppressWarnings({"unchecked", "rawtypes"})
public class LiveRoomServiceImpl extends ServiceImpl<LiveRoomMapper, LiveRoom> implements LiveRoomService {

	@Autowired
	private LiveRoomMapper liveRoomMapper;
    @Autowired
    private LiveAddressGenerator liveAddressGenerator;

    @Override
    public LiveRoom saveLiveRoom(LiveRoomVo liveRoomVo) {
        Long userId = AuthContextHolder.getUserId();
        LiveRoom liveRoom = this.getOne(new LambdaQueryWrapper<LiveRoom>().eq(LiveRoom::getUserId, userId).ge(LiveRoom::getExpireTime, new Date()));

        //如果存在未过期直播，则抛出异常
        if (liveRoom != null) throw new GuiguException(ResultCodeEnum.EXIST_NO_EXPIRE_LIVE);

        liveRoom = new LiveRoom();
        BeanUtils.copyProperties(liveRoomVo,liveRoom);
        liveRoom.setUserId(userId);
        liveRoom.setStatus("1");
        liveRoom.setAppName(liveAddressGenerator.getAppName());
        liveRoom.setStreamName("guigu"+userId);

        // 获取直播过期时间，单位：秒
        long expire = liveRoomVo.getExpireTime().getTime() / 1000;
        TencentLiveAddressVo addressUrl = this.liveAddressGenerator.getAddressUrl(liveRoom.getStreamName(), expire);
        liveRoom.setPushUrl(addressUrl.getPushWebRtcUrl());
        liveRoom.setPlayUrl(addressUrl.getPullWebRtcUrl());
        this.save(liveRoom);
        return liveRoom;
    }
}
