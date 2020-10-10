package com.changgou.oauth.util;

import com.alibaba.fastjson.JSON;
import org.springframework.cloud.bootstrap.encrypt.KeyProperties;
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

public class AdminToken {
    
    public static String adminToken(){
        /*//读取配置文件获取私钥、
        这里获取私钥的代码可以，尝试采用AuthorizationServerConfig中的keyProperties这个Bean对象，但是将Bean注入到静态方法中不能直接注入
        需要通过Autowired主注解静态方法或者使用PostConstruct注解*/

        //创建Token
        //读取配置文件获取私钥、
         ClassPathResource resource  = new ClassPathResource("changgouPro.jks");

         //通过密钥工厂解析文件获取数据
         KeyStoreKeyFactory keyStoreKeyFactory = new KeyStoreKeyFactory(resource,"changgouPro".toCharArray());

         KeyPair keyPair = keyStoreKeyFactory.getKeyPair("changgouPro", "changgouPro".toCharArray());

        //获取私钥
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();

        //封装playload
        Map<String,Object> map = new HashMap<String,Object>();
        map.put("authotities",new String[]{"admin"});

        //在oauth2.0中使用JWTHelper创建token
        Jwt jwt = JwtHelper.encode(JSON.toJSONString(map), new RsaSigner(privateKey));
        return jwt.getEncoded();
    }
}
