package com.changgou.pay.service.impl;

import com.alibaba.fastjson.JSON;
import com.changgou.pay.service.WeixinPayService;
import com.github.wxpay.sdk.WXPayUtil;
import entity.HttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

@Service
public class WeixinPayServiceImpl implements WeixinPayService {
    @Value("${weixin.appid}")
    private String appid;

    @Value("${weixin.partner}")
    private String partner;

    @Value("${weixin.partnerkey}")
    private String partnerkey;

    @Value("${weixin.notifyurl}")
    private String notifyurl;

    /***
     * 关闭微信支付
     * @param orderId
     * @return
     * @throws Exception
     */
    @Override
    public Map<String, String> closePay(Long orderId) throws Exception {
        //参数设置
        Map<String,String> paramMap = new HashMap<String,String>();
        paramMap.put("appid",appid); //应用ID
        paramMap.put("mch_id",partner);    //商户编号
        paramMap.put("nonce_str",WXPayUtil.generateNonceStr());//随机字符
        paramMap.put("out_trade_no",String.valueOf(orderId));   //商家的唯一编号

        //将Map数据转成XML字符
        String xmlParam = WXPayUtil.generateSignedXml(paramMap,partnerkey);

        //确定url
        String url = "https://api.mch.weixin.qq.com/pay/closeorder";

        //发送请求
        HttpClient httpClient = new HttpClient(url);
        //https
        httpClient.setHttps(true);
        //提交参数
        httpClient.setXmlParam(xmlParam);

        //提交
        httpClient.post();

        //获取返回数据
        String content = httpClient.getContent();

        //将返回数据解析成Map
        return  WXPayUtil.xmlToMap(content);
    }
    /**
     * 创建二维码
     * @param para
     * @return
     */
    @Override
    public Map createNative(Map<String,String> para) {
        try {
            Map<String,String> paraMap = new HashMap<String,String>();
            //參數
            paraMap.put("appid", appid);                              //应用ID
            paraMap.put("mch_id", partner);                           //商户ID号
            paraMap.put("nonce_str", WXPayUtil.generateNonceStr());   //随机数
            paraMap.put("body", "畅购商城订单");                              //订单描述
            paraMap.put("out_trade_no",para.get("outtradeno"));                 //商户订单号
            paraMap.put("total_fee", para.get("totalfee"));                      //交易金额
            paraMap.put("spbill_create_ip", "127.0.0.1");           //终端IP
            paraMap.put("notify_url", notifyurl);                    //回调地址
            paraMap.put("trade_type", "NATIVE");                     //交易类型

            //获取自定义参数，也就是当前的订单是秒杀订单还是普通订单的队列名
            /*
            *普通订单的：
            * exchange: exchange.order
            * routingkey:queue.order
            *
            * 秒杀订单
            * exchange:exchange.seckillorder
            * routingkey:queue.seckillorder
            *
            * */
            Map<String,String> queueInfo = new HashMap<String,String>();
            queueInfo.put("exchange",para.get("exchange"));
            queueInfo.put("routingkey",para.get("routingkey"));
            //如果是秒杀订单还需要username
            if(!StringUtils.isEmpty(para.get("username"))){
                queueInfo.put("username",para.get("username"));
            }

            String queueString = JSON.toJSONString(queueInfo);

            //存放自定义数据，用于微信返回
            paraMap.put("attach",queueString);

            //将map转成带签名的xml
            String signedXml = WXPayUtil.generateSignedXml(paraMap, partnerkey);

            //URL地址
            String URL = "https://api.mch.weixin.qq.com/pay/unifiedorder";

            //提交方式
            HttpClient httpClient = new HttpClient(URL);
            httpClient.setHttps(true);

            //提交參數
            httpClient.setXmlParam(signedXml);

            //执行提交
            httpClient.post();

            //获取返回数据
            String result = httpClient.getContent();
            
            //将返回数据转成map
            Map<String, String> reslutMap = WXPayUtil.xmlToMap(result);

            return reslutMap;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 查询订单结果
     * @param out_trade_no : 客户端自定义订单编号
     * @return
     */
    @Override
    public Map queryPayStatus(String out_trade_no) {
        try {
            //1.封装参数
            Map<String,String> param = new HashMap<String,String>();
            param.put("appid",appid);                            //应用ID
            param.put("mch_id",partner);                         //商户号
            param.put("out_trade_no",out_trade_no);              //商户订单编号
            param.put("nonce_str",WXPayUtil.generateNonceStr()); //随机字符

            //2、将参数转成xml字符，并携带签名
            String paramXml = WXPayUtil.generateSignedXml(param,partnerkey);

            //3、发送请求
            HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/orderquery");
            httpClient.setHttps(true);
            httpClient.setXmlParam(paramXml);
            httpClient.post();

            //4、获取返回值，并将返回值转成Map
            String content = httpClient.getContent();
            return WXPayUtil.xmlToMap(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Map<String, String> closeOrder(String orderId) {
        return null;
    }
}
