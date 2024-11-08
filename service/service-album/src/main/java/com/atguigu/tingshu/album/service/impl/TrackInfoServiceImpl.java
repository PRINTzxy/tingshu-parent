package com.atguigu.tingshu.album.service.impl;

import com.atguigu.tingshu.album.config.VodConstantProperties;
import com.atguigu.tingshu.album.mapper.AlbumInfoMapper;
import com.atguigu.tingshu.album.mapper.TrackInfoMapper;
import com.atguigu.tingshu.album.mapper.TrackStatMapper;
import com.atguigu.tingshu.album.service.TrackInfoService;
import com.atguigu.tingshu.album.service.VodService;
import com.atguigu.tingshu.common.constant.SystemConstant;
import com.atguigu.tingshu.common.execption.GuiguException;
import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.common.result.ResultCodeEnum;
import com.atguigu.tingshu.common.util.AuthContextHolder;
import com.atguigu.tingshu.model.album.AlbumInfo;
import com.atguigu.tingshu.model.album.TrackInfo;


import com.atguigu.tingshu.model.album.TrackStat;
import com.atguigu.tingshu.model.user.UserPaidTrack;
import com.atguigu.tingshu.query.album.TrackInfoQuery;
import com.atguigu.tingshu.user.client.UserInfoFeignClient;
import com.atguigu.tingshu.user.client.UserListenProcessFeignClient;
import com.atguigu.tingshu.vo.album.*;
import com.atguigu.tingshu.vo.user.UserInfoVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Assert;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    @Qualifier("com.atguigu.tingshu.user.client.UserInfoFeignClient")
    @Autowired
    private UserInfoFeignClient userInfoFeignClient;
    @Qualifier("com.atguigu.tingshu.user.client.UserListenProcessFeignClient")
    @Autowired
    private UserListenProcessFeignClient userListenProcessFeignClient;
    @Autowired
    private VodConstantProperties vodConstantProperties;


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
        trackInfo.setMediaUrl(trackMediaInfo.getMediaUrl());
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
        this.saveTrackStat(trackInfo.getId(),SystemConstant.TRACK_STAT_PRAISE);
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

    @Override
    public IPage<AlbumTrackListVo> findTrackByAlbumIdAndPage(Long albumId, Page<AlbumTrackListVo> albumTrackListVoPage) {
        //1. 根据专辑id分页查询声音
        IPage<AlbumTrackListVo> trackListVoIPage = this.trackInfoMapper.findTrackByAlbumIdAndPage(albumId, albumTrackListVoPage);
        List<AlbumTrackListVo> trackListVos = trackListVoIPage.getRecords();
        //2. 根据id查询专辑，获取付费类型：0101-免费、0102-vip免费、0103-付费
        AlbumInfo albumInfo = albumInfoMapper.selectById(albumId);
        Assert.notNull(albumInfo,"专辑为空！！");

        //3. 判断付费类型
        String payType = albumInfo.getPayType();
        //3.1. 如果免费，直接返回声音分页列表（不显示付费标签）
        if (SystemConstant.ALBUM_PAY_TYPE_FREE.equals(payType)) return trackListVoIPage;

        //3.2. 获取需要付费的声音集合
        //免费试听集数
        Integer tracksForFree = albumInfo.getTracksForFree();
        List<AlbumTrackListVo> needPayTracks = trackListVos.stream().filter(track -> track.getOrderNum() > tracksForFree).collect(Collectors.toList());
        //如果付费集数为null则直接返回
        if(CollectionUtils.isEmpty(needPayTracks)) return trackListVoIPage;
        //是否需要显示付费标签
        Boolean isNeedPay = false;

        //3.3. 如果是vip收费，需要显示付费标签
        if(SystemConstant.ALBUM_PAY_TYPE_REQUIRE.equals(payType)) isNeedPay = true;

        //3.4. 如果vip免费
        if(SystemConstant.ALBUM_PAY_TYPE_VIPFREE.equals(payType)){
            //获取登录状态，判断用户是否登录
            Long userId = AuthContextHolder.getUserId();
            if(userId == null){
                //如果未登录则需要显示付费标识
                isNeedPay = true;
            }else {
                //查询用户信息
                Result<UserInfoVo> userInfoResult = userInfoFeignClient.getUserInfoById(userId);
                Assert.notNull(userInfoResult,"用户信息为空！！");
                UserInfoVo userInfoVo = userInfoResult.getData();
                //如果用户不会vip，需要显示付费标识
                if (userInfoVo.getIsVip() == 0) isNeedPay = true;
                //如果用户是vip，但是已经过期了，需要显示付费标识
                else if (userInfoVo.getIsVip() ==1 && userInfoVo.getVipExpireTime().before(new Date())) isNeedPay = true;
            }
        }

        //4. 如果需要付费，判断用户是否购买过专辑或者声音
        if(isNeedPay){
            //根据专辑id查询该用户是否订购过，如果订购过不显示付费标签
            Result<Boolean> result = userInfoFeignClient.getPaidAlbumStatByAlbumIdAndUserId(albumId);
            Assert.notNull(result,"无法获取专辑订购信息");
            Boolean flag = result.getData();

            //没有订购过
            if(flag == null || !flag){
                Result<List<UserPaidTrack>> paidTrackResult = userInfoFeignClient.getPaidTracksByAlbumIdAndUserId(albumId);
                Assert.notNull(paidTrackResult,"没有获取到订购的声音列表");

                //根据专辑id查询该用户订购过的声音列表
                List<UserPaidTrack> userPaidTracks = paidTrackResult.getData();
                if (!CollectionUtils.isEmpty(userPaidTracks)){
                    //获取订购过的声音ids
                    List<Long> paidTrackIds = userPaidTracks.stream().map(UserPaidTrack::getTrackId).collect(Collectors.toList());
                    List<Long> finalPaidTrackIds = paidTrackIds;
                    needPayTracks.forEach(track -> {
                        //如果订购过得声音列表中不包含当前声音，则显示付费标识
                        if (!finalPaidTrackIds.contains(track.getTrackId())) track.setIsShowPaidMark(true);
                    });
                }else {
                    needPayTracks.forEach(track -> track.setIsShowPaidMark(true));
                }
            }
        }
        return trackListVoIPage;
    }

    @Override
    public TrackStatVo getTrackStatVo(Long trackId) {
        return this.trackInfoMapper.getTrackStatVo(trackId);
    }

    @Override
    public Map<String, Object> getPlayToken(Long trackId) {
        TrackInfo trackInfo = this.getById(trackId);
        Assert.notNull(trackInfo,"声音对象不能为空");
        AlbumInfo albumInfo = this.albumInfoMapper.selectById(trackInfo.getAlbumId());
        Assert.notNull(albumInfo,"专辑对象不能为空");

        Long userId = AuthContextHolder.getUserId();

        // 如果声音所属专辑不属于免费专辑 或者 不在试听集数内
        if (!SystemConstant.ALBUM_PAY_TYPE_FREE.equals(albumInfo.getPayType()) && trackInfo.getOrderNum() > albumInfo.getTracksForFree()){
            if(userId == null){
                throw new GuiguException(ResultCodeEnum.NO_BUY_NOT_SEE);
            }else {
                //查询用户是否购买过该专辑
                Result<Boolean> albumPaidResult = userInfoFeignClient.getPaidAlbumStatByAlbumIdAndUserId(albumInfo.getId());
                Assert.notNull(albumPaidResult,"没有获取到专辑购买信息");
                Boolean isPaid = albumPaidResult.getData();
                if (isPaid == null || !isPaid){
                    //查询用户购买过该专辑下的声音列表
                    Result<List<UserPaidTrack>> paidTracksResult = userInfoFeignClient.getPaidTracksByAlbumIdAndUserId(albumInfo.getId());
                    List<UserPaidTrack> userPaidTracks = paidTracksResult.getData();

                    //没有订过购该声音
                    if (!CollectionUtils.isEmpty(userPaidTracks) || !userPaidTracks.stream().anyMatch(userPaidTrack -> userPaidTrack.getTrackId() == trackId)){
                        //查询用户信息
                        Result<UserInfoVo> userInfoVoResult = userInfoFeignClient.getUserInfoById(userId);
                        UserInfoVo userInfoVo = userInfoVoResult.getData();
                        //如果是普通用户 或者是 未过期vip 抛出异常，或者vip收费
                        if (userInfoVo.getIsVip() == 0 || userInfoVo.getVipExpireTime().before(new Date()) || SystemConstant.ALBUM_PAY_TYPE_REQUIRE.equals(albumInfo.getPayType())){
                            throw new GuiguException(ResultCodeEnum.NO_BUY_NOT_SEE);
                        }
                    }
                }
            }
        }
        //获取播放进度
        Result<BigDecimal> result = userListenProcessFeignClient.getTrackBreakSecond(trackId);
        Assert.notNull(result,"获取到跳出时间结果不能为空");
        BigDecimal breakSecond = result.getData();
        String playToken = vodService.getPlayToken(trackInfo.getMediaFileId());
        Map<String,Object> map = new HashMap<>();
        map.put("playToken", playToken);
        map.put("mediaFileId", trackInfo.getMediaFileId());
        map.put("breakSecond", breakSecond);
        map.put("appId",vodConstantProperties.getAppId());

        //获取下一个播放声音
        LambdaQueryWrapper<TrackInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TrackInfo::getAlbumId,trackInfo.getAlbumId());
        queryWrapper.gt(TrackInfo::getOrderNum,trackInfo.getOrderNum());
        queryWrapper.orderByAsc(TrackInfo::getOrderNum);
        queryWrapper.select(TrackInfo::getId);
        queryWrapper.last("limit 1");
        TrackInfo nextTrackInfo = this.getOne(queryWrapper);
        if(null != nextTrackInfo) map.put("nextTrackId", nextTrackInfo.getId());
        else map.put("nextTrackId",0L);
        return map;

    }

    private void saveTrackStat(Long trackId, String trackType) {
        TrackStat trackStat = new TrackStat();
        trackStat.setTrackId(trackId);
        trackStat.setStatType(trackType);
        trackStat.setStatNum(0);
        this.trackStatMapper.insert(trackStat);
    }

}
