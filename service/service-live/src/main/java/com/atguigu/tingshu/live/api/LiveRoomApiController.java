package com.atguigu.tingshu.live.api;

import com.atguigu.tingshu.common.login.GuiGuLogin;
import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.common.util.AuthContextHolder;
import com.atguigu.tingshu.live.service.LiveRoomService;
import com.atguigu.tingshu.model.live.LiveRoom;
import com.atguigu.tingshu.vo.live.LiveRoomVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/live/liveRoom")
@SuppressWarnings({"unchecked", "rawtypes"})
public class LiveRoomApiController {

	@Autowired
	private LiveRoomService liveRoomService;

	@GuiGuLogin
	@GetMapping("/getCurrentLive")
	public Result<LiveRoom> getCurrentLive() {
		Long userId = AuthContextHolder.getUserId();
		LiveRoom liveRoom = liveRoomService.getOne(new LambdaQueryWrapper<LiveRoom>().eq(LiveRoom::getUserId,userId).ge(LiveRoom::getExpireTime,new Date()));
		return Result.ok(liveRoom);
	}

	@Operation(summary = "保存直播间")
	@PostMapping("saveLiveRoom")
	@GuiGuLogin
	public Result<LiveRoom> saveLiveRoom(@RequestBody @Validated LiveRoomVo liveRoomVo) {
		LiveRoom liveRoom = this.liveRoomService.saveLiveRoom(liveRoomVo);
		return Result.ok(liveRoom);
	}

	@GuiGuLogin
	@Operation(summary = "根据id查询直播间")
	@GetMapping("/getById/{userId}")
	public Result<LiveRoom> getById(@PathVariable("userId") Long userId) {
		LiveRoom byId = liveRoomService.getById(userId);
		return Result.ok(byId);
	}

	@GetMapping("/findLiveList")
	@Operation(summary = "获取当前直播列表")
	public Result<List<LiveRoom>> findLiveList() {
		List<LiveRoom> listRoom = liveRoomService.list(new LambdaQueryWrapper<LiveRoom>().gt(LiveRoom::getExpireTime, new Date()));
		return Result.ok(listRoom);
	}

}

