package com.changgou.seckill.listener;

import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RabbitListener(queues = "seckillQueue")
public class SeckillDelayMessageListener {

    /**
     * 延时队列监听
     * @param message
     */
    @RabbitHandler
    public void getDelayMessage(String message){
        System.out.println("================监听到秒杀的延时队列中的数据，看是否将数据移入到死信队列，若有取消支付订单，删除订单==================");
    }
}
