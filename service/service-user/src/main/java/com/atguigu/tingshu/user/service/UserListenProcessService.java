package com.atguigu.tingshu.user.service;

import com.atguigu.tingshu.vo.user.UserListenProcessVo;

import java.math.BigDecimal;
import java.util.Map;

public interface UserListenProcessService {

    BigDecimal getTrackBreakSecond(Long trackId);

    void updateListenProcess(UserListenProcessVo userListenProcessVo);

    Map<String, Object> getLatelyTrack();
}
