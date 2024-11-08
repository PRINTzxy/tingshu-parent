package com.atguigu.tingshu.vo.search;

import com.atguigu.tingshu.model.album.AlbumInfo;
import com.atguigu.tingshu.model.album.BaseCategoryView;
import com.atguigu.tingshu.model.user.UserInfo;
import com.atguigu.tingshu.vo.album.AlbumStatVo;
import lombok.Data;

@Data
public class AlbumItemVo {

    private UserInfo announcer;

    private AlbumInfo albumInfo;

    private AlbumStatVo albumStatVo;

    private BaseCategoryView baseCategoryView;
}
