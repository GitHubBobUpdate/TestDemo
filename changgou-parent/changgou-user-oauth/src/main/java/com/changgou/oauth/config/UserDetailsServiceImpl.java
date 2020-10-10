package com.changgou.oauth.config;
import com.changgou.oauth.util.UserJwt;
import com.changgou.user.feign.UserFeign;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/*****
 * 自定义授权认证类
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    ClientDetailsService clientDetailsService;

    @Autowired
    private UserFeign userFeign;

    /****
     * 自定义授权认证
     * @param username
     * @return
     * @throws UsernameNotFoundException
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //=================================客户端信息验证 start===========================
        //取出身份，如果身份为空说明没有认证
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        //没有认证统一采用httpbasic认证，httpbasic中存储了client_id和client_secret，开始认证client_id和client_secret
        if(authentication==null){
            ClientDetails clientDetails = clientDetailsService.loadClientByClientId(username);
            if(clientDetails!=null){
                //秘钥
                String clientSecret = clientDetails.getClientSecret();
                //静态方式，写死了
                //return new User(username,new BCryptPasswordEncoder().encode(clientSecret), AuthorityUtils.commaSeparatedStringToAuthorityList(""));
                //数据库查找方式
                return new User(username,//客户端ID
                        clientSecret, //客户端密钥
                        AuthorityUtils.commaSeparatedStringToAuthorityList("")//拥有权限
                );
            }
        }
        //=================================客户端信息验证 end===========================
        //=================================用户信息验证 start===========================
        if (StringUtils.isEmpty(username)) {
            return null;
        }

        //调用user的feign来获取数据，但是由于每次访问之前会访问认证服务器，并且我们这时候并未登录没有token，所以访问不了
        /*1、没有令牌需要生成（一个admin的特殊令牌）
            2、令牌需要携带过去
            3、令牌需要存放到头文件中
            4、feign调用
        */
        //这里在之后去解决
        //临时解决方案是，给fandByid方法配置多个路径
        //最终的解决方案，在feign调用之前通过拦截器RequestIntrcepter生成token
        Result<com.changgou.user.pojo.User> userResult = userFeign.findById(username);
        if(null == userResult || null == userResult.getData()){
            return null;
        }

        //根据用户名查询用户信息
        String pwd = userResult.getData().getPassword();
                //new BCryptPasswordEncoder().encode("szitheima");

        //创建User对象
        String permissions = "goods_list";

        UserJwt userDetails = new UserJwt(username,pwd,AuthorityUtils.commaSeparatedStringToAuthorityList(permissions));


        //userDetails.setComy(songsi);
        return userDetails;
        //=================================用户信息验证 end===========================
    }
}
