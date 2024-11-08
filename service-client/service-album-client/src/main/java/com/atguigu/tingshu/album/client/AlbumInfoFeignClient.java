package com.atguigu.tingshu.album.client;

import com.atguigu.tingshu.album.client.impl.AlbumInfoDegradeFeignClient;
import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.model.album.AlbumAttributeValue;
import com.atguigu.tingshu.model.album.AlbumInfo;
import com.atguigu.tingshu.vo.album.AlbumListVo;
import com.atguigu.tingshu.vo.album.AlbumStatVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * <p>
 * 产品列表API接口
 * </p>
 *
 * @author qy
 */
@FeignClient(value = "service-album", fallback = AlbumInfoDegradeFeignClient.class)
public interface AlbumInfoFeignClient {
    @PostMapping("api/album/albumInfo/findAllAlbumPage/{page}/{limit}")
    Result<List<AlbumListVo>> findAllAlbumPage(@PathVariable("page") Integer page, @PathVariable("limit") Integer limit);

    @GetMapping("api/album/albumInfo/getAlbumInfo/{id}")
    Result<AlbumInfo> getAlbumInfoById(@PathVariable Long id);

    @PostMapping("api/album/albumInfo/findAlbumInfoAttributeValuesByAlbumInfoId/{albumInfoId}")
    Result<List<AlbumAttributeValue>> findAlbumInfoAttributeValuesByAlbumInfoId(@PathVariable("albumInfoId") Long albumInfoId);

    @GetMapping("api/album/albumInfo/getAlbumState/{albumId}")
    public Result<AlbumStatVo> getAlbumStateByAlbumId(@PathVariable("albumId")Long albumId);

    @GetMapping("api/album/albumInfo/findLatelyUpdateAlbum/{startTime}/{endTime}")
    public Result<List<Long>> findLatelyUpdateAlbum(@PathVariable("startTime")String startTime, @PathVariable("endTime")String endTime);

    @PostMapping("api/album/albumInfo/findAlbumStatVoList")
    public Result<List<AlbumStatVo>> findAlbumStatVoList(@RequestBody List<Long> albumIds);
}