package com.atguigu.tingshu.live.utils;

import com.atguigu.tingshu.vo.live.TencentLiveAddressVo;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

//@ConfigurationProperties(prefix = "live")
@Configuration
@Data
public class LiveUtil {

    private String appName;
    private String pushKey;
    private String pushDomain;
    private String pullDomain;

    public TencentLiveAddressVo getLiveAddress(String streamName, Long txTime) {

        String token = getSafeUrl(pushKey, streamName, txTime);

        String pushUrl = "webrtc://" + pushDomain + "/" + appName + "/" + streamName + "?" + token;
        String pullUrl = "webrtc://" + pullDomain + "/" + appName + "/" + streamName + "?" + token;

        TencentLiveAddressVo addressVo = new TencentLiveAddressVo();
        addressVo.setPushWebRtcUrl(pushUrl);
        addressVo.setPullWebRtcUrl(pullUrl);
        return addressVo;
    }

    private static final char[] DIGITS_LOWER =
            {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    /*
     * KEY+ streamName + txTime
     */
    private static String getSafeUrl(String key, String streamName, long txTime) {
        String input = new StringBuilder().
                append(key).
                append(streamName).
                append(Long.toHexString(txTime).toUpperCase()).toString();

        String txSecret = null;
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            txSecret  = byteArrayToHexString(
                    messageDigest.digest(input.getBytes("UTF-8")));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return txSecret == null ? "" :
                new StringBuilder().
                        append("txSecret=").
                        append(txSecret).
                        append("&").
                        append("txTime=").
                        append(Long.toHexString(txTime).toUpperCase()).
                        toString();
    }

    private static String byteArrayToHexString(byte[] data) {
        char[] out = new char[data.length << 1];

        for (int i = 0, j = 0; i < data.length; i++) {
            out[j++] = DIGITS_LOWER[(0xF0 & data[i]) >>> 4];
            out[j++] = DIGITS_LOWER[0x0F & data[i]];
        }
        return new String(out);
    }
}
