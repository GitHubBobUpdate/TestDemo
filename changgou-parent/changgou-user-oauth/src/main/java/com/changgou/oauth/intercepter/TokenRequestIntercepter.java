package com.changgou.oauth.intercepter;

import com.alibaba.fastjson.JSON;
import com.changgou.oauth.util.AdminToken;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.bootstrap.encrypt.KeyProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.RsaSigner;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;

import javax.annotation.Resource;
import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class TokenRequestIntercepter implements RequestInterceptor {
    /**
     * 在登录时，feign调用之前拦截，创建Token,使之拥有调用feign的权限，验证登录用户数据
     * @param requestTemplate
     */
    @Override
    public void apply(RequestTemplate requestTemplate) {
        //创建特殊Token
        String adminToken = "bearer " + AdminToken.adminToken();
        //将令牌添加到head中
        requestTemplate.header("Authorization",adminToken);
    }
}
