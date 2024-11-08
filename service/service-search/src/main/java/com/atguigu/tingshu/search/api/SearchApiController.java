package com.atguigu.tingshu.search.api;

import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.query.album.AlbumInfoQuery;
import com.atguigu.tingshu.query.search.AlbumIndexQuery;
import com.atguigu.tingshu.search.service.SearchService;
import com.atguigu.tingshu.vo.search.AlbumInfoIndexVo;
import com.atguigu.tingshu.vo.search.AlbumSearchResponseVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "搜索专辑管理")
@RestController
@RequestMapping("api/search/albumInfo")
@SuppressWarnings({"unchecked", "rawtypes"})
public class SearchApiController {

    @Autowired
    private SearchService searchService;

    /*@Operation(summary = "商品上架")
    @GetMapping("upper/album/{albumId}")
    public Result upperAlbum(@PathVariable("albumId") Long albumId) {
        this.searchService.upperAlbum(albumId);
        return Result.ok();
    }*/

    @GetMapping("/channel/{category1Id}")
    public Result<List<Map<String,Object>>> channel(@PathVariable("category1Id") Long category1Id) throws Exception{
        List<Map<String, Object>> mapList = this.searchService.channel(category1Id);
        return Result.ok(mapList);

    }

    //搜索
    @PostMapping
    public Result search(@RequestBody AlbumIndexQuery albumIndexQuery){
        AlbumSearchResponseVo responseVo = this.searchService.search(albumIndexQuery);
        return Result.ok(responseVo);
    }

    @GetMapping("/completeSuggest/{keyword}")
    public Result<List<String>> completeSuggest(@PathVariable("keyword")String keyword){
        List<String> list = this.searchService.completeSuggest(keyword);
        return Result.ok(list);
    }

    @Operation(summary = "获取排行榜列表")
    @Parameters({
            @Parameter(name = "category1Id", description = "一级分类",in = ParameterIn.PATH,required = true),
            @Parameter(name = "field", description = "热度:hotScore、播放量:playStatNum、订阅量:subscribeStatNum、购买量:buyStatNum、评论数:albumCommentStatNum",required = true,in = ParameterIn.PATH),
    })
    @GetMapping("/findRankingList/{category1Id}/{field}")
    public Result<List<AlbumInfoIndexVo>> findRankingList(@PathVariable("category1Id") Long category1Id, @PathVariable("field") String field) {
        //  调用服务层方法
        List<AlbumInfoIndexVo> infoIndexVoList = searchService.findRankingList(category1Id, field);
        //  返回结果集
        return Result.ok(infoIndexVoList);
    }

    @Operation(summary = "更新最近的专辑统计信息到es")
    @GetMapping("updateLatelyAlbumStat")
    public Result updateLatelyAlbumStat(){
        this.searchService.updateLatelyAlbumStat();
        return Result.ok();
    }



}

