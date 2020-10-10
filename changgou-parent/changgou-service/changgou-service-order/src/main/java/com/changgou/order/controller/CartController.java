package com.changgou.order.controller;

import com.changgou.order.pojo.OrderItem;
import com.changgou.order.service.CartService;
import entity.Result;
import entity.StatusCode;
import entity.TokenDecode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.web.bind.annotation.*;
import sun.plugin.liveconnect.SecurityContextHelper;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cart")
@CrossOrigin
public class CartController {
    @Autowired
    private CartService cartService;

    /**
     * 将商品添加到购物车
     * @return
     */
    @GetMapping(value = "/add")
    public Result add(Integer num, Long skuID){
        //获取购物车列表时，从请求头中获取用户数据
        Map<String, String> userInfo = TokenDecode.getUserInfo();
        String username = userInfo.get("username");

        //将商品加入购物车
        cartService.add(num,skuID,username);
        return new Result(true, StatusCode.OK,"加入购物车成功！");
    }

    /***
     * 查询用户购物车列表
     * @return
     */
    @GetMapping(value = "/list")
    public Result list(@RequestParam(value = "ids",required = false) String ids){
        //获取购物车列表时，从请求头中获取用户数据
        Map<String, String> userInfo = TokenDecode.getUserInfo();
        String username = userInfo.get("username");

        //通过工具类解析令牌
        //用户名
        List<OrderItem> orderItems = cartService.list(username,ids);
        return new Result(true,StatusCode.OK,"购物车列表查询成功！",orderItems);
    }
}
