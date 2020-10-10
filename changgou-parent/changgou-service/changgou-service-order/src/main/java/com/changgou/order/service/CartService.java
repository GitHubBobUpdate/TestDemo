package com.changgou.order.service;

import com.changgou.order.pojo.OrderItem;

import java.util.List;

public interface CartService {
    /***
     * 添加购物车
     * @param num
     * @param skuID
     * @param username
     */
    void add(Integer num, Long skuID, String username);

    /***
     * 查询用户的购物车数据
     * @param username
     * @return
     */
    List<OrderItem> list(String username,String ids);
}
