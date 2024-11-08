package com.atguigu.tingshu.album.api;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.tingshu.album.config.MinioConstantProperties;
import com.atguigu.tingshu.album.service.BaseCategoryService;
import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.model.album.BaseCategory3;
import com.atguigu.tingshu.model.album.BaseCategoryView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@Tag(name = "分类管理")
@RestController
@RequestMapping(value="/api/album/category")
@SuppressWarnings({"unchecked", "rawtypes"})
public class BaseCategoryApiController {
	
	@Autowired
	private BaseCategoryService baseCategoryService;

	@Operation(summary = "查询分类列表")
	@GetMapping("/getBaseCategoryList")
	public Result getBaseCategoryList(){
		List<JSONObject> categorys  = this.baseCategoryService.getBaseCategoryList();
		return Result.ok(categorys);
	}

	@Operation(summary = "查询专辑标签")
	@GetMapping("/findAttribute/{categoryId}")
	public Result findAttribute(@PathVariable("categoryId") Long categoryId){
		List<JSONObject> attribute = this.baseCategoryService.findAttributeByCategoryId(categoryId);
		return Result.ok(attribute);
	}

	@GetMapping("findBaseCategoryViewByCategory3Id/{category3Id}")
	public Result<BaseCategoryView> findBaseCategoryViewByCategory3Id(@PathVariable("category3Id") Long category3Id){
		BaseCategoryView categoryView = this.baseCategoryService.findBaseCategoryViewCategory3Id(category3Id);
		return Result.ok(categoryView);

	}

	@Operation(summary = "根据一级分类Id查询置顶频道页的三级分类")
	@GetMapping("/findTopBaseCategory3/{category1Id}")
	public Result<List<BaseCategory3>> findTopBaseCategory3(@PathVariable("category1Id") Long category1Id){
		List<BaseCategory3> category3List= baseCategoryService.findTopBaseCategory3(category1Id);
		return Result.ok(category3List);
	}

	@Operation(summary = "根据一级分类id获取全部分类信息")
	@GetMapping("/getBaseCategoryList/{category1Id}")
	public Result<JSONObject> getBaseCategoryList(@PathVariable("category1Id") Long category1Id){
		JSONObject jsonObject = baseCategoryService.getAllCategoryList(category1Id);
		return Result.ok(jsonObject);
	}






}

