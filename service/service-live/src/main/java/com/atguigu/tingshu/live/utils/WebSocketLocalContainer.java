package com.atguigu.tingshu.live.utils;

import com.alibaba.fastjson.JSON;
import com.atguigu.tingshu.model.live.FromUser;
import com.atguigu.tingshu.model.live.SocketMsg;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import jakarta.websocket.Session;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class WebSocketLocalContainer {

    // 聊天群组：直播间id和用户之间的对应关系，可以根据直播间id获取直播间用户集合
    private static Map<Long, Set<Long>> userIdsMap = new ConcurrentHashMap<>();

    // 用户id与用户基本信息之间的对应关系，通过用户id可以获取用户的基本信息
    // 建立连接时，可以根据token获取用户id获取用户的基本信息，放入该容器
    // 将来用户发送消息时，可以根据用户id获取用户的基本信息
    private static Map<Long, FromUser> fromUserMap = new ConcurrentHashMap<>();

    // 每个用户对应的session会话对象
    private static Map<Long, Session> sessionMap = new ConcurrentHashMap<>();

    public static void addUserToLiveRoom(Long liveRoomId, Long userId){
        Set<Long> userIds = userIdsMap.get(liveRoomId);
        if (userIds == null){
            userIds = new HashSet<>();
            userIdsMap.put(liveRoomId, userIds);
        }
        userIds.add(userId);
    }

    public static void removeUserFromLiveRoom(Long liveRoomId, Long userId){
        Set<Long> userIds = userIdsMap.get(liveRoomId);
        if (userIds == null){
            return;
        }
        userIds.remove(userId);
    }

    public static Set<Long> getUserIds(Long liveRoomId){
        Set<Long> userIds = userIdsMap.get(liveRoomId);
        if (userIds == null){
            userIds = new HashSet<>();
        }
        return userIds;
    }

    public static Integer getUserCount(Long liveRoomId){
        Set<Long> userIds = userIdsMap.get(liveRoomId);
        if (userIds == null){
            return 0;
        }
        return userIds.size();
    }

    public static void addFromUser(Long userId, FromUser fromUser){
        fromUserMap.put(userId, fromUser);
    }

    public static void removeFromUser(Long userId){
        fromUserMap.remove(userId);
    }

    public static FromUser getFromUser(Long userId){
        return fromUserMap.get(userId);
    }

    public static void addSession(Long userId, Session session){
        sessionMap.put(userId, session);
    }

    public static void removeSession(Long userId){
        sessionMap.remove(userId);
    }

    public static Session getSession(Long userId){
        return sessionMap.get(userId);
    }

    /**
     * 封装群发消息的方法
     * @param socketMsg
     */
    public static void sendMsg(SocketMsg socketMsg){
        // 心跳消息直接跳过
        if (socketMsg.getMsgType().equals(SocketMsg.MsgTypeEnum.HEART_BEAT.getCode())){
            return;
        }
        // 获取同一直播间的用户id集合
        Set<Long> userIds = WebSocketLocalContainer.getUserIds(socketMsg.getLiveRoomId());
        if (CollectionUtils.isEmpty(userIds)){
            return;
        }
        // 群发消息
        userIds.forEach(userId -> {
            // 获取对应用户的session
            Session session = WebSocketLocalContainer.getSession(userId);
            if (session != null) {
                // 异步发送消息
                session.getAsyncRemote().sendText(JSON.toJSONString(socketMsg));
            }
        });
    }
}
