package com.atguigu.tingshu.live.config;

import com.atguigu.tingshu.live.utils.WebSocketLocalContainer;
import com.atguigu.tingshu.model.live.SocketMsg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@Configuration
public class RedisListenerConfig {

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * Redis 消息监听器绑定监听指定通道
     * 可以添加多个监听器，监听多个通道，只需要将消息监听器与订阅的通道/主题绑定即可。
     * @return
     */
    @Bean
    public RedisMessageListenerContainer listenerContainer(RedisConnectionFactory connectionFactory, MessageListener messageListener){
        RedisMessageListenerContainer redisMessageListenerContainer = new RedisMessageListenerContainer();
        //设置连接工厂，RedisConnectionFactory 可以直接从容器中取，也可以从RedisTemplate中取
        redisMessageListenerContainer.setConnectionFactory(connectionFactory);
        //可以同时订阅多个消息通道
        //订阅名称叫 tingshu:live:message 的通道, 类似 Redis 中的 subscribe 命令
        //new PatternTopic("tingshu:*") 类似 Redis 的 pSubscribe 命令
        redisMessageListenerContainer.addMessageListener(messageListener,new ChannelTopic("tingshu:live:message"));
        return redisMessageListenerContainer;
    }



    /**
     * 初始化redis消息监听器
     * @return
     */
    @Bean
    public MessageListener messageListener(){
        return (Message message, byte[] pattern) -> {
            // 消息来自于那个通道
            String channel = new String(pattern);
            System.out.println("消息通道: " + channel);
            // System.out.println("消息通道: " + new String(message.getChannel()));

            // 消息内容
            SocketMsg msg = (SocketMsg) redisTemplate.getValueSerializer().deserialize(message.getBody());
            System.out.println("消息内容: " + msg);

            // 发送消息
            WebSocketLocalContainer.sendMsg(msg);
        };
    }


}
