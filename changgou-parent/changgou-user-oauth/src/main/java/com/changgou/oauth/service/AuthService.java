package com.changgou.oauth.service;

import com.changgou.oauth.util.AuthToken;

/*****
 * @Author: www.itheima
 * @Date: 2019/7/7 16:23
 * @Description: com.changgou.oauth.service
 ****/
public interface AuthService {

    /***
     * 授权认证方法:密码认证模式
     */
    AuthToken login(String username, String password, String clientId, String clientSecret) throws Exception;
}
