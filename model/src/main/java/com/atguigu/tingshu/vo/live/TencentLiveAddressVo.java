package com.atguigu.tingshu.vo.live;

import lombok.Data;

@Data
public class TencentLiveAddressVo {

    private String pushWebRtcUrl;
    private String pullFlvUrl;
    private String pullM3u8Url;
    private String pushRtmpUrl;
    private String pullWebRtcUrl;
}
