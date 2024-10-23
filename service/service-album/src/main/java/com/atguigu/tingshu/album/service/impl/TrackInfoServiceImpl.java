package com.atguigu.tingshu.album.service.impl;

import com.atguigu.tingshu.album.mapper.AlbumInfoMapper;
import com.atguigu.tingshu.album.mapper.TrackInfoMapper;
import com.atguigu.tingshu.album.mapper.TrackStatMapper;
import com.atguigu.tingshu.album.service.TrackInfoService;
import com.atguigu.tingshu.album.service.VodService;
import com.atguigu.tingshu.common.constant.SystemConstant;
import com.atguigu.tingshu.common.execption.GuiguException;
import com.atguigu.tingshu.common.result.ResultCodeEnum;
import com.atguigu.tingshu.common.util.AuthContextHolder;
import com.atguigu.tingshu.model.album.AlbumInfo;
import com.atguigu.tingshu.model.album.TrackInfo;


import com.atguigu.tingshu.model.album.TrackStat;
import com.atguigu.tingshu.query.album.TrackInfoQuery;
import com.atguigu.tingshu.vo.album.TrackInfoVo;
import com.atguigu.tingshu.vo.album.TrackListVo;
import com.atguigu.tingshu.vo.album.TrackMediaInfoVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@SuppressWarnings({"unchecked", "rawtypes"})
public class TrackInfoServiceImpl extends ServiceImpl<TrackInfoMapper, TrackInfo> implements TrackInfoService {

	@Autowired
	private TrackInfoMapper trackInfoMapper;
    @Autowired
    private AlbumInfoMapper albumInfoMapper;
    @Autowired
    private TrackStatMapper trackStatMapper;
    @Autowired
    private VodService vodService;


    @Override
    public void saveTrackInfo(TrackInfoVo trackInfoVo) {
        //  1.保存声音信息表
        TrackInfo trackInfo = new TrackInfo();
        BeanUtils.copyProperties(trackInfoVo, trackInfo);
        trackInfo.setUserId(AuthContextHolder.getUserId());

        // 查询上一条音频的order_num，设置该条音频的order_num
        TrackInfo preTrackInfo = this.getOne(new LambdaQueryWrapper<TrackInfo>()
                .eq(TrackInfo::getAlbumId, trackInfo.getAlbumId())
                .orderByDesc(TrackInfo::getOrderNum)
                .select(TrackInfo::getOrderNum)
                .last("limit 1"));
        trackInfo.setOrderNum(preTrackInfo == null ? 1 : preTrackInfo.getOrderNum() + 1);

        // 设置媒体相关参数
        // 先获取媒体相关信息
        TrackMediaInfoVo trackMediaInfo = this.vodService.getTrackMediaInfo(trackInfoVo.getMediaFileId());
        // 设置参数
        trackInfo.setMediaDuration(new BigDecimal(trackMediaInfo.getDuration()));
        trackInfo.setMediaSize(trackMediaInfo.getSize());
        trackInfo.setMediaType(trackMediaInfo.getType());
        // 设置声音状态
        trackInfo.setStatus(SystemConstant.TRACK_STATUS_PASS);
        this.save(trackInfo);
        Long trackInfoId = trackInfo.getId();

        // 更新专辑声音总数
        AlbumInfo albumInfo = this.albumInfoMapper.selectById(trackInfo.getAlbumId());
        if (albumInfo != null) {
            albumInfo.setIncludeTrackCount(albumInfo.getIncludeTrackCount() + 1);
            this.albumInfoMapper.updateById(albumInfo);
        }

        // 新增音频统计信息
        this.saveTrackStat(trackInfo.getId(), SystemConstant.TRACK_STAT_PLAY);
        this.saveTrackStat(trackInfo.getId(), SystemConstant.TRACK_STAT_COLLECT);
        this.saveTrackStat(trackInfo.getId(), SystemConstant.TRACK_STAT_COMMENT);
    }

    @Override
    public IPage<TrackListVo> findUserTrackPage(Page<Object> trackListVoPage, TrackInfoQuery trackInfoQuery) {
        return trackInfoMapper.findUserTrackPage(trackListVoPage,trackInfoQuery);
    }

    @Override
    public void updateTrackInfo(Long id, TrackInfoVo trackInfoVo) {
        //	获取到声音对象
        TrackInfo trackInfo = this.getById(id);
        String mediaFileId = trackInfo.getMediaFileId();
        //	进行属性拷贝
        BeanUtils.copyProperties(trackInfoVo, trackInfo);
        //	获取声音信息.
        if (!trackInfoVo.getMediaFileId().equals(mediaFileId)) {
            //	说明已经修改过了.
            TrackMediaInfoVo trackMediaInfoVo = vodService.getTrackMediaInfo(trackInfoVo.getMediaFileId());
            //	判断对象不为空.
            if (null==trackMediaInfoVo){
                //	抛出异常
                throw new GuiguException(ResultCodeEnum.VOD_FILE_ID_ERROR);
            }
            trackInfo.setMediaUrl(trackMediaInfoVo.getMediaUrl());
            trackInfo.setMediaType(trackMediaInfoVo.getType());
            trackInfo.setMediaDuration(BigDecimal.valueOf(trackMediaInfoVo.getDuration()));
            trackInfo.setMediaSize(trackMediaInfoVo.getSize());
        }
        //	修改数据
        this.updateById(trackInfo);
    }

    @Override
    public void removeTrackInfo(Long id) {
        TrackInfo trackInfo = this.getById(id);
        // 1. 删除对应的记录
        this.removeById(id);
        // 2. 更新专辑中的声音数量
        AlbumInfo albumInfo = this.albumInfoMapper.selectById(trackInfo.getAlbumId());
        albumInfo.setIncludeTrackCount(albumInfo.getIncludeTrackCount() - 1);
        this.albumInfoMapper.updateById(albumInfo);
        // 3. 删除统计表信息
        this.trackStatMapper.delete(new LambdaUpdateWrapper<TrackStat>().eq(TrackStat::getTrackId, id));
        // 4. 删除音频信息
        this.vodService.removeTrackMedia(trackInfo.getMediaFileId());
    }

    private void saveTrackStat(Long trackId, String trackType) {
        TrackStat trackStat = new TrackStat();
        trackStat.setTrackId(trackId);
        trackStat.setStatType(trackType);
        trackStat.setStatNum(0);
        this.trackStatMapper.insert(trackStat);
    }

}
