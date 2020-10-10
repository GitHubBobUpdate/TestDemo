package com.changgou.seckill.mq;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class MQConfig {
    /**
     * 超时订单取消订单，回滚库存
     *
     * */
    //延时队列
    //死信队列
    //交换机
    /**
     * 自己创建的死信队列
     * @return
     */
    @Bean
    public Queue seckillQueue() {
        return new Queue("seckillQueue", true);
    }

    /**
     * 存未过期消息的延时队列
     * @return
     */
    @Bean
    public Queue delaySeckillQueue() {
        return QueueBuilder.durable("delaySeckillQueue")        //延时队列名
                .withArgument("x-dead-letter-routing-key", "seckillQueue")   // 消息超时之后移入的死信队列名
                .withArgument("x-dead-letter-exchange", "seckillExchange")           // 死信队列绑定的交换机
                .build();
    }

    /***
     * 创建交换机
     * @return
     */
    @Bean
    public DirectExchange seckillExchange(){
        return new DirectExchange("seckillExchange");
    }


    /***
     * 延时队列的交换机绑定
     * @param seckillQueue
     * @param seckillExchange
     * @return
     */
    @Bean
    public Binding basicBinding(Queue seckillQueue, DirectExchange seckillExchange) {
        return BindingBuilder.bind(seckillQueue)
                .to(seckillExchange)
                .with("delaySeckillQueue");
    }
}
