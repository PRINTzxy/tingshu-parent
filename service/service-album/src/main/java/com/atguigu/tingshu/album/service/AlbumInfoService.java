package com.atguigu.tingshu.album.service;

import com.atguigu.tingshu.model.album.AlbumAttributeValue;
import com.atguigu.tingshu.model.album.AlbumInfo;
import com.atguigu.tingshu.query.album.AlbumInfoQuery;
import com.atguigu.tingshu.vo.album.AlbumInfoVo;
import com.atguigu.tingshu.vo.album.AlbumListVo;
import com.atguigu.tingshu.vo.album.AlbumStatVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.apache.ibatis.annotations.Param;

import java.io.FileNotFoundException;
import java.util.List;

public interface AlbumInfoService extends IService<AlbumInfo> {


    void saveAlbumInfo(AlbumInfoVo albumInfoVo) throws FileNotFoundException;

    IPage<AlbumListVo> findUserAlbumPage(Integer page, Integer limit, AlbumInfoQuery albumInfoQuery);

    void removeAlbumInfo(@Param("albumId") Long albumId);


    AlbumInfo getAlbumInfo(Long albumId);

    void updateAlbumInfo(Long albumId, AlbumInfoVo albumInfoVo);

    List<AlbumInfo> findUserAllAlbumList();

    IPage<AlbumListVo> findAllAlbumPage(Integer page, Integer limit);

    List<AlbumAttributeValue> findAlbumInfoAttributeValueByAlbumInfoId(Long albumInfoId);

    AlbumStatVo getAlbumStateByAlbumId(Long albumId);

    List<Long> findLatelyUpdateAlbum(String startTime, String endTime);

    List<AlbumStatVo> findAlbumStatVoList(List<Long> albumIds);
}
