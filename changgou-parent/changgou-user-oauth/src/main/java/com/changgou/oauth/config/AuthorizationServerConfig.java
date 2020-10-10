package com.changgou.oauth.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.bootstrap.encrypt.KeyProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService;
import org.springframework.security.oauth2.provider.code.AuthorizationCodeServices;
import org.springframework.security.oauth2.provider.code.InMemoryAuthorizationCodeServices;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.security.oauth2.provider.token.DefaultAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.security.KeyPair;


@Configuration
@EnableAuthorizationServer
class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {
    //数据源，用于从数据库获取数据进行认证操作，测试可以从内存中获取
    @Autowired
    private DataSource dataSource;
    //jwt令牌转换器
    @Autowired
    private JwtAccessTokenConverter jwtAccessTokenConverter;
    //SpringSecurity 用户自定义授权认证类
    @Autowired
    UserDetailsService userDetailsService;
    //授权认证管理器
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    AuthorizationCodeServices authenticationCodeServices;

    //客户端管理服务
    @Autowired
    ClientDetailsService clientDetailsService;
    //令牌持久化存储接口
    @Autowired
    TokenStore tokenStore;
    @Autowired
    private CustomUserAuthenticationConverter customUserAuthenticationConverter;

    /***
     * 客户端信息配置
     * @param clients
     * @throws Exception
     */
    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        //配置客户端信息
//        clients.inMemory()
//                .withClient("changgou")          //客户端id
//                .secret("changgou")                      //秘钥
//                .redirectUris("http://localhost")       //重定向地址
//                .accessTokenValiditySeconds(3600)          //访问令牌有效期
//                .refreshTokenValiditySeconds(3600)         //刷新令牌有效期
//                .authorizedGrantTypes(
//                        "authorization_code",          //根据授权码生成令牌
//                        "client_credentials",          //客户端认证
//                        "refresh_token",                //刷新令牌
//                        "password")                     //密码方式认证
//                .scopes("app");                         //客户端范围，名称自定义，必填
        clients.jdbc(dataSource).clients(clientDetails());
    }

    /***
     * 授权服务器端点配置
     * @param endpoints
     * @throws Exception
     */
    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        endpoints.accessTokenConverter(jwtAccessTokenConverter)
                .authorizationCodeServices(authenticationCodeServices)//授权码服务
                .tokenServices(tokenServices())//灵台管理服务
                .authenticationManager(authenticationManager)//认证管理器
                .tokenStore(tokenStore)                       //令牌存储
                .userDetailsService(userDetailsService)     //用户信息service
                .allowedTokenEndpointRequestMethods(HttpMethod.POST);
    }

    //设置授权码模式的授权吗如何存取
    @Bean
    public AuthorizationCodeServices authenticationCodeServices(){
        return new InMemoryAuthorizationCodeServices();
    }

    /***
     * 授权服务器的安全配置
     * @param oauthServer
     * @throws Exception
     * *  配置：安全检查流程,用来配置令牌端点（Token Endpoint）的安全与权限访问
     * 	 *  默认过滤器：BasicAuthenticationFilter
     * 	 *  1、oauth_client_details表中clientSecret字段加密【ClientDetails属性secret】
     * 	 *  2、CheckEndpoint类的接口 oauth/check_token 无需经过过滤器过滤，默认值：denyAll()
     * 	 * 对以下的几个端点进行权限配置：
     * 	 * /oauth/authorize：授权端点
     * 	 * /oauth/token：令牌端点
     * 	 * /oauth/confirm_access：用户确认授权提交端点
     * 	 * /oauth/error：授权服务错误信息端点
     * 	 * /oauth/check_token：用于资源服务访问的令牌解析端点
     * 	 * /oauth/token_key：提供公有密匙的端点，如果使用JWT令牌的话
     */
    @Override
    public void configure(AuthorizationServerSecurityConfigurer oauthServer) throws Exception {
        oauthServer.allowFormAuthenticationForClients()
                .passwordEncoder(new BCryptPasswordEncoder())
                .tokenKeyAccess("permitAll()")///oauth/token_key是公开的
                .checkTokenAccess("isAuthenticated()")///oauth/token_key是需要权限的
                .allowFormAuthenticationForClients();//允许表单认证
    }


    //读取密钥的配置
    @Bean("keyProp")
    public KeyProperties keyProperties(){
        return new KeyProperties();
    }

    @Resource(name = "keyProp")
    private KeyProperties keyProperties;

    //客户端配置
    @Bean
    public ClientDetailsService  clientDetails() {
        return new JdbcClientDetailsService(dataSource);
    }

    @Bean
    @Autowired
    public TokenStore tokenStore(JwtAccessTokenConverter jwtAccessTokenConverter) {
        return new JwtTokenStore(jwtAccessTokenConverter);
    }

    /****
     * JWT令牌转换器
     * @param customUserAuthenticationConverter
     * @return
     */
    @Bean
    public JwtAccessTokenConverter jwtAccessTokenConverter(CustomUserAuthenticationConverter customUserAuthenticationConverter) {
        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
        KeyPair keyPair = new KeyStoreKeyFactory(
                keyProperties.getKeyStore().getLocation(),                          //证书路径 changgou.jks
                keyProperties.getKeyStore().getSecret().toCharArray())              //证书秘钥 changgouapp
                .getKeyPair(
                        keyProperties.getKeyStore().getAlias(),                     //证书别名 changgou
                        keyProperties.getKeyStore().getPassword().toCharArray());   //证书密码 changgou
        converter.setKeyPair(keyPair);
        //配置自定义的CustomUserAuthenticationConverter
        DefaultAccessTokenConverter accessTokenConverter = (DefaultAccessTokenConverter) converter.getAccessTokenConverter();
        accessTokenConverter.setUserTokenConverter(customUserAuthenticationConverter);
        return converter;
    }

    @Bean
    public AuthorizationServerTokenServices tokenServices(){
        DefaultTokenServices tokenServices = new DefaultTokenServices();
        tokenServices.setClientDetailsService(clientDetailsService);//客户端详情服务
        tokenServices.setSupportRefreshToken(true);//是否支持令牌刷新
        tokenServices.setTokenStore(tokenStore);//令牌存储策略
        tokenServices.setAccessTokenValiditySeconds(7200);//令牌默认有效期
        tokenServices.setRefreshTokenValiditySeconds(259200);
        return tokenServices;
    }
}

