package com.atguigu.tingshu.user.service;

import com.atguigu.tingshu.model.user.UserInfo;
import com.atguigu.tingshu.model.user.UserPaidTrack;
import com.atguigu.tingshu.vo.user.UserInfoVo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

public interface UserInfoService extends IService<UserInfo> {

    Map<String, Object> wxLogin(String code) throws Exception;

    UserInfoVo getUserInfo(Long id);

    Boolean getPaidAlbumStatByAlbumIdAndUserId(Long albumId, Long userId);

    List<UserPaidTrack> getPaidTracksByAlbumIdAndUserId(Long albumId, Long userId);

    void updateExpiredVipStatus();
}
