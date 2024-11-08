package com.atguigu.tingshu.user.service.impl;

import com.atguigu.tingshu.model.user.UserInfo;
import com.atguigu.tingshu.model.user.UserPaidAlbum;
import com.atguigu.tingshu.model.user.UserPaidTrack;
import com.atguigu.tingshu.user.login.LoginAccount;
import com.atguigu.tingshu.user.login.LoginClient;
import com.atguigu.tingshu.user.mapper.UserInfoMapper;
import com.atguigu.tingshu.user.mapper.UserPaidAlbumMapper;
import com.atguigu.tingshu.user.mapper.UserPaidTrackMapper;
import com.atguigu.tingshu.user.service.UserInfoService;
import com.atguigu.tingshu.vo.user.UserInfoVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Map;


@Slf4j
@Service
@SuppressWarnings({"unchecked", "rawtypes"})
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements UserInfoService {

	@Autowired
	private LoginClient loginClient;
    @Autowired
    private UserInfoMapper userInfoMapper;
    @Autowired
    private UserPaidAlbumMapper userPaidAlbumMapper;
    @Autowired
    private UserPaidTrackMapper userPaidTrackMapper;


    @GlobalTransactional
    @Override
    public Map<String, Object> wxLogin(String code) throws Exception{
        LoginAccount account = new LoginAccount();
        account.setCode(code);
        return this.loginClient.login(1, account);
    }

    @Override
    public UserInfoVo getUserInfo(Long id) {
        UserInfo userInfo = this.getById(id);
        UserInfoVo userInfoVo = new UserInfoVo();
        BeanUtils.copyProperties(userInfo, userInfoVo);
        return userInfoVo;
    }

    @Override
    public Boolean getPaidAlbumStatByAlbumIdAndUserId(Long albumId, Long userId) {
        return userPaidAlbumMapper.selectOne(new LambdaQueryWrapper<UserPaidAlbum>().eq(UserPaidAlbum::getAlbumId,albumId).eq(UserPaidAlbum::getUserId,userId)) != null;
    }

    @Override
    public List<UserPaidTrack> getPaidTracksByAlbumIdAndUserId(Long albumId, Long userId) {
        return userPaidTrackMapper.selectList(new LambdaQueryWrapper<UserPaidTrack>().eq(UserPaidTrack::getUserId,albumId).eq(UserPaidTrack::getUserId,userId));
    }

    @Transactional
    @Override
    public void updateExpiredVipStatus() {
        UserInfo userInfo = new UserInfo();
        userInfo.setIsVip(0);
        userInfoMapper.update(userInfo,new LambdaQueryWrapper<UserInfo>()
                                    .eq(UserInfo::getIsVip,1)
                                    .le(UserInfo::getVipExpireTime,new Date()));
    }
}
