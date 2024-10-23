package com.atguigu.tingshu.album.service.impl;

import com.atguigu.tingshu.album.mapper.AlbumAttributeValueMapper;
import com.atguigu.tingshu.album.mapper.AlbumInfoMapper;
import com.atguigu.tingshu.album.mapper.AlbumStatMapper;
import com.atguigu.tingshu.album.service.AlbumInfoService;
import com.atguigu.tingshu.common.constant.SystemConstant;
import com.atguigu.tingshu.common.util.AuthContextHolder;
import com.atguigu.tingshu.model.album.AlbumAttributeValue;
import com.atguigu.tingshu.model.album.AlbumInfo;
import com.atguigu.tingshu.model.album.AlbumStat;
import com.atguigu.tingshu.query.album.AlbumInfoQuery;
import com.atguigu.tingshu.vo.album.AlbumAttributeValueVo;
import com.atguigu.tingshu.vo.album.AlbumInfoVo;
import com.atguigu.tingshu.vo.album.AlbumListVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@SuppressWarnings({"unchecked", "rawtypes"})
public class AlbumInfoServiceImpl extends ServiceImpl<AlbumInfoMapper, AlbumInfo> implements AlbumInfoService {

	@Autowired
	private AlbumInfoMapper albumInfoMapper;
    @Autowired
    private AlbumAttributeValueMapper albumAttributeValueMapper;
    @Autowired
    private AlbumStatMapper albumStatMapper;



    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAlbumInfo(AlbumInfoVo albumInfoVo) {
        AlbumInfo albumInfo = new AlbumInfo();
        BeanUtils.copyProperties(albumInfoVo,albumInfo);

        albumInfo.setUserId(AuthContextHolder.getUserId() == null?1:AuthContextHolder.getUserId()); //设置用户id
        albumInfo.setStatus(SystemConstant.ALBUM_STATUS_PASS); //设置专辑状态
        if(!SystemConstant.ALBUM_PAY_TYPE_FREE.equals(albumInfo.getPayType())) albumInfo.setTracksForFree(5);

        this.save(albumInfo);
        Long albumInfoId = albumInfo.getId();

        List<AlbumAttributeValueVo> albumAttributeValueVoList = albumInfoVo.getAlbumAttributeValueVoList();
        if (!CollectionUtils.isEmpty(albumAttributeValueVoList)){
            albumAttributeValueVoList.forEach(albumAttributeValueVo -> {
                AlbumAttributeValue albumAttributeValue = new AlbumAttributeValue();
                BeanUtils.copyProperties(albumAttributeValueVo,albumAttributeValue);
                albumAttributeValue.setAlbumId(albumInfoId);
                albumAttributeValueMapper.insert(albumAttributeValue);
            });
        }

        this.initAlbumStat(albumInfo.getId(), SystemConstant.ALBUM_STAT_PLAY);
        this.initAlbumStat(albumInfo.getId(),SystemConstant.ALBUM_STAT_SUBSCRIBE);
        this.initAlbumStat(albumInfo.getId(),SystemConstant.ALBUM_STAT_BROWSE);
        this.initAlbumStat(albumInfo.getId(),SystemConstant.ALBUM_STAT_COMMENT);



    }

    @Override
    public IPage<AlbumListVo> findUserAlbumPage(Integer page, Integer limit, AlbumInfoQuery albumInfoQuery) {
        Page<AlbumListVo> page1 = new Page<>(page, limit);
        albumInfoQuery.setUserId(1L);
        return albumInfoMapper.findUserAlumnPage(page1,albumInfoQuery);
    }

    @Override
    public void removeAlbumInfo(Long albumId) {
        this.removeById(albumId);
        albumAttributeValueMapper.delete(new LambdaQueryWrapper<AlbumAttributeValue>().eq(AlbumAttributeValue::getAlbumId,albumId));
        albumStatMapper.delete(new LambdaQueryWrapper<AlbumStat>().eq(AlbumStat::getAlbumId,albumId));
    }

    @Override
    public AlbumInfo getAlbumInfo(Long albumId) {
        AlbumInfo albumInfo = this.getById(albumId);
        if (albumInfo != null){
            List<AlbumAttributeValue> albumAttributeValueList = albumAttributeValueMapper.selectList(new LambdaQueryWrapper<AlbumAttributeValue>().eq(AlbumAttributeValue::getAlbumId, albumId));
            albumInfo.setAlbumAttributeValueVoList(albumAttributeValueList);
        }
        return albumInfo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAlbumInfo(Long albumId, AlbumInfoVo albumInfoVo) {
        //更新专辑信息表
        AlbumInfo albumInfo = this.getById(albumId);
        BeanUtils.copyProperties(albumInfoVo,albumInfo);
        this.updateById(albumInfo);
        //删除专辑标签
        albumAttributeValueMapper.delete(new LambdaQueryWrapper<AlbumAttributeValue>().eq(AlbumAttributeValue::getAlbumId,albumId));
        //新增专辑标签
        List<AlbumAttributeValueVo> albumAttributeValueVoList = albumInfoVo.getAlbumAttributeValueVoList();
        if(!CollectionUtils.isEmpty(albumAttributeValueVoList)){
            albumAttributeValueVoList.forEach(albumAttributeValueVo -> {
                AlbumAttributeValue albumAttributeValue = new AlbumAttributeValue();
                BeanUtils.copyProperties(albumAttributeValueVo,albumAttributeValue);
                albumAttributeValue.setAlbumId(albumId);
                albumAttributeValueMapper.insert(albumAttributeValue);
            });
        }
    }

    @Override
    public List<AlbumInfo> findUserAllAlbumList() {
        return this.list(new LambdaQueryWrapper<AlbumInfo>().eq(AlbumInfo::getUserId,AuthContextHolder.getUserId()==null?1:AuthContextHolder.getUserId()).eq(AlbumInfo::getIsDeleted,0).select(AlbumInfo::getId,AlbumInfo::getAlbumTitle).orderByDesc(AlbumInfo::getId));
    }


    private void initAlbumStat(Long albumId, String statType){
        AlbumStat albumStat = new AlbumStat();
        albumStat.setAlbumId(albumId);
        albumStat.setStatNum(0);
        albumStat.setStatType(statType);
        albumStatMapper.insert(albumStat);
    }
}
