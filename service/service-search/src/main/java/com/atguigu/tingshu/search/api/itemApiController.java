package com.atguigu.tingshu.search.api;

import com.atguigu.tingshu.common.login.GuiGuLogin;
import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.search.service.ItemService;
import com.atguigu.tingshu.vo.album.AlbumStatVo;
import com.atguigu.tingshu.vo.search.AlbumItemVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "专辑详情管理")
@RestController
@RequestMapping("api/search/albumInfo")
@SuppressWarnings({"unchecked", "rawtypes"})
public class itemApiController {

	@Autowired
	private ItemService itemService;

	@GetMapping("/{albumId}")
	@GuiGuLogin(required = false)
	@Operation(summary = "专辑详情页")
	public Result loadItem(@PathVariable("albumId") Long albumId) {
		//获取到专辑详情数据
		Map<String,Object> result = this.itemService.loadItem(albumId);
		//返回数据
		return Result.ok(result);
	}
}

