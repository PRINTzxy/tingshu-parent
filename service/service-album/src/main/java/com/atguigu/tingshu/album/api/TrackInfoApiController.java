package com.atguigu.tingshu.album.api;

import com.atguigu.tingshu.album.service.TrackInfoService;
import com.atguigu.tingshu.album.service.VodService;
import com.atguigu.tingshu.album.service.impl.VodServiceImpl;
import com.atguigu.tingshu.common.login.GuiGuLogin;
import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.common.util.AuthContextHolder;
import com.atguigu.tingshu.model.album.AlbumInfo;
import com.atguigu.tingshu.model.album.TrackInfo;
import com.atguigu.tingshu.query.album.TrackInfoQuery;
import com.atguigu.tingshu.vo.album.AlbumTrackListVo;
import com.atguigu.tingshu.vo.album.TrackInfoVo;
import com.atguigu.tingshu.vo.album.TrackListVo;
import com.atguigu.tingshu.vo.album.TrackStatVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.querydsl.QPageRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.sound.midi.Track;
import java.util.List;
import java.util.Map;

@Tag(name = "声音管理")
@RestController
@RequestMapping("api/album/trackInfo")
@SuppressWarnings({"unchecked", "rawtypes"})
public class TrackInfoApiController {

	@Autowired
	private TrackInfoService trackInfoService;
    @Autowired
    private VodService vodService;

	@Operation(summary = "上传声音")
	@PostMapping("/uploadTrack")
	public Result<Map<String,Object>> uploadTrack(MultipartFile file){
		Map<String,Object> map = vodService.uploadTrack(file);
		return Result.ok(map);
	}

	@GuiGuLogin
	@Operation(summary = "新增声音")
	@PostMapping("/saveTrackInfo")
	public Result saveTrackInfo(@RequestBody TrackInfoVo trackInfoVo){
		this.trackInfoService.saveTrackInfo(trackInfoVo);
		return Result.ok();
	}

	@GuiGuLogin
	@Operation(summary = "获取当前用户声音分页列表")
	@PostMapping("/findUserTrackPage/{page}/{limit}")
	public Result<IPage<TrackListVo>> findUserTrackPage(@Parameter(name = "page",description = "页码", required = true) @PathVariable("page")Long page,
														@Parameter(name = "limit",description = "条数",required = true) @PathVariable("limit")Long limit,
														@Parameter(name = "trackInfoQuery",description = "查询对象",required = false) @RequestBody TrackInfoQuery trackInfoQuery){
		trackInfoQuery.setUserId(AuthContextHolder.getUserId() == null?1:AuthContextHolder.getUserId());

		Page<Object> trackListVoPage = new Page<>(page, limit);
		IPage<TrackListVo> trackListVoIPage = trackInfoService.findUserTrackPage(trackListVoPage,trackInfoQuery);
		return Result.ok(trackListVoIPage);
	}

	@Operation(summary = "根据id查询声音")
	@GetMapping("getTrackInfo/{id}")
	public Result<TrackInfo> getTrackInfo(@PathVariable Long id){
		TrackInfo trackInfo = this.trackInfoService.getById(id);
		return Result.ok(trackInfo);
	}

	@GuiGuLogin
	@Operation(summary = "修改声音")
	@PutMapping("updateTrackInfo/{id}")
	public Result updateById(@PathVariable Long id, @RequestBody @Validated TrackInfoVo trackInfoVo) {
		//	调用服务层方法
		this.trackInfoService.updateTrackInfo(id, trackInfoVo);
		return Result.ok();
	}

	@GuiGuLogin
	@Operation(summary = "删除声音")
	@DeleteMapping("removeTrackInfo/{id}")
	public Result removeTrackInfo(@PathVariable Long id){
		this.trackInfoService.removeTrackInfo(id);
		return Result.ok();
	}


	@GetMapping("/findAlbumTrackPage/{albumId}/{page}/{limit}")
	public Result<IPage<AlbumTrackListVo>> findTrackByAlbumIdAndPage(
			@PathVariable Long albumId,
			@PathVariable Long page,
			@PathVariable Long limit){
		IPage<AlbumTrackListVo> trackListVoIPage = this.trackInfoService.findTrackByAlbumIdAndPage(albumId,new Page<AlbumTrackListVo>(page,limit));
		return Result.ok(trackListVoIPage);

	}

	@GetMapping("getTrackStatVo/{trackId}")
	public Result<TrackStatVo> getTrackStatVo(@PathVariable Long trackId){
		TrackStatVo statVo = this.trackInfoService.getTrackStatVo(trackId);
		return Result.ok(statVo);
	}

	@GuiGuLogin(required = false)
	@Operation(summary = "获取声音播放凭证")
	@GetMapping("getPlayToken/{trackId}")
	public Result<Map<String,Object>> getPlayToken(@PathVariable Long trackId){
		Map<String,Object> map = trackInfoService.getPlayToken(trackId);
		return Result.ok(map);
	}


}

