package com.atguigu.tingshu.vo.album;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
@Schema(description = "用户专辑列表信息")
public class AlbumListVo {

	@Schema(description = "专辑id")
	private Long albumId;

	@Schema(description = "标题")
	private String albumTitle;

	@Schema(description = "专辑封面原图，尺寸不固定，最大尺寸为960*960（像素）")
	private String coverUrl;

	@Schema(description = "专辑包含声音总数")
	private Integer includeTrackCount;

	@Schema(description = "专辑是否完结：0-否；1-完结；")
	private String isFinished;

	@Schema(description = "状态")
	private String status;

	@Schema(description = "播放量")
	private Integer playStatNum;

	@Schema(description = "订阅量")
	private Integer subscribeStatNum;

	@Schema(description = "购买量")
	private Integer buyStatNum;

	@Schema(description = "评论数")
//	private Integer albumCommentStatNum;
	private Integer commentStatNum;

	///////////////////新增////////////////////////

	@Schema(description = "简介")
	private String albumIntro;

	@Schema(description = "主播id")
	private Long userId;

	@Schema(description = "三级分类id")
	private Long category3Id;

	@Schema(description = "付费类型")
	private String payType;

	@Schema(description = "创建时间")
	private Date createTime;

}