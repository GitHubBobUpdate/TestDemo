package com.changgou.pay.controller;

import com.alibaba.fastjson.JSON;
import com.changgou.pay.service.WeixinPayService;
import com.github.wxpay.sdk.WXPayUtil;
import entity.Result;
import entity.StatusCode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(value = "/weixin/pay")
@CrossOrigin
public class WeixinPayController {
    @Autowired
    private WeixinPayService weixinPayService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /***
     * 支付回调，微信支付完成之后将支付结果返回给我们
     * 回调地址必须要可以通过互联网访问，所以采用花生壳实现内网穿透
     * @param request
     * @return
     */
    @RequestMapping(value = "/notify/url")
    public String notifyUrl(HttpServletRequest request){
            InputStream inStream;
        try {
            //返回的是通过网络输入流传递的，获取网络输入流
            //读取支付回调数据
            inStream = request.getInputStream();
            ByteArrayOutputStream outSteam = new ByteArrayOutputStream();

            byte[] buffer = new byte[1024];//缓冲区

            //将输入流写入到Array的输出流中
            int len = 0;
            while ((len = inStream.read(buffer)) != -1) {
                outSteam.write(buffer, 0, len);
            }
            outSteam.close();
            inStream.close();

            //将输出流转换成xml字符串并且读取数据
            String resultStr = new String(outSteam.toByteArray(), "UTF-8");

            Map<String, String> resultMap = WXPayUtil.xmlToMap(resultStr);

            //获取自定义参数
            String attach = resultMap.get("attach");
            Map<String,String> attachMap = JSON.parseObject(attach, Map.class);

            //获取到结果信息之后，将结果发送到rabbiteMQ
            rabbitTemplate.convertAndSend(attachMap.get("exchange"),attachMap.get("routingkey"), JSON.toJSONString(resultMap));

            //响应数据设置
            Map<String,String> respMap = new HashMap<String,String>();
            respMap.put("return_code","SUCCESS");
            respMap.put("return_msg","OK");
            return WXPayUtil.mapToXml(respMap);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 创建二维码
     * @param paraMap
     * @return
     */
    @RequestMapping(value = "/create/native")
    public Result createNative(@RequestParam Map<String,String> paraMap){

        Map<String,String> resultMap = weixinPayService.createNative(paraMap);

        return new Result(true, StatusCode.OK,"创建二维码预付订单成功！",resultMap);
    }

    /***
     * 查询支付状态
     * @param outtradeno
     * @return
     */
    @GetMapping(value = "/status/query")
    public Result queryStatus(String outtradeno){
        Map<String,String> resultMap = weixinPayService.queryPayStatus(outtradeno);
        return new Result(true,StatusCode.OK,"查询状态成功！",resultMap);
    }


    /** * 关闭微信订单 * @param orderId * @return */
    @PutMapping("/close/{orderId}")
    public Result closeOrder(@PathVariable String orderId){
        Map map = weixinPayService.closeOrder(orderId);
        return new Result( true,StatusCode.OK,"订单关闭成功",map ); }


}

