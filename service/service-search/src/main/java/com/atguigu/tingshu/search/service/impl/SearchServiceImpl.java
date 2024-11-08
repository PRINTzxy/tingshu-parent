package com.atguigu.tingshu.search.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.HitsMetadata;
import co.elastic.clients.elasticsearch.core.search.Suggestion;
import co.elastic.clients.elasticsearch.nodes.Cpu;
import com.atguigu.tingshu.album.client.AlbumInfoFeignClient;
import com.atguigu.tingshu.album.client.CategoryFeignClient;
import com.atguigu.tingshu.common.constant.RedisConstant;
import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.model.album.BaseCategory3;
import com.atguigu.tingshu.model.search.AlbumInfoIndex;
import com.atguigu.tingshu.model.search.SuggestIndex;
import com.atguigu.tingshu.query.search.AlbumIndexQuery;
import com.atguigu.tingshu.search.service.SearchService;
import com.atguigu.tingshu.vo.album.AlbumStatVo;
import com.atguigu.tingshu.vo.search.AlbumInfoIndexVo;

import com.atguigu.tingshu.vo.search.AlbumSearchResponseVo;
import com.google.common.collect.Lists;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;


@Slf4j
@Service
@SuppressWarnings({"unchecked", "rawtypes"})
public class SearchServiceImpl implements SearchService {
    @Resource
    private CategoryFeignClient categoryFeignClient;
    @Autowired
    private ElasticsearchClient elasticsearchClient;
    @Autowired
    private RedisTemplate redisTemplate;
    @Resource
    private AlbumInfoFeignClient albumInfoFeignClient;
    @Autowired
    private ExecutorService executorService;
    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Override
    public List<Map<String, Object>> channel(Long category1Id) {
        try {
            //根据一级分类ID查询置顶三级分类
            Result<List<BaseCategory3>> topBaseCategory3Result = categoryFeignClient.findTopBaseCategory3(category1Id);
            Assert.notNull(topBaseCategory3Result,"获取三级分类失败"+category1Id); //断言器
            List<BaseCategory3> baseCategory3s = topBaseCategory3Result.getData();
            //三级分类列表为null，直接返回null
            if(CollectionUtils.isEmpty(baseCategory3s)) return null;
            //获取三级分类id集合 FieldValue反射
            List<FieldValue> category3Ids = baseCategory3s.stream().map(baseCategory3 -> FieldValue.of(baseCategory3.getId())).collect(Collectors.toList());
            //为了方便获取三级分类对象，把三级分类list集合转化成map集合：key-category3Id  value-BaseCategory3
            Map<Long, BaseCategory3> baseCategory3Map = baseCategory3s.stream().collect(Collectors.toMap(BaseCategory3::getId, baseCategory3 -> baseCategory3));
            //组装dsl
            SearchRequest request = SearchRequest.of(s -> s.index("albuminfo")
                    .query(q -> q.terms(t -> t.field("category3Id")
                            .terms(ts -> ts.value(category3Ids))))
                    .aggregations("category3IdAgg", agg -> agg.terms(t -> t.field("category3Id"))
                            .aggregations("topHotsAgg", subAgg -> subAgg.topHits(top -> top.sort(st -> st.field(f -> f.field("hotScore").order(SortOrder.Desc))).size(6)))));
            System.out.println(request);
            //执行搜索
            SearchResponse<AlbumInfoIndex> response = elasticsearchClient.search(request, AlbumInfoIndex.class);
            //获取响应结果集中的聚合
            Map<String, Aggregate> aggregations = response.aggregations();
            if (CollectionUtils.isEmpty(aggregations)) return null;
            //获取category3IdAgg聚合
            Aggregate aggregate = aggregations.get("category3IdAgg");
            if (aggregate == null) return null;
            //把桶集合转化成List<Map<String, Object>>
            List<Map<String,Object>> maps = aggregate.lterms().buckets().array().stream().map(
                    bucket->{
                        Map<String,Object> map = new HashMap<>();
                        //桶的key是三级分类id，根据三级分类id获取三级分类对象
                        map.put("baseCategory3",baseCategory3Map.get(bucket.key()));
                        //获取子聚合列表
                        Map<String, Aggregate> subAggs = bucket.aggregations();
                        //子聚合为空则直接返回
                        if (CollectionUtils.isEmpty(subAggs)) return map;
                        //获取top子聚合
                        Aggregate topHotsAgg = subAggs.get("topHotsAgg");
                        if (topHotsAgg == null) return map;
                        //获取top数据集合
                        List<AlbumInfoIndex> albumInfoIndexList = topHotsAgg.topHits().hits().hits().stream().map(hit -> hit.source().to(AlbumInfoIndex.class)).collect(Collectors.toList());
                        map.put("list", albumInfoIndexList);
                        return map;
                    }
            ).collect(Collectors.toList());
            return maps;
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public AlbumSearchResponseVo search(AlbumIndexQuery albumIndexQuery) {
        try {
            // 构建dsl条件
            SearchRequest request = buildDsl(albumIndexQuery);
            // 执行搜索获取响应结果集
            SearchResponse response = this.elasticsearchClient.search(request, AlbumInfoIndex.class);
            // 解析结果集
            AlbumSearchResponseVo responseVo = this.parseResult(response);
            responseVo.setPageNo(albumIndexQuery.getPageNo());
            responseVo.setPageSize(albumIndexQuery.getPageSize());
            responseVo.setTotalPages(responseVo.getTotal() % responseVo.getPageSize() == 0 ?
                    responseVo.getTotal() / responseVo.getPageSize() : responseVo.getTotal() / responseVo.getPageSize() + 1);
            return responseVo;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<String> completeSuggest(String keyword) {
        try {
            // 组装dsl并执行搜索
            SearchResponse<SuggestIndex> response = this.elasticsearchClient
                    .search(s -> s
                            .index("suggestinfo")
                            .suggest(sg -> sg
                                    .suggesters("keywordSuggest", ks -> ks
                                            .prefix(keyword)
                                            .completion(c -> c
                                                    .field("keyword")
                                                    .size(10)
                                                    .skipDuplicates(true)))
                                    .suggesters("keywordPinYinSuggest", ks -> ks
                                            .prefix(keyword)
                                            .completion(c -> c
                                                    .field("keywordPinyin")
                                                    .size(10)
                                                    .skipDuplicates(true)))
                                    .suggesters("keywordSequenceSuggest", ks -> ks
                                            .prefix(keyword)
                                            .completion(c -> c
                                                    .field("keywordSequence")
                                                    .size(10)
                                                    .skipDuplicates(true)))
                            ), SuggestIndex.class);

            // 解析结果集
            List<String> keywordList = this.parseSuggest(response, "keywordSuggest");
            List<String> keywordPinYinList = this.parseSuggest(response, "keywordPinYinSuggest");
            List<String> keywordSequenceList = this.parseSuggest(response, "keywordSequenceSuggest");
            // 整合到一起并去重
            Set<String> set = new HashSet<>();
            set.addAll(keywordList);
            set.addAll(keywordPinYinList);
            set.addAll(keywordSequenceList);
            // 如果数量充足，则直接返回
            if (set.size() >= 10){
                return new ArrayList<>(set);
            }

            // 如果不充足，再去根据结果去搜索相关
            SearchResponse<SuggestIndex> searchResponse = this.elasticsearchClient.search(s -> s
                    .index("suggestinfo")
                    .size(10)
                    .query(q -> q.match(m -> m
                            .field("title")
                            .query(keyword))), SuggestIndex.class);
            // 查询结果集
            List<Hit<SuggestIndex>> hitList = searchResponse.hits().hits();
            // 查询结果集为空，则直接返回
            if (CollectionUtils.isEmpty(hitList)){
                return new ArrayList<>(set);
            }
            // 查询不为空
            hitList.forEach(hit -> {
                set.add(hit.source().getTitle());
                if (set.size() >= 10){
                    return ;
                }
            });

            return new ArrayList<>(set);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<AlbumInfoIndexVo> findRankingList(Long category1Id, String field) {
        try {
            // 根据统计字段降序排列，取前10
            SearchResponse<AlbumInfoIndex> response = this.elasticsearchClient.search(s -> s
                    .index("albuminfo")
                    .query(q -> q.term(t -> t.field("category1Id").value(category1Id)))
                    .sort(t -> t.field(f -> f.field(field).order(SortOrder.Desc)))
                    .size(10), AlbumInfoIndex.class);
            // 获取搜索结果集
            List<Hit<AlbumInfoIndex>> hits = response.hits().hits();
            if (CollectionUtils.isEmpty(hits)){
                return null;
            }
            // 转化成AlbumInfoIndexVo集合
            return hits.stream().map(hit -> {
                // source对应的就是AlbumInfoIndex
                AlbumInfoIndex albumInfoIndex = hit.source();
                // 初始化vo集合，并赋值
                AlbumInfoIndexVo albumInfoIndexVo = new AlbumInfoIndexVo();
                BeanUtils.copyProperties(albumInfoIndex, albumInfoIndexVo);
                return albumInfoIndexVo;
            }).collect(Collectors.toList());
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateLatelyAlbumStat() {
        log.info("同步专辑统计信息开始：{}",System.currentTimeMillis());
        try {
            //为了防止每次统计间隙有数据遗漏
            //记录统计开始时间到redis，上一次统计的截止时间是这一次的开始时间
            String startTime =(String) this.redisTemplate.opsForValue().get(RedisConstant.ALBUM_STAT_ENDTIME);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            if (StringUtils.isBlank(startTime)) {
                // 如果没有则取一个小时前的时间
                startTime = LocalDateTime.now().minusHours(1).format(formatter);
            }
            //统计截止时间
            String endTime = LocalDateTime.now().format(formatter);

            //记录到redis作为下次统计的起始时间
            this.redisTemplate.opsForValue().set(RedisConstant.ALBUM_STAT_ENDTIME,endTime);

            // 获取最近统计信息发生变化的专辑列表
            Result<List<Long>> albumIdResult = this.albumInfoFeignClient.findLatelyUpdateAlbum(startTime, endTime);
            Assert.notNull(albumIdResult,"同步数据时，获取专辑列表失败");
            List<Long> albumIds = albumIdResult.getData();
            if (CollectionUtils.isEmpty(albumIds)) return;

            /// 如果专辑id列表不为空则需要同步到es
            // 为了提高性能，每1000个专辑id拆分成一个部分，然后使用多线程并发执行
            List<List<Long>> partition = Lists.partition(albumIds, 1000);
            int count = partition.size();

            //初始化countDownLatch
            CountDownLatch countDownLatch = new CountDownLatch(count);
            partition.forEach(partAlbumIds->{
                // 通过线程池控制线程数
                executorService.execute(()->{
                    // 查询这部分专辑的统计信息
                    Result<List<AlbumStatVo>> albumStatResult = this.albumInfoFeignClient.findAlbumStatVoList(partAlbumIds);
                    Assert.notNull(albumStatResult,"同步数据到es时，获取专辑统计信息失败！");
                    List<AlbumStatVo> albumStatVos = albumStatResult.getData();
                    //如果为空
                    if (CollectionUtils.isEmpty(albumStatVos)) return;

                    //遍历同步到es
                    albumStatVos.forEach(albumStatVo -> {
                        AlbumInfoIndex albumInfoIndex = new AlbumInfoIndex();
                        BeanUtils.copyProperties(albumStatVo,albumInfoIndex);
                        albumInfoIndex.setId(albumStatVo.getAlbumId());
                        //设置热度
                        double hotScore = 0.2 * albumStatVo.getPlayStatNum()
                                        + 0.3 * albumStatVo.getSubscribeStatNum()
                                        + 0.4 * albumStatVo.getBuyStatNum()
                                        + 0.1 * albumStatVo.getCommentStatNum();
                        albumInfoIndex.setHotScore(hotScore);
                        this.elasticsearchTemplate.update(albumInfoIndex);
                    });
                });
                countDownLatch.countDown();
            });
            countDownLatch.await();
        }catch (InterruptedException e){
            throw new RuntimeException(e);
        }finally {
            log.info("同步专辑统计信息结束：{}", System.currentTimeMillis());
        }


    }

    //解析提示词补全结果集
    private List<String> parseSuggest(SearchResponse response, String suggestName){
        List<String> list = new ArrayList<>();
        // 根据suggestName获取suggest
        Map<String, List<Suggestion<SuggestIndex>>> suggest = response.suggest();
        List<Suggestion<SuggestIndex>> suggestions = suggest.get(suggestName);
        // 如果不为空，解析提示词结果集
        if (CollectionUtils.isEmpty(suggestions)){
            return null;
        }
        // 遍历suggestions
        suggestions.forEach(suggestion -> {
            // 获取options集合
            suggestion.completion().options().forEach(option -> {
                list.add(option.source().getTitle());
            });
        });
        return list;
    }

    //解析结果集
    private AlbumSearchResponseVo parseResult(SearchResponse response){
        AlbumSearchResponseVo responseVo = new AlbumSearchResponseVo();

        // 获取响应结果集中的hits
        HitsMetadata<AlbumInfoIndex> hits = response.hits();
        // 设置总记录数
        responseVo.setTotal(hits.total().value());
        // 设置当前页数据
        List<Hit<AlbumInfoIndex>> hitList = hits.hits();
        if (!CollectionUtils.isEmpty(hitList)){
            // 把hit集合转化成albumInfoIndexVo集合
            List<AlbumInfoIndexVo> albumInfoIndexVos = hitList.stream().map(hit->{
                // source对应的就是AlbumInfoIndex
                AlbumInfoIndex albumInfoIndex = hit.source();
                // 初始化vo集合，并赋值
                AlbumInfoIndexVo albumInfoIndexVo = new AlbumInfoIndexVo();
                BeanUtils.copyProperties(albumInfoIndex,albumInfoIndexVo);
                // 设置高亮
                Map<String,List<String>> hightlight = null;
                if (!CollectionUtils.isEmpty(hightlight=hit.highlight())&& !CollectionUtils.isEmpty(hightlight.get("albumTitle"))) albumInfoIndexVo.setAlbumTitle(hightlight.get("albumTitle").get(0));
                return albumInfoIndexVo;
            }).collect(Collectors.toList());
            responseVo.setList(albumInfoIndexVos);
        }
        return responseVo;
    }

    //搜索条件
    private static SearchRequest buildDsl(AlbumIndexQuery albumIndexQuery) {
        // 构建request请求体
        SearchRequest.Builder requestBuilder = new SearchRequest.Builder();
        requestBuilder.index("albuminfo"); //搜索的索引库

        // 1. 构建布尔查询
        BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();
        // 1.1. 获取搜索关键字，构建多字段匹配查询
        String keyword = albumIndexQuery.getKeyword();
        if (StringUtils.isNotBlank(keyword)){
            boolQueryBuilder.must(m -> m.multiMatch(mm -> mm
                    .fields("albumTitle", "albumIntro", "announcerName").query(keyword)));
        }

        // 1.2. 构建三级分类过滤
        Long category1Id = albumIndexQuery.getCategory1Id();
        Long category2Id = albumIndexQuery.getCategory2Id();
        Long category3Id = albumIndexQuery.getCategory3Id();
        if (category1Id != null) boolQueryBuilder.filter(f->f.term(t-> t.field("category1Id").value(category1Id)));
        if (category2Id != null) boolQueryBuilder.filter(f->f.term(t-> t.field("category2Id").value(category2Id)));
        if (category3Id != null) boolQueryBuilder.filter(f->f.term(t-> t.field("category3Id").value(category3Id)));
        // 1.3. 构建属性的嵌套过滤
        List<String> attributeList = albumIndexQuery.getAttributeList();
        if (!CollectionUtils.isEmpty(attributeList)){
            attributeList.forEach(attribute -> {
                // 以:进行分割，分割后应该是2个元素，属性id:属性值id
                String[] attrs = StringUtils.split(attribute, ":");
                if (attrs == null || attrs.length != 2){
                    return;
                }
                // 构建嵌套过滤
                boolQueryBuilder.filter(f -> f.nested(n -> n
                        .path("attributeValueIndexList")
                        .query(q -> q.bool(b -> b
                                .must(m -> m.term(t -> t.field("attributeValueIndexList.attributeId").value(attrs[0])))
                                .must(m -> m.term(t -> t.field("attributeValueIndexList.valueId").value(attrs[1])))))));
            });
        }
        // 把构建好的boolQuery放入requestBuilder对象
        requestBuilder.query(boolQueryBuilder.build()._toQuery());

        // 2. 构建分页参数
        Integer size = albumIndexQuery.getPageSize();
        requestBuilder.from((albumIndexQuery.getPageNo() - 1) * size).size(size);

        // 3. 构建排序条件  综合排序[1:desc] 播放量[2:desc] 发布时间[3:desc]；asc:升序 desc:降序
        String order = albumIndexQuery.getOrder();
        if (StringUtils.isNotBlank(order)){
            String[] orders = StringUtils.split(order, ",");
            if (orders.length == 2 && orders != null){
                String orderFiled = "hotScore";
                switch (orders[0]){
                    case "1": orderFiled = "hotScore"; break;
                    case "2": orderFiled = "playStatNum"; break;
                    case "3": orderFiled = "createTime"; break;
                }
                // lambda表达式中需要final类型的字符串变量
                final String field = orderFiled;
                requestBuilder.sort(s->s.field(f->f.field(field).order(SortOrder.Desc)));
            }
        }

        // 4. 高亮
        requestBuilder.highlight(h -> h.fields("albumTitle", f -> f.preTags("<font color='red'>").postTags("</font>")));

        // 5. 结果集过滤
        requestBuilder.source(s -> s.filter(f -> f.excludes("hotScore", "attributeValueIndexList")));

        SearchRequest request = requestBuilder.build();
        System.out.println(request.toString());
        return request;
    }

}
