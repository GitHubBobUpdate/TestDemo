package com.changgou.pay.service;

import java.util.Map;

public interface WeixinPayService {
    /*****
     * 创建二维码
     *  moa : 客户端自定义订单编号
     *  total_fee    : 交易金额,单位：分
     * @return
     */
    public Map createNative(Map<String,String> para);

    /***
     * 查询订单状态
     * @param out_trade_no : 客户端自定义订单编号
     * @return
     */
    public Map queryPayStatus(String out_trade_no);

    /** * 关闭订单 * @param orderId * @return */
    public Map<String,String> closeOrder(String orderId);

    /***
     * 关闭支付
     * @param orderId
     * @return
     */
    Map<String,String> closePay(Long orderId) throws Exception;
}
