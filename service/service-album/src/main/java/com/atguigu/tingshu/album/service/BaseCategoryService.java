package com.atguigu.tingshu.album.service;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.tingshu.model.album.BaseCategory1;
import com.atguigu.tingshu.model.album.BaseCategory3;
import com.atguigu.tingshu.model.album.BaseCategoryView;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface BaseCategoryService extends IService<BaseCategory1> {


    List<JSONObject> getBaseCategoryList();

    List<JSONObject> findAttributeByCategoryId(Long categoryId);

    BaseCategoryView findBaseCategoryViewCategory3Id(Long category3Id);

    List<BaseCategory3> findTopBaseCategory3(Long category1Id);

    JSONObject getAllCategoryList(Long category1Id);
}
