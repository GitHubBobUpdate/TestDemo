package entity;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;

/**
 * 公共拦截器，在feign调用之前获取请求头中的令牌存放到feign调用的head中
 */
public class FeignInterceptor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate requestTemplate) {
        //获取请求附加属性
        try {
            ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if(null != requestAttributes){
                HttpServletRequest request = requestAttributes.getRequest();
                Enumeration<String> headerNames = request.getHeaderNames();

                if(null != headerNames){
                    while (headerNames.hasMoreElements()){
                        String name = headerNames.nextElement();
                        String value = request.getHeader(name);

                        //将head中的数据存放到feign请求的头中
                        requestTemplate.header(name,value);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
