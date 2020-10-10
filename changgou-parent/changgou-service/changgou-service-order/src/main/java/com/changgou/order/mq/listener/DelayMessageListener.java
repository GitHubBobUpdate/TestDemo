package com.changgou.order.mq.listener;

import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RabbitListener(queues = "queue.message.delay")
public class DelayMessageListener {

    /**
     * 延时队列监听
     * @param message
     */
    @RabbitHandler
    public void getDelayMessage(String message){
        System.out.println("================监听到延时队列中的数据，看是否将数据移入到死信队列，若有取消订单==================");
    }
}
