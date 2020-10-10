package com.changgou.web.item.controller;


import com.changgou.search.feign.SkuFeign;
import com.changgou.search.pojo.SkuInfo;
import entity.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Controller
@RequestMapping(value = "/search")
public class SkuController {
    @Autowired
    private SkuFeign skuFeign;

    /**
     * 将查询到的search数据返回给thyemleaf渲染,就是springmvc
     * @param searchMap
     * @param model
     * @return
     */
    @GetMapping("/list")
    public String search(@RequestParam(required = false) Map<String, String> searchMap, Model model){
        handlerSearchMap(searchMap);
        //直接调用search微服务中的feign接口
        Map<String,Object> resultSearch = skuFeign.search(searchMap);

        //获取本次请求的地址
        String url = getUrl(searchMap);
        model.addAttribute("url",url);

        //获取本次请求的升降地址
        String sortUrl = getSortUrl(searchMap);
        model.addAttribute("sortUrl",sortUrl);

        model.addAttribute("result", resultSearch);

        //通过搜索框搜索之后将搜素条件回显在输入框
        model.addAttribute("searchMap", searchMap);

        //创建一个分页的对象  可以获取当前页 和总个记录数和显示的页码(以当前页为中心的5个页码)
        Page<SkuInfo> pageInfo = new Page<SkuInfo>(
                Long.valueOf(resultSearch.get("total").toString()),
                Integer.valueOf(resultSearch.get("pageNum").toString()),
                Integer.valueOf(resultSearch.get("pageSize").toString())
        );

        model.addAttribute("pageInfo", pageInfo);
        return "search";
    }

    /**
     * 获取用户每次的请求地址包含升降序地址
     * 页面需要在这次请求的地址上添加额外的搜索条件
     * @param searchMap
     */
    public String getUrl(Map<String,String> searchMap){
        //默认的请求
        String url = "/search/list";
        if(null != searchMap && searchMap.size() > 0){
            url += "?";
            //将搜索map中条件一个个拼接
            for (Map.Entry<String, String> stringEntry : searchMap.entrySet()) {
                String key = stringEntry.getKey();
                //当在请求参数中存在分页的参数就不拼接
                if(key.equals("pageNum")){
                    continue;
                }
                url += key+"="+stringEntry.getValue()+"&";
            }
            //当请求的地址中是以&结尾就需要将其截取掉
            if(url.lastIndexOf("&") != -1){
                url = url.substring(0,url.lastIndexOf("&"));
            }
        }
        return url;
    }

    /**
     * 获取用户每次的升降序地址
     * 页面需要在这次请求的地址上添加额外的搜索条件
     * @param searchMap
     */
    public String getSortUrl(Map<String,String> searchMap){
        //默认的请求
        String url = "/search/list";
        if(null != searchMap && searchMap.size() > 0){
            url += "?";
            //将搜索map中条件一个个拼接
            for (Map.Entry<String, String> stringEntry : searchMap.entrySet()) {
                String key = stringEntry.getKey();
                //当在请求参数中存在分页的参数或者升降序参数就不拼接
                if(key.equalsIgnoreCase("pageNum") || key.equalsIgnoreCase("sortRule") || key.equalsIgnoreCase("sortField")){
                    continue;
                }
                url += key+"="+stringEntry.getValue()+"&";
            }
            //当请求的地址中是以&结尾就需要将其截取掉
            if(url.lastIndexOf("&") != -1){
                url = url.substring(0,url.lastIndexOf("&"));
            }
        }
        return url;
    }

    /**
     * 将请求参数中的数据进行处理转义
     * @param searchMap
     */
    public void handlerSearchMap(Map<String,String> searchMap){
        if(null != searchMap && searchMap.size() > 0){
            for (Map.Entry<String, String> stringEntry : searchMap.entrySet()) {
                if(stringEntry.getKey().startsWith("spec_")){
                    stringEntry.setValue(stringEntry.getValue().replace("+","%2B"));
                }
            }
        }
    }
}
