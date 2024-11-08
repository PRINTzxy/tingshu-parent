package com.atguigu.tingshu.search.service;

import com.atguigu.tingshu.vo.album.AlbumStatVo;
import com.atguigu.tingshu.vo.search.AlbumItemVo;

import java.util.Map;

public interface ItemService {


    Map<String, Object> loadItem(Long albumId);
}
