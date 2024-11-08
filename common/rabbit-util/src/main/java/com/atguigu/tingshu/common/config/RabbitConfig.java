package com.atguigu.tingshu.common.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;


@Slf4j
@Configuration
public class RabbitConfig {

    @Resource
    private RabbitTemplate rabbitTemplate;

    @PostConstruct
    public void init(){
        // 确认消息是否到达交换机：不管有没有到达交换机都会执行该回调
        this.rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (!ack){
                // 如果没有到达则重试、记录日志
                log.error("消息没有到达交换机：{}", cause);
            }
        });
        // 确认消息是否到达队列，没有到达队列才会执行
        this.rabbitTemplate.setReturnsCallback(returned -> {
            // 记录日志或者重试
            log.error("消息没有到达队列。交换机：{}，路由键：{}，消息内容：{}，回调状态：{}，回调文本：{}",
                    returned.getExchange(), returned.getRoutingKey(), new String(returned.getMessage().getBody()), returned.getReplyCode(), returned.getReplyText());
        });
    }
}
