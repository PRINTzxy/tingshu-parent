package com.atguigu.tingshu.album.client.impl;


import com.atguigu.tingshu.album.client.AlbumInfoFeignClient;
import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.model.album.AlbumAttributeValue;
import com.atguigu.tingshu.model.album.AlbumInfo;
import com.atguigu.tingshu.vo.album.AlbumListVo;
import com.atguigu.tingshu.vo.album.AlbumStatVo;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AlbumInfoDegradeFeignClient implements AlbumInfoFeignClient {
    @Override
    public Result<List<AlbumListVo>> findAllAlbumPage(Integer page, Integer limit) {
        return null;
    }

    @Override
    public Result<AlbumInfo> getAlbumInfoById(Long id) {
        return null;
    }

    @Override
    public Result<List<AlbumAttributeValue>> findAlbumInfoAttributeValuesByAlbumInfoId(Long albumInfoId) {
        return null;
    }

    @Override
    public Result<AlbumStatVo> getAlbumStateByAlbumId(Long albumId) {
        return null;
    }

    @Override
    public Result<List<Long>> findLatelyUpdateAlbum(String startTime, String endTime) {
        return null;
    }

    @Override
    public Result<List<AlbumStatVo>> findAlbumStatVoList(List<Long> albumIds) {
        return null;
    }
}
