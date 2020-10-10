package com.changgou.order.service.impl;

import com.changgou.goods.feign.SkuFeign;
import com.changgou.goods.feign.SpuFeign;
import com.changgou.goods.pojo.Sku;
import com.changgou.goods.pojo.Spu;
import com.changgou.order.pojo.OrderItem;
import com.changgou.order.service.CartService;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {
    @Autowired
    private SkuFeign skuFeign;
    @Autowired
    private SpuFeign spuFeign;
    @Autowired
    private RedisTemplate redisTemplate;
    //添加购物车
    @Override
    public void add(Integer num, Long skuID, String username) {
        //当加入的购物车数量为负数，不需要添加redis，将对应的hash值删除
        if(num < 0){
            redisTemplate.boundHashOps("Cart_" + username).delete(skuID);
            //如果此时购物车数据为空就直接移除购物车数据
            Long size = redisTemplate.boundHashOps("Cart_" + username).size();
            if(null == size || size <= 0){
                redisTemplate.delete("Cart_" + username);
            }
            return;
        }
        //查询数据
        //1、获取sku信息
        Result<Sku> skuResult = skuFeign.findById(skuID);
        if(null != skuResult && null != skuResult.getData()){
            //2、获取spu信息
            Result<Spu> spuResult = spuFeign.findById(skuResult.getData().getSpuId());

            //将信息封装成OrderItem对象
            OrderItem orderItem = sku2OrderItem(skuResult.getData(),spuResult.getData(),num);

            //将数据存放到redis
            redisTemplate.boundHashOps("Cart_" + username).put(skuID,orderItem);
        }
    }

    @Override
    public List<OrderItem> list(String username,String ids) {
        List<OrderItem> orderItems = new ArrayList<OrderItem>();
        //当时需要只查询选中的商品显示在结算页面，就判断ids是否为空
        if(!StringUtils.isEmpty(ids)){
            String[] split = ids.split(",");
            for (String id : split) {
                //根据skuID从redis中获取数据,价格可能出现
                orderItems.add((OrderItem) redisTemplate.boundHashOps("Cart_" + username).get(id));
            }
            return orderItems;
        }
        orderItems = redisTemplate.boundHashOps("Cart_" + username).values();
        return orderItems;
    }

    /**
     * 将sku信息封装成OrderItem对象
     * @param sku
     * @param spu
     * @param num
     * @return
     */
    private OrderItem sku2OrderItem(Sku sku,Spu spu,Integer num){
        OrderItem orderItem = new OrderItem();
        orderItem.setSpuId(sku.getSpuId());
        orderItem.setSkuId(sku.getId());
        orderItem.setName(sku.getName());
        orderItem.setPrice(sku.getPrice());
        orderItem.setNum(num);
        orderItem.setMoney(num*orderItem.getPrice());       //单价*数量
        orderItem.setPayMoney(num*orderItem.getPrice());    //实付金额
        orderItem.setImage(sku.getImage());
        orderItem.setWeight(sku.getWeight()*num);           //重量=单个重量*数量

        //分类ID设置
        orderItem.setCategoryId1(spu.getCategory1Id());
        orderItem.setCategoryId2(spu.getCategory2Id());
        orderItem.setCategoryId3(spu.getCategory3Id());
        return orderItem;
    }
}
