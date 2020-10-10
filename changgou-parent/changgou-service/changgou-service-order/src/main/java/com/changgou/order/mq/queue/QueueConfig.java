package com.changgou.order.mq.queue;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QueueConfig {

    /** 延时队列*/
    public static final String QUEUE_MESSAGE_DELAY = "queue.message.delay";

    /** 交换机 */
    public static final String DLX_EXCHANGE = "dlx.exchange";

    /** 过期之后移入的死信队列 */
    public static final String QUEUE_MESSAGE_DEATH = "queue.message.death";

    /**
     * 自己创建的死信队列
     * @return
     */
    @Bean
    public Queue messageQueue() {
        return new Queue(QUEUE_MESSAGE_DEATH, true);
    }

    /**
     * 存未过期消息的延时队列
     * @return
     */
    @Bean
    public Queue delayMessageQueue() {
        return QueueBuilder.durable(QUEUE_MESSAGE_DELAY)        //延时队列名
                .withArgument("x-dead-letter-routing-key", QUEUE_MESSAGE_DEATH)   // 消息超时之后移入的死信队列名
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)           // 死信队列绑定的交换机
                .build();
    }

    /***
     * 创建交换机
     * @return
     */
    @Bean
    public DirectExchange directExchange(){
        return new DirectExchange(DLX_EXCHANGE);
    }


    /***
     * 延时队列的交换机绑定
     * @param messageQueue
     * @param directExchange
     * @return
     */
    @Bean
    public Binding basicBinding(Queue messageQueue, DirectExchange directExchange) {
        return BindingBuilder.bind(messageQueue)
                .to(directExchange)
                .with(QUEUE_MESSAGE_DELAY);
    }
}
