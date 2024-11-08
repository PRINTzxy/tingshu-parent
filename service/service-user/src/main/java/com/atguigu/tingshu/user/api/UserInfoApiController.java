package com.atguigu.tingshu.user.api;

import com.atguigu.tingshu.common.login.GuiGuLogin;
import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.common.util.AuthContextHolder;
import com.atguigu.tingshu.model.user.UserInfo;
import com.atguigu.tingshu.model.user.UserPaidTrack;
import com.atguigu.tingshu.user.service.UserInfoService;
import com.atguigu.tingshu.vo.user.UserInfoVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "用户管理接口")
@RestController
@RequestMapping("api/user/userInfo")
@SuppressWarnings({"unchecked", "rawtypes"})
public class UserInfoApiController {

	@Autowired
	private UserInfoService userInfoService;

	@GetMapping("/getUserInfoById/{userId}")
	public Result<UserInfoVo> getUserInfoById(@PathVariable("userId") Long userId) {
		UserInfoVo userInfoVo = this.userInfoService.getUserInfo(userId);
		return Result.ok(userInfoVo);
	}

	@GuiGuLogin
	@Operation(summary = "根据专辑id查询当前用户是否购买过该专辑")
	@GetMapping("/getPaidAlbumStat/{albumId}")
	public Result<Boolean> getPaidAlbumStatByAlbumIdAndUserId(@PathVariable("albumId") Long albumId){
		Long userId = AuthContextHolder.getUserId();
		Boolean flag = userInfoService.getPaidAlbumStatByAlbumIdAndUserId(albumId,userId);
		return Result.ok(flag);
	}

	@Operation(summary = "根据专辑id查询当前用户购买过该专辑下的声音列表")
	@GuiGuLogin
	@GetMapping("/getPaidTracks/{albumId}")
	public Result<List<UserPaidTrack>> getPaidTracksByAlbumIdAndUserId(@PathVariable Long albumId){
		Long userId = AuthContextHolder.getUserId();
		List<UserPaidTrack> userPaidTracks = userInfoService.getPaidTracksByAlbumIdAndUserId(albumId, userId);
		return Result.ok(userPaidTracks);
	}

	@Operation(summary = "更新过期vip状态")
	@GetMapping("updateExpiredVipStatus")
	public Result updateExpiredVipStatus() {
		this.userInfoService.updateExpiredVipStatus();
		return Result.ok();
	}

}

