package com.changgou.gateway.web.filter;

import com.changgou.gateway.web.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 网关拦截器
 */
@Component
public class AuthorizeFilter implements GlobalFilter, Ordered {
    //令牌头名字
    private static final String AUTHORIZE_TOKEN = "Authorization";
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //获取Request、Response对象
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        //拦截请求时，判断当前的请求是否拦截
        String url = request.getURI().toString();
        if(URLFilter.hasAuthorize(url)){
            Mono<Void> filter = chain.filter(exchange);
            return filter;
        }

        //获取头文件中的令牌信息
        String token = request.getHeaders().getFirst(AUTHORIZE_TOKEN);

        //判断请求头中是否村存在token,默认存在
        boolean hasToken = true;

        //如果头文件中没有，则从请求参数中获取
        if (StringUtils.isEmpty(token)) {
            hasToken = false;
            token = request.getQueryParams().getFirst(AUTHORIZE_TOKEN);
        }

        //如果头文件和参数中都不存在就从cookies中获取
        if(StringUtils.isEmpty(token)){
            HttpCookie httpCookie = request.getCookies().getFirst(AUTHORIZE_TOKEN);
            if(null != httpCookie){
                hasToken = false;
                token =  httpCookie.getValue();
            }
        }

        //如果为空，则输出错误代码
        if (StringUtils.isEmpty(token)) {
            //设置方法不允许被访问，405错误代码
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }

        //当三个参数中都为空，那么就说明没有登录，需要报错
        if(StringUtils.isEmpty(token)){
            //解析失败，响应401错误
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            //表示未登录，那么就将当前的页面的请求记录，使得在登录之后可以跳转
            response.getHeaders().set("Location",url);
            return response.setComplete();
        }else {
            //当token存在时，要判断head中是否存在token,当在head中存在token那么就不需要将token存放到head中
            if(!hasToken){
                //将token进行处理之后存档
                if(!token.startsWith("bearer ") && token.startsWith("Bearer ")){
                    token += "bearer ";
                }
                //将token信息存放到head中
                request.mutate().header(AUTHORIZE_TOKEN,token);
            }
        }
        //注意在head中的Authorization字段，不能更改值，并且一个key可以存放多个value

        //放行
        return chain.filter(exchange);
    }


    /***
     * 过滤器执行顺序
     * @return
     */
    @Override
    public int getOrder() {
        return 0;
    }
}
