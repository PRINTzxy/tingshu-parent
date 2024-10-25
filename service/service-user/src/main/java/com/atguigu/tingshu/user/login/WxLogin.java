package com.atguigu.tingshu.user.login;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import com.atguigu.tingshu.account.client.UserAccountFeignClient;
import com.atguigu.tingshu.common.constant.RedisConstant;
import com.atguigu.tingshu.common.execption.GuiguException;
import com.atguigu.tingshu.common.result.ResultCodeEnum;
import com.atguigu.tingshu.model.user.UserInfo;
import com.atguigu.tingshu.user.mapper.UserInfoMapper;
import com.atguigu.tingshu.vo.user.UserInfoVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import io.seata.spring.annotation.GlobalTransactional;
import jakarta.annotation.Resource;
import me.chanjar.weixin.common.error.WxErrorException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@LoginBean(LoginType.WX_LOGIN)
public class WxLogin implements ILogin{
    @Autowired
    private UserInfoMapper userInfoMapper;
    @Autowired
    private WxMaService wxMaService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Resource
    private UserAccountFeignClient userAccountFeignClient;

    @Override
    public Map<String, Object> login(LoginAccount account) throws WxErrorException {
        //1. 根据code appid和appsecret调用微信接口 获取openid
        WxMaJscode2SessionResult sessionInfo = this.wxMaService.getUserService().getSessionInfo(account.getCode());
        String openid = sessionInfo.getOpenid();
        //如果获取失败，重新回调登录页面
        if(StringUtils.isBlank(openid)) throw new GuiguException(ResultCodeEnum.LOGIN_AUTH);

        //2. 根据openId查询用户
        UserInfo userInfo = this.userInfoMapper.selectOne(new LambdaQueryWrapper<UserInfo>().eq(UserInfo::getWxOpenId, openid));
        if (userInfo == null) {
            //3. 隐式注册
            userInfo  = new UserInfo();
            userInfo.setWxOpenId(openid);
            userInfo.setNickname("用户"+this.redisTemplate.opsForValue().increment(RedisConstant.USER_NICKNAME_KEY));
            userInfo.setAvatarUrl("https://lovecards.yny84666.website/storage/image/20241018/36b0046bc06142d2a0dc5c8c24fef6fe.jpg");
            userInfo.setStatus("1");
            this.userInfoMapper.insert(userInfo);
            this.userAccountFeignClient.init(userInfo.getId());



        }
        //4. 生成UUID作为token，保存到redis中
        String token = UUID.randomUUID().toString();
        UserInfoVo userInfoVo = new UserInfoVo();
        BeanUtils.copyProperties(userInfo,userInfoVo);
        this.redisTemplate.opsForValue().set(RedisConstant.USER_LOGIN_KEY_PREFIX + token,userInfoVo,RedisConstant.USER_LOGIN_KEY_TIMEOUT, TimeUnit.SECONDS);

        //5. 返回token
        Map<String, Object> map = new HashMap<>();
        map.put("token",token);
        return map;
    }

    /*@Override
    public Map<String, Object> login(LoginAccount account) throws WxErrorException {
        // 1.根据code appid和appsecret调用微信接口 获取openId
        WxMaJscode2SessionResult sessionInfo = this.wxMaService.getUserService().getSessionInfo(account.getCode());
        String openid = sessionInfo.getOpenid();
        // 如果获取失败，重新回调登录页面
        if (StringUtils.isBlank(openid)){
            throw new GuiguException(ResultCodeEnum.LOGIN_AUTH);
        }

        // 2.根据openId查询用户
        UserInfo userInfo = this.userInfoMapper.selectOne(new LambdaQueryWrapper<UserInfo>().eq(UserInfo::getWxOpenId, openid));
        if (userInfo == null){
            // 3.隐式注册
            userInfo = new UserInfo();
            userInfo.setWxOpenId(openid);
            userInfo.setNickname("用户" + +this.redisTemplate.opsForValue().increment(RedisConstant.USER_NICKNAME_KEY));
            userInfo.setAvatarUrl("https://lovecards.yny84666.website/storage/image/20241018/36b0046bc06142d2a0dc5c8c24fef6fe.jpg");
            userInfo.setStatus("1");
            this.userInfoMapper.insert(userInfo);
            // 初始化账户信息
            this.userAccountFeignClient.init(userInfo.getId());

//			int i = 1/0;
        }

        // 4.生成uuid作为token，保存到redis中
        String token = UUID.randomUUID().toString();
        UserInfoVo userInfoVo = new UserInfoVo();
        BeanUtils.copyProperties(userInfo, userInfoVo);
        this.redisTemplate.opsForValue().set(RedisConstant.USER_LOGIN_KEY_PREFIX + token, userInfoVo, RedisConstant.USER_LOGIN_KEY_TIMEOUT, TimeUnit.SECONDS);

        // 5.返回token
        Map<String, Object> map = new HashMap<>();
        map.put("token", token);
        return map;
    }*/
}
