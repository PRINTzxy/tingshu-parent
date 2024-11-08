package com.atguigu.tingshu.album.api;

import com.atguigu.tingshu.album.service.AlbumInfoService;
import com.atguigu.tingshu.common.login.GuiGuLogin;
import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.model.album.AlbumAttributeValue;
import com.atguigu.tingshu.model.album.AlbumInfo;
import com.atguigu.tingshu.query.album.AlbumInfoQuery;
import com.atguigu.tingshu.vo.album.AlbumInfoVo;
import com.atguigu.tingshu.vo.album.AlbumListVo;
import com.atguigu.tingshu.vo.album.AlbumStatVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.FileNotFoundException;
import java.util.List;

@Tag(name = "专辑管理")
@RestController
@RequestMapping("api/album/albumInfo")
@SuppressWarnings({"unchecked", "rawtypes"})
public class AlbumInfoApiController {

	@Autowired
	private AlbumInfoService albumInfoService;

	@GuiGuLogin
	@Operation(summary = "新增专辑")
	@PostMapping("/saveAlbumInfo")
	public Result saveAlbumInfo(@RequestBody @Validated AlbumInfoVo albumInfoVo) throws FileNotFoundException {
		albumInfoService.saveAlbumInfo(albumInfoVo);
		return Result.ok();
	}

	@GuiGuLogin
	@Operation(summary = "获取当前用户专辑列表")
	@PostMapping("/findUserAlbumPage/{page}/{limit}")
	public Result<IPage<AlbumListVo>> findUserAlbumPage(@Parameter(name = "page", description = "页码", required = true) @PathVariable("page") Integer page,
													   @Parameter(name = "limit", description = "条数", required = true) @PathVariable("limit") Integer limit,
													   @Parameter(name = "albumInfoQuery", description = "查询条件", required = false) @RequestBody AlbumInfoQuery albumInfoQuery){
		IPage<AlbumListVo> pages = albumInfoService.findUserAlbumPage(page,limit,albumInfoQuery);
		return Result.ok(pages);
	}

	@GuiGuLogin
	@Operation(summary = "删除专辑信息")
	@DeleteMapping("/removeAlbumInfo/{albumId}")
	public Result removeAlbumInfo(@PathVariable("albumId") Long albumId) {
		albumInfoService.removeAlbumInfo(albumId);
		return Result.ok();
	}



	@Operation(summary = "获取专辑信息")
	@GetMapping("/getAlbumInfo/{albumId}")
	public Result<AlbumInfo> getAlbumInfo(@PathVariable("albumId") Long albumId) {
		AlbumInfo albumInfo = albumInfoService.getAlbumInfo(albumId);
		return Result.ok(albumInfo);
	}


	@GuiGuLogin
	@Operation(summary = "修改专辑信息")
	@PutMapping("/updateAlbumInfo/{albumId}")
	public Result<AlbumInfo> updateAlbumInfo(@PathVariable("albumId") Long albumId, @RequestBody AlbumInfoVo albumInfoVo) {
		albumInfoService.updateAlbumInfo(albumId,albumInfoVo);
		return Result.ok();
	}

	@GuiGuLogin
	@Operation(summary = "查询用户声音列表")
	@GetMapping("/findUserAllAlbumList")
	public Result findUserAllAlbumList() {
		List<AlbumInfo> list = albumInfoService.findUserAllAlbumList();
		return Result.ok(list);
	}


	@Operation(summary = "分页查询所有专辑信息")
	@PostMapping("findAllAlbumPage/{page}/{limit}")
	public Result<List<AlbumListVo>> findAllAlbumPage(@Parameter(name = "page", description = "页码", required = true) @PathVariable("page") Integer page,
			@PathVariable("limit") Integer limit) {
		IPage<AlbumListVo> albumInfoPage = this.albumInfoService.findAllAlbumPage(page, limit);
		List<AlbumListVo> records = albumInfoPage.getRecords();
		return Result.ok(records);
	}

	@Operation(summary = "查询专辑属性信息")
	@PostMapping("findAlbumInfoAttributeValuesByAlbumInfoId/{albumInfoId}")
	public Result<List<AlbumAttributeValue>> findAlbumInfoAttributeValueByAlbumInfoId(@PathVariable("albumInfoId") Long albumInfoId){
		//调用服务层保存方法
		List<AlbumAttributeValue> albumAttributeValues = this.albumInfoService.findAlbumInfoAttributeValueByAlbumInfoId(albumInfoId);
		return Result.ok(albumAttributeValues);
	}

	@Operation(summary = "获取当前用户全部专辑列表")
	@GetMapping("/getAlbumState/{albumId}")
	public Result<AlbumStatVo> getAlbumStateByAlbumId(@PathVariable Long albumId){
		AlbumStatVo albumStatVo = albumInfoService.getAlbumStateByAlbumId(albumId);
		return Result.ok(albumStatVo);
	}

	@Operation(summary = "查询统计信息最近更新过的专辑id列表")
	@GetMapping("findLatelyUpdateAlbum/{startTime}/{endTime}")
	public Result<List<Long>> findLatelyUpdateAlbum(@PathVariable("startTime")String startTime, @PathVariable("endTime")String endTime){
		List<Long> albumIds = albumInfoService.findLatelyUpdateAlbum(startTime, endTime);
		return Result.ok(albumIds);
	}

	@Operation(summary = "根据专辑id列表查询统计信息")
	@PostMapping("findAlbumStatVoList")
	public Result<List<AlbumStatVo>> findAlbumStatVoList(@RequestBody List<Long> albumIds){
		List<AlbumStatVo> albumStatVos = this.albumInfoService.findAlbumStatVoList(albumIds);
		return Result.ok(albumStatVos);
	}
}

