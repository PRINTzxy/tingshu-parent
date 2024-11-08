package com.atguigu.tingshu.search.service;

import com.atguigu.tingshu.query.album.AlbumInfoQuery;
import com.atguigu.tingshu.query.search.AlbumIndexQuery;
import com.atguigu.tingshu.vo.search.AlbumInfoIndexVo;
import com.atguigu.tingshu.vo.search.AlbumSearchResponseVo;

import java.util.List;
import java.util.Map;

public interface SearchService {


    List<Map<String, Object>> channel(Long category1Id) throws Exception;

    AlbumSearchResponseVo search(AlbumIndexQuery albumIndexQuery);

    List<String> completeSuggest(String keyword);

    List<AlbumInfoIndexVo> findRankingList(Long category1Id, String field);

    void updateLatelyAlbumStat();
}
