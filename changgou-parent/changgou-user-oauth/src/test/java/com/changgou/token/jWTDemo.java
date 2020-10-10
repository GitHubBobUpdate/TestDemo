package com.changgou.token;

import com.alibaba.fastjson.JSON;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.RsaSigner;
import org.springframework.security.jwt.crypto.sign.RsaVerifier;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.util.HashMap;
import java.util.Map;

public class jWTDemo {

    @Test
    public void testCreateJWT(){
        //读取配置文件获取私钥、
        ClassPathResource resource  = new ClassPathResource("changgouPro.jks");

        //通过密钥工厂解析文件获取数据
        KeyStoreKeyFactory keyStoreKeyFactory = new KeyStoreKeyFactory(resource,"changgouPro".toCharArray());

        KeyPair keyPair = keyStoreKeyFactory.getKeyPair("changgouPro", "changgouPro".toCharArray());
        //获取私钥
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();

        Map<String,Object> map = new HashMap<String,Object>();
        map.put("nikename","tomcat");
        map.put("address","sz");
        map.put("user","admin");

        //在oauth2.0中使用JWTHelper创建token
        Jwt jwt = JwtHelper.encode(JSON.toJSONString(map), new RsaSigner(privateKey));
        String encoded = jwt.getEncoded();
        System.out.println(encoded);
    }

    @Test
    public void paseToekn()
    {
        String token = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJhZGRyZXNzIjoic3oiLCJuaWtlbmFtZSI6InRvbWNhdCIsInVzZXIiOiJhZG1pbiJ9.b4uf_B9CDcMQh8KlVtNfZII4HzoCt7JNos-BGDsiAxDYqLBUTrpfSKOKmZl2YGBWRCUS2QmXMByg2NyH_FpOFiknaPb4uSJXidpCLO52nUQvx2Nnmh653Sa8FEY5GeTdwTcwuXYR2EcT_yoBL5koO2O2P5ATSFEFd0w3-iaXfE5Fy8U0UUZulJBUTbmF8Svqjn7L_d7GCL7Mg4wEQ6vf04yC0kUVxNPSXYpC9fEkpIib2XWtrBY-TcQRMTRtUeBz36L4wFx2srxDaGgXWGETFjbvTWecFU9uGbVOfjHd9BgwLxktxqzLdTW4z9ZOYdPF0lMeMTojpHESokngrXMuKA";
        Jwt jwt = JwtHelper.decodeAndVerify(token, new RsaVerifier("-----BEGIN PUBLIC KEY-----MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAhJt+7/0lExGRRCIe8bs3Q7iUf/v1jFG3aCEe2Mphie10v4e+ZgBqVsXbO2E0JrViFf4yxXBsbIbOsmRWSHw+xYNnn0siqZ9GwxcQ+s7/+xkrROyUnAy+ut6b08Lao4rKt6N6DLZL9A/uOaB220wxHf7j0eJkz8A65VmHxf0uvVfV2ajtc2ETbBMNuJGrAD2xEWdBTTjkodpYyHgq0QONrA8VoKXLsh86LkX5Sdwz8w2b1T2URQEzQWGPHgJRgsiHfHVJD/47GaJ684UJBfQNX5V4upP0Y0l58n7sbdlmD5l9woun3BxrtMoZWHVlp4Z9h8ms/pF4+97M8uWxex36XwIDAQAB-----END PUBLIC KEY-----"));
        String encoded = jwt.getClaims();

        System.out.println(encoded);
    }
}
