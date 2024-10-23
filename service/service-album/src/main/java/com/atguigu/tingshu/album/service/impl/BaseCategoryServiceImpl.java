package com.atguigu.tingshu.album.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.tingshu.album.mapper.*;
import com.atguigu.tingshu.album.service.BaseCategoryService;
import com.atguigu.tingshu.model.album.BaseCategory1;
import com.atguigu.tingshu.model.album.BaseCategoryView;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@SuppressWarnings({"unchecked", "rawtypes"})
public class BaseCategoryServiceImpl extends ServiceImpl<BaseCategory1Mapper, BaseCategory1> implements BaseCategoryService {

	@Autowired
	private BaseCategory1Mapper baseCategory1Mapper;

	@Autowired
	private BaseCategory2Mapper baseCategory2Mapper;

	@Autowired
	private BaseCategory3Mapper baseCategory3Mapper;

	@Autowired
	private BaseCategoryViewMapper baseCategoryViewMapper;
    @Autowired
    private BaseAttributeMapper baseAttributeMapper;

	@Override
    public List<JSONObject> getBaseCategoryList() {
		List<JSONObject> category1 = new ArrayList<>();

		List<BaseCategoryView> baseCategoryViews = baseCategoryViewMapper.selectList(null);
		if (CollectionUtils.isEmpty(baseCategoryViews)) return null;

		Map<Long,List<BaseCategoryView>> category1Map = baseCategoryViews.stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory1Id));
		category1Map.forEach((category1Id,category2List)->{
			JSONObject jsonObject1 = new JSONObject();
			category1.add(jsonObject1);
			jsonObject1.put("categoryId",category1Id);
			jsonObject1.put("categoryName",category2List.get(0).getCategory1Name());

			List<JSONObject> category2 = new ArrayList<>();
			jsonObject1.put("categoryChild",category2);

			Map<Long,List<BaseCategoryView>> category2Map = category2List.stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory2Id));
			category2Map.forEach((category2Id,category3List)->{
				JSONObject jsonObject2 = new JSONObject();
				category2.add(jsonObject2);
				jsonObject2.put("categoryId",category2Id);
				jsonObject2.put("categoryName",category3List.get(0).getCategory2Name());
				jsonObject2.put("categoryChild",category3List.stream().map(baseCategoryView -> {
					JSONObject jsonObject3 = new JSONObject();
					jsonObject3.put("categoryId",baseCategoryView.getCategory3Id());
					jsonObject3.put("categoryName",baseCategoryView.getCategory3Name());
					return jsonObject3;
				}).collect(Collectors.toList()));
			});
		});
		System.out.println("\n"+category1+"\n");
		return category1;
    }

	@Override
	public List<JSONObject> findAttributeByCategoryId(Long categoryId) {
		return baseAttributeMapper.findAttributeByCategoryId(categoryId);
	}
}
