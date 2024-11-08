package com.atguigu.tingshu.album.mapper;

import com.atguigu.tingshu.model.album.AlbumStat;
import com.atguigu.tingshu.vo.album.AlbumStatVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AlbumStatMapper extends BaseMapper<AlbumStat> {


    AlbumStatVo getAlbumStateByAlbumId(Long albumId);

    List<AlbumStatVo> findAlbumStatVoList(@Param("albumIds") List<Long> albumIds);
}
