package com.atguigu.tingshu.album.service;

import com.atguigu.tingshu.model.album.AlbumInfo;
import com.atguigu.tingshu.query.album.AlbumInfoQuery;
import com.atguigu.tingshu.vo.album.AlbumInfoVo;
import com.atguigu.tingshu.vo.album.AlbumListVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AlbumInfoService extends IService<AlbumInfo> {


    void saveAlbumInfo(AlbumInfoVo albumInfoVo);

    IPage<AlbumListVo> findUserAlbumPage(Integer page, Integer limit, AlbumInfoQuery albumInfoQuery);

    void removeAlbumInfo(@Param("albumId") Long albumId);


    AlbumInfo getAlbumInfo(Long albumId);

    void updateAlbumInfo(Long albumId, AlbumInfoVo albumInfoVo);

    List<AlbumInfo> findUserAllAlbumList();
}
