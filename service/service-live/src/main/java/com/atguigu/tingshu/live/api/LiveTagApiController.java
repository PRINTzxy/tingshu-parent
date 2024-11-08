package com.atguigu.tingshu.live.api;

import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.live.service.LiveTagService;
import com.atguigu.tingshu.model.live.LiveTag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/live/liveTag")
@SuppressWarnings({"unchecked", "rawtypes"})
public class LiveTagApiController {

	@Autowired
	private LiveTagService liveTagService;

	@GetMapping("/findAllLiveTag")
	public Result<List<LiveTag>> findAllLiveTag() {
		List<LiveTag> liveTags = this.liveTagService.list();
		return Result.ok(liveTags);
	}

}

