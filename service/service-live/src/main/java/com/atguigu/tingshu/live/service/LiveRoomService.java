package com.atguigu.tingshu.live.service;

import com.atguigu.tingshu.model.live.LiveRoom;
import com.atguigu.tingshu.vo.live.LiveRoomVo;
import com.baomidou.mybatisplus.extension.service.IService;

public interface LiveRoomService extends IService<LiveRoom> {
    LiveRoom saveLiveRoom(LiveRoomVo liveRoomVo);
}
