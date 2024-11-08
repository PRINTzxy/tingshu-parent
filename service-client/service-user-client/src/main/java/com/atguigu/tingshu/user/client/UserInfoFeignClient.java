package com.atguigu.tingshu.user.client;

import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.model.user.UserInfo;
import com.atguigu.tingshu.model.user.UserPaidTrack;
import com.atguigu.tingshu.user.client.impl.UserInfoDegradeFeignClient;
import com.atguigu.tingshu.vo.user.UserInfoVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * <p>
 * 产品列表API接口
 * </p>
 *
 * @author qy
 */
@FeignClient(value = "service-user", fallback = UserInfoDegradeFeignClient.class)
public interface UserInfoFeignClient {
    @GetMapping("api/user/userInfo/getUserInfoById/{userId}")
    Result<UserInfo> getUserById(@PathVariable Long userId);

    @GetMapping("api/user/userInfo/getUserInfoById/{userId}")
    Result<UserInfoVo> getUserInfoById(@PathVariable("userId") Long userId);

    @GetMapping("api/user/userInfo/getPaidAlbumStat/{albumId}")
    Result<Boolean> getPaidAlbumStatByAlbumIdAndUserId(@PathVariable("albumId") Long albumId);

    @GetMapping("api/user/userInfo/getPaidTracks/{albumId}")
    Result<List<UserPaidTrack>> getPaidTracksByAlbumIdAndUserId(@PathVariable("albumId")Long albumId);

    @GetMapping("api/user/userInfo/updateExpiredVipStatus")
    Result updateExpiredVipStatus();
}