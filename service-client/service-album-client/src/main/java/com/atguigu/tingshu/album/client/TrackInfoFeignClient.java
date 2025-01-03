package com.atguigu.tingshu.album.client;

import com.atguigu.tingshu.album.client.impl.TrackInfoDegradeFeignClient;
import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.vo.album.AlbumTrackListVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * <p>
 * 产品列表API接口
 * </p>
 *
 * @author qy
 */
@FeignClient(value = "service-album", fallback = TrackInfoDegradeFeignClient.class)
public interface TrackInfoFeignClient {
    @GetMapping("api/album/trackInfo/findAlbumTrackPage/{albumId}/{page}/{limit}")
    Result<IPage<AlbumTrackListVo>> findTrackByAlbumIdAndPage(
            @PathVariable("albumId") Long albumId,
            @PathVariable("page") Long page,
            @PathVariable("limit") Long limit);


}