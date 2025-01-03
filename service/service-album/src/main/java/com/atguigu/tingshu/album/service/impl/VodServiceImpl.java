package com.atguigu.tingshu.album.service.impl;

import com.atguigu.tingshu.album.config.VodConstantProperties;
import com.atguigu.tingshu.album.service.VodService;
import com.atguigu.tingshu.common.util.UploadFileUtil;
import com.atguigu.tingshu.vo.album.TrackMediaInfoVo;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.qcloud.vod.VodUploadClient;
import com.qcloud.vod.model.VodUploadRequest;
import com.qcloud.vod.model.VodUploadResponse;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.vod.v20180717.VodClient;
import com.tencentcloudapi.vod.v20180717.models.*;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


@Service
public class VodServiceImpl implements VodService {

    @Autowired
    private VodConstantProperties vodConstantProperties;

    @Override
    @SneakyThrows
    public Map<String, Object> uploadTrack(MultipartFile file) {
        //  声音上传临时目录：
        String tempPath = UploadFileUtil.uploadTempPath(vodConstantProperties.getTempPath(), file);
        //初始化一个上传客户端对象
        VodUploadClient client = new VodUploadClient(vodConstantProperties.getSecretId(), vodConstantProperties.getSecretKey());
        //构造上传请求对象
        VodUploadRequest request = new VodUploadRequest();
        request.setMediaFilePath(tempPath);
        //设置任务流
        request.setProcedure(vodConstantProperties.getProcedure());
        //调用上传
        VodUploadResponse response = client.upload(vodConstantProperties.getRegion(), request);
        //创建map 对象
        Map<String, Object> map = new HashMap<>();
        map.put("mediaFileId",response.getFileId());
        map.put("mediaUrl",response.getMediaUrl());
        return map;
    }

    @Override
    public TrackMediaInfoVo getTrackMediaInfo(String mediaFileId) {
        try{
            // 实例化一个认证对象，入参需要传入腾讯云账户 SecretId 和 SecretKey，此处还需注意密钥对的保密
            Credential cred = new Credential(vodConstantProperties.getSecretId(), vodConstantProperties.getSecretKey());
            // 实例化要请求产品的client对象,clientProfile是可选的
            VodClient client = new VodClient(cred, vodConstantProperties.getRegion());
            // 实例化一个请求对象,每个接口都会对应一个request对象
            DescribeMediaInfosRequest req = new DescribeMediaInfosRequest();
            req.setFileIds(new String[]{mediaFileId});
            // 返回的resp是一个DescribeMediaInfosResponse的实例，与请求对象对应
            DescribeMediaInfosResponse resp = client.DescribeMediaInfos(req);
            // 输出json格式的字符串回包
            System.out.println(DescribeMediaInfosResponse.toJsonString(resp));

            // 获取媒体信息，如果不为空
            MediaInfo[] mediaInfoSet = resp.getMediaInfoSet();
            if (mediaInfoSet != null && mediaInfoSet.length > 0) {
                MediaInfo mediaInfo = mediaInfoSet[0];
                // 组装对象并返回
                TrackMediaInfoVo trackMediaInfoVo = new TrackMediaInfoVo();
                trackMediaInfoVo.setDuration(mediaInfo.getMetaData().getDuration());
                trackMediaInfoVo.setSize(mediaInfo.getMetaData().getSize());
                trackMediaInfoVo.setType(mediaInfo.getBasicInfo().getType());
                trackMediaInfoVo.setMediaUrl(mediaInfo.getBasicInfo().getMediaUrl());
                return trackMediaInfoVo;
            }
        } catch (TencentCloudSDKException e) {
            System.out.println(e.toString());
        }
        return null;
    }

    @Override
    public void removeTrackMedia(String mediaFileId) {
        try{
            // 实例化一个认证对象，入参需要传入腾讯云账户 SecretId 和 SecretKey，此处还需注意密钥对的保密
            Credential cred = new Credential(vodConstantProperties.getSecretId(), vodConstantProperties.getSecretKey());
            // 实例化要请求产品的client对象
            VodClient client = new VodClient(cred, "ap-guangzhou");
            // 实例化一个请求对象,每个接口都会对应一个request对象
            DeleteMediaRequest req = new DeleteMediaRequest();
            req.setFileId(mediaFileId);
            // 返回的resp是一个DeleteMediaResponse的实例，与请求对象对应
            DeleteMediaResponse resp = client.DeleteMedia(req);
            // 输出json格式的字符串回包
            System.out.println(DeleteMediaResponse.toJsonString(resp));
        } catch (TencentCloudSDKException e) {
            System.out.println(e.toString());
        }
    }

    @Override
    public String getPlayToken(String mediaFileId) {
        Integer AppId = vodConstantProperties.getAppId();
        String FileId = mediaFileId;
        String AudioVideoType = "Original";
        Integer RawAdaptiveDefinition = 10;
        Integer ImageSpriteDefinition = 10;
        //播放器签名的派发时间为
        Integer CurrentTime = Math.toIntExact(new Date().getTime() / 1000);
        //播放器签名的过期时间，根据录制最长时间设置，如：两小时
        Integer PsignExpire = CurrentTime + 60 * 60 * 2;
        //防盗链的过期时间
        String UrlTimeExpire = String.valueOf(PsignExpire);
        //播放密钥
        String PlayKey = vodConstantProperties.getPlayKey();
        HashMap<String, Object> urlAccessInfo = new HashMap<String,Object>();
        urlAccessInfo.put("t",UrlTimeExpire);
        HashMap<String, Object> contentInfo = new HashMap<String, Object>();
        contentInfo.put("audioVideoType",AudioVideoType);
        contentInfo.put("rawAdaptiveDefinition",RawAdaptiveDefinition);
        contentInfo.put("imageSpriteDefinition",ImageSpriteDefinition);

        Algorithm algorithm = Algorithm.HMAC256(PlayKey);
        String token = JWT.create().withClaim("appId",AppId)
                                    .withClaim("fileId", FileId)
                                    .withClaim("contentInfo", contentInfo)
                                    .withClaim("currentTimeStamp", CurrentTime)
                                    .withClaim("expireTimeStamp", PsignExpire)
                                    .withClaim("urlAccessInfo", urlAccessInfo)
                                    .sign(algorithm);
        System.out.println("token:"+token);
        return token;

    }
}
