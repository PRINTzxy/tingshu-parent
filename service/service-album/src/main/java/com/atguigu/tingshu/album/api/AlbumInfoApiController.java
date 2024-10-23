package com.atguigu.tingshu.album.api;

import com.atguigu.tingshu.album.service.AlbumInfoService;
import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.model.album.AlbumInfo;
import com.atguigu.tingshu.query.album.AlbumInfoQuery;
import com.atguigu.tingshu.vo.album.AlbumInfoVo;
import com.atguigu.tingshu.vo.album.AlbumListVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "专辑管理")
@RestController
@RequestMapping("api/album/albumInfo")
@SuppressWarnings({"unchecked", "rawtypes"})
public class AlbumInfoApiController {

	@Autowired
	private AlbumInfoService albumInfoService;

	@Operation(summary = "新增专辑")
	@PostMapping("/saveAlbumInfo")
	public Result saveAlbumInfo(@RequestBody @Validated AlbumInfoVo albumInfoVo) {
		albumInfoService.saveAlbumInfo(albumInfoVo);
		return Result.ok();
	}

	@Operation(summary = "获取当前用户专辑列表")
	@PostMapping("/findUserAlbumPage/{page}/{limit}")
	public Result<IPage<AlbumListVo>> findUserAlbumPage(@Parameter(name = "page", description = "页码", required = true) @PathVariable("page") Integer page,
													   @Parameter(name = "limit", description = "条数", required = true) @PathVariable("limit") Integer limit,
													   @Parameter(name = "albumInfoQuery", description = "查询条件", required = false) @RequestBody AlbumInfoQuery albumInfoQuery){
		IPage<AlbumListVo> pages = albumInfoService.findUserAlbumPage(page,limit,albumInfoQuery);
		return Result.ok(pages);
	}

	@Operation(summary = "删除专辑信息")
	@DeleteMapping("/removeAlbumInfo/{albumId}")
	public Result removeAlbumInfo(@PathVariable("albumId") Long albumId) {
		albumInfoService.removeAlbumInfo(albumId);
		return Result.ok();
	}

	@Operation(summary = "编辑专辑信息")
	@GetMapping("/getAlbumInfo/{albumId}")
	public Result<AlbumInfo> getAlbumInfo(@PathVariable("albumId") Long albumId) {
		AlbumInfo albumInfo = albumInfoService.getAlbumInfo(albumId);
		return Result.ok(albumInfo);
	}

	@Operation(summary = "修改专辑信息")
	@PutMapping("/updateAlbumInfo/{albumId}")
	public Result<AlbumInfo> updateAlbumInfo(@PathVariable("albumId") Long albumId, @RequestBody AlbumInfoVo albumInfoVo) {
		albumInfoService.updateAlbumInfo(albumId,albumInfoVo);
		return Result.ok();
	}

	@Operation(summary = "查询用户声音列表")
	@GetMapping("/findUserAllAlbumList")
	public Result findUserAllAlbumList() {
		List<AlbumInfo> list = albumInfoService.findUserAllAlbumList();
		return Result.ok(list);
	}


}

