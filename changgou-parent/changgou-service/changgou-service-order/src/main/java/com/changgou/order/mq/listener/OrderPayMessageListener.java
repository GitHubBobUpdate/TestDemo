package com.changgou.order.mq.listener;

import com.alibaba.fastjson.JSON;
import com.changgou.order.service.OrderService;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RabbitListener(queues = "${mq.pay.queue.order}")
public class OrderPayMessageListener {
    @Autowired
    private OrderService orderService;

    @RabbitHandler
    public void getMessage(String message){
        //支付结果
        Map<String,String> resultMap = JSON.parseObject(message, Map.class);

        //通信结果
        //return_code=SUCCESS
        String return_code = resultMap.get("return_code");

        //业务结果 result_code=SUCCESS/FAIL，修改订单状态
        if(return_code.equalsIgnoreCase("success") ){
            //获取订单号
            String outtradeno = resultMap.get("out_trade_no");
            //业务结果
            String result_code = resultMap.get("result_code");

            //支付成功
            if(result_code.equalsIgnoreCase("success")){

                if(outtradeno!=null){
                    //修改订单状态  out_trade_no
                    orderService.updateStatus(outtradeno,resultMap.get("time_end"),resultMap.get("transaction_id"));
                }
            }else{
                //支付失败
                // 关闭支付,需要调用微信服务器的API接口，通知微信关闭

                // 取消订单
                // 回滚库存
                orderService.deleteOrder(outtradeno);
            }
        }
        //微信支付流水号
        //订单号
    }

}
