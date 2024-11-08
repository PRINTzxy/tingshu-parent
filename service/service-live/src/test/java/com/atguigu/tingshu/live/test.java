package com.atguigu.tingshu.live;

import org.springframework.boot.test.context.SpringBootTest;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@SpringBootTest
public class test {

    // 域名管理中点击推流域名-->推流配置-->鉴权配置-->主KEY
    private String pushKey;
    // 云直播控制台配置的推流域名
    private String pushDomain;
    // 云直播控制台配置的拉流域名
    private String pullDomain;
    // 直播SDK --> Licene管理 --> 应用管理 --> 自己创建应用中的应用名称
    private String appName;

    public static void main(String[] args) {
        String key = "76e1b8ba9d7970e54207e746a2e71889";
        String streamName = "java";
        LocalDateTime localDateTime = LocalDateTime.now();
        long nowTime = localDateTime.toEpochSecond(ZoneOffset.of("+8"));
        long txTime = nowTime + 60 * 60 * 12; // 默认12小时

        String safeUrl = getSafeUrl(key,streamName,txTime);
        System.out.println(safeUrl);

        String pushUrl = "webrtc://206217.push.tlivecloud.com/live/" + streamName + "?" + safeUrl;
        System.out.println(pushUrl);

        String playUrl = "webrtc://lxfplay.atguigu.cn/live/" + streamName + "?" + safeUrl;
        System.out.println(playUrl);

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
