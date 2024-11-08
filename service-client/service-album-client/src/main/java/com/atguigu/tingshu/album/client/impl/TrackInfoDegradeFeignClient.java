package com.atguigu.tingshu.album.client.impl;


import com.atguigu.tingshu.album.client.TrackInfoFeignClient;
import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.vo.album.AlbumTrackListVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.stereotype.Component;

@Component
public class TrackInfoDegradeFeignClient implements TrackInfoFeignClient {
    @Override
    public Result<IPage<AlbumTrackListVo>> findTrackByAlbumIdAndPage(Long albumId, Long page, Long limit) {
        return null;
    }
}
