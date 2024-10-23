package com.atguigu.tingshu.album.api;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.tingshu.album.config.MinioConstantProperties;
import com.atguigu.tingshu.album.service.BaseCategoryService;
import com.atguigu.tingshu.common.result.Result;
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
}

