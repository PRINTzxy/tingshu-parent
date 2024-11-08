package com.atguigu.tingshu.album.service;

import com.atguigu.tingshu.model.album.AlbumInfo;
import com.atguigu.tingshu.model.album.TrackInfo;
import com.atguigu.tingshu.query.album.TrackInfoQuery;
import com.atguigu.tingshu.vo.album.AlbumTrackListVo;
import com.atguigu.tingshu.vo.album.TrackInfoVo;
import com.atguigu.tingshu.vo.album.TrackListVo;
import com.atguigu.tingshu.vo.album.TrackStatVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

public interface TrackInfoService extends IService<TrackInfo> {

    void saveTrackInfo(TrackInfoVo trackInfoVo);

    IPage<TrackListVo> findUserTrackPage(Page<Object> trackListVoPage, TrackInfoQuery trackInfoQuery);

    void updateTrackInfo(Long id, TrackInfoVo trackInfoVo);

    void removeTrackInfo(Long id);

    IPage<AlbumTrackListVo> findTrackByAlbumIdAndPage(Long albumId, Page<AlbumTrackListVo> albumTrackListVoPage);

    TrackStatVo getTrackStatVo(Long trackId);

    Map<String, Object> getPlayToken(Long trackId);
}
