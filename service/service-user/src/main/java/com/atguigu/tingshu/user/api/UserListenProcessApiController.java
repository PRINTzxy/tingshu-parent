package com.atguigu.tingshu.user.api;

import com.atguigu.tingshu.common.login.GuiGuLogin;
import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.user.service.UserListenProcessService;
import com.atguigu.tingshu.vo.user.UserListenProcessVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@Tag(name = "用户声音播放进度管理接口")
@RestController
@RequestMapping("api/user/userListenProcess")
@SuppressWarnings({"unchecked", "rawtypes"})
public class UserListenProcessApiController {

	@Autowired
	private UserListenProcessService userListenProcessService;

	@GuiGuLogin
	@Operation(summary = "获取播放进度")
	@GetMapping("getTrackBreakSecond/{trackId}")
	public Result<BigDecimal> getTrackBreakSecond(@PathVariable("trackId") Long trackId) {
		BigDecimal trackBreakSecond = userListenProcessService.getTrackBreakSecond(trackId);
		return Result.ok(trackBreakSecond);
	}

	@GuiGuLogin
	@Operation(summary = "更新播放进度")
	@PostMapping("updateListenProcess")
	public Result updateListenProcess(@RequestBody UserListenProcessVo userListenProcessVo) {
		userListenProcessService.updateListenProcess(userListenProcessVo);
		return Result.ok();
	}

	@GuiGuLogin
	@Operation(summary = "获取最近一次播放声音")
	@GetMapping("getLatelyTrack")
	public Result getLatelyTrack(){
		// 获取播放记录
		Map<String,Object> map = this.userListenProcessService.getLatelyTrack();
		return Result.ok(map);
	}

}

