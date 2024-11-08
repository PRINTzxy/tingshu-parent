package com.atguigu.tingshu;

import com.atguigu.tingshu.album.client.AlbumInfoFeignClient;
import com.atguigu.tingshu.album.client.CategoryFeignClient;
import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.common.util.PinYinUtils;
import com.atguigu.tingshu.model.album.AlbumAttributeValue;
import com.atguigu.tingshu.model.album.BaseCategoryView;
import com.atguigu.tingshu.model.search.AlbumInfoIndex;
import com.atguigu.tingshu.model.search.AttributeValueIndex;
import com.atguigu.tingshu.model.search.SuggestIndex;
import com.atguigu.tingshu.model.user.UserInfo;
import com.atguigu.tingshu.user.client.UserInfoFeignClient;
import com.atguigu.tingshu.vo.album.AlbumListVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Assert;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.suggest.Completion;

import java.util.List;
import java.util.stream.Collectors;


@SpringBootTest
public class ServiceSearchApplicationTest {
    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;
    @Qualifier("com.atguigu.tingshu.album.client.AlbumInfoFeignClient")
    @Autowired
    @Resource
    private AlbumInfoFeignClient albumInfoFeignClient;
    @Resource
    private UserInfoFeignClient userInfoFeignClient;
    @Resource
    private CategoryFeignClient categoryFeignClient;

    @Test
    void test(){
        IndexOperations indexOps = this.elasticsearchTemplate.indexOps(AlbumInfoIndex.class);
        if(!indexOps.exists()){
            indexOps.create();
            indexOps.putMapping();
        }
        IndexOperations indexOps1 = this.elasticsearchTemplate.indexOps(SuggestIndex.class);
        if (!indexOps1.exists()){
            indexOps1.create();
            indexOps1.putMapping();
        }

        Integer pageNum = 1;
        Integer pageSize = 100;

        do{
            // 分页查询专辑
            Result<List<AlbumListVo>> albumResult = this.albumInfoFeignClient.findAllAlbumPage(pageNum, pageSize);
            Assert.notNull(albumResult.getData(), "查询专辑结果集为空");
            List<AlbumListVo> albumListVos = albumResult.getData();
            // 如果为空说明到了最后一页，导入程序可以终止
            if (CollectionUtils.isEmpty(albumListVos)){
                return;
            }

            /// 把专辑实体类集合 转化成 索引实体类AlbumInfoIndex集合。并保存到索引库
            List<AlbumInfoIndex> albumInfoIndexList = albumListVos.stream().map(albumListVo -> {
                AlbumInfoIndex albumInfoIndex = new AlbumInfoIndex();
                // 属性复制
                BeanUtils.copyProperties(albumListVo, albumInfoIndex);
                albumInfoIndex.setId(albumListVo.getAlbumId());

                // 设置专辑所属用户名
                Result<UserInfo> userInfoResult = this.userInfoFeignClient.getUserById(albumListVo.getUserId());
                Assert.notNull(userInfoResult, "获取用户信息失败！");
                UserInfo userInfo = userInfoResult.getData();
                if (userInfo != null) {
                    albumInfoIndex.setAnnouncerName(userInfo.getNickname());
                }

                // 设置分类参数
                Result<BaseCategoryView> baseCategoryViewResult = this.categoryFeignClient.findBaseCategoryViewByCategory3Id(albumListVo.getCategory3Id());
                Assert.notNull(baseCategoryViewResult, "获取分类信息失败！");
                BaseCategoryView baseCategoryView = baseCategoryViewResult.getData();
                Assert.notNull(baseCategoryView, "获取分类信息失败！");
                albumInfoIndex.setCategory1Id(baseCategoryView.getCategory1Id());
                albumInfoIndex.setCategory2Id(baseCategoryView.getCategory2Id());

                // 设置专辑属性值信息
                Result<List<AlbumAttributeValue>> albumAttributeValueResult = this.albumInfoFeignClient.findAlbumInfoAttributeValuesByAlbumInfoId(albumListVo.getAlbumId());
                Assert.notNull(albumAttributeValueResult, "专辑属性值获取失败！");
                List<AlbumAttributeValue> albumAttributeValues = albumAttributeValueResult.getData();
                if (!CollectionUtils.isEmpty(albumAttributeValues)){
                    // 把专辑属性AlbumAttributeValue集合 转化成 AttributeValueIndex集合
                    albumInfoIndex.setAttributeValueIndexList(albumAttributeValues.stream().map(albumAttributeValue -> {
                        AttributeValueIndex attributeValueIndex = new AttributeValueIndex();
                        BeanUtils.copyProperties(albumAttributeValue, attributeValueIndex);
                        return attributeValueIndex;
                    }).collect(Collectors.toList()));
                }
                // 自动补全-标题
                SuggestIndex titleSuggestIndex = new SuggestIndex();
                titleSuggestIndex.setId(null);
                titleSuggestIndex.setTitle(albumListVo.getAlbumTitle());
                titleSuggestIndex.setKeyword(new Completion(new String[]{albumListVo.getAlbumTitle()}));
                titleSuggestIndex.setKeywordPinyin(new Completion(new String[]{PinYinUtils.toHanyuPinyin(albumListVo.getAlbumTitle())}));
                titleSuggestIndex.setKeywordSequence(new Completion(new String[]{PinYinUtils.getFirstLetter(albumListVo.getAlbumTitle())}));
                this.elasticsearchTemplate.save(titleSuggestIndex);
// 自动补全-主播
                if (userInfo != null){
                    SuggestIndex announcerSuggestIndex = new SuggestIndex();
                    announcerSuggestIndex.setId(null);
                    announcerSuggestIndex.setTitle(userInfo.getNickname());
                    announcerSuggestIndex.setKeyword(new Completion(new String[]{userInfo.getNickname()}));
                    announcerSuggestIndex.setKeywordPinyin(new Completion(new String[]{PinYinUtils.toHanyuPinyin(userInfo.getNickname())}));
                    announcerSuggestIndex.setKeywordSequence(new Completion(new String[]{PinYinUtils.getFirstLetter(userInfo.getNickname())}));
                    this.elasticsearchTemplate.save(announcerSuggestIndex);
                }

                return albumInfoIndex;
            }).collect(Collectors.toList());
            // 保存到索引库
            this.elasticsearchTemplate.save(albumInfoIndexList);

            // 下一页
            pageSize = albumListVos.size();
            pageNum++;


        }while (pageSize == 100); //如果没有100条说明到最后一页了，退出循环

    }

}
