package com.changgou.web.item.service.impl;

import com.alibaba.fastjson.JSON;
import com.changgou.goods.feign.CategoryFeign;
import com.changgou.goods.feign.SkuFeign;
import com.changgou.goods.feign.SpuFeign;
import com.changgou.goods.pojo.Sku;
import com.changgou.goods.pojo.Spu;
import com.changgou.web.item.service.PageService;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PageServiceImpl implements PageService {
    @Autowired
    private SkuFeign skuFeign;

    @Autowired
    private SpuFeign spuFeign;

    @Autowired
    private CategoryFeign categoryFeign;

    @Autowired
    private TemplateEngine templateEngine;//注入模板引擎

    @Value("${pagepath}")
    private String pagePath;//生成的静态文件存放路径
    /**
     * 根据spuid创建模板页
     * @param spuId
     */
    @Override
    public void createPageHtml(Long spuId) {
        //构建创建模板静态页的数据
        Context context = new Context();

        //查询三大类信息，category\sku\spu
        Map<String,Object> dateModel = buildDataModel(spuId);
        context.setVariables(dateModel);

        //准备文件
        File dir = new File(pagePath);
        //文件路径不存在就创建
        if(!dir.exists()){
            dir.mkdir();
        }
        //创建html文件
        File dest = new File(dir,spuId + ".html");
        //生成页面
        try{
            PrintWriter writer = new PrintWriter(dest, "UTF-8");

            templateEngine.process("item", context, writer);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除指定静态页
     * @param spuId
     */
    @Override
    public void deleteHtml(Long spuId) {
        //准备文件
        File dir = new File(pagePath);
        //文件路径不存在就创建
        if(dir.exists()){
            File dest = new File(dir,spuId + ".html");
            // 路径为文件且不为空则进行删除
            if (dest.isFile() && dest.exists()) {
                dest.delete();
            }
        }
    }

    /**
     * 构建三大类数据map
     * @param spuId
     * @return
     */
    private Map<String, Object> buildDataModel(Long spuId) {
        //构建数据模型
        Map<String,Object> dataMap = new HashMap<String,Object>();
        Result<Spu> result = spuFeign.findById(spuId);
        Spu spu = result.getData();

        //获取分类信息
        dataMap.put("category1",categoryFeign.findById(spu.getCategory1Id()).getData());
        dataMap.put("category2",categoryFeign.findById(spu.getCategory2Id()).getData());
        dataMap.put("category3",categoryFeign.findById(spu.getCategory3Id()).getData());

        //将照片获取
        if(null != spu.getImages()){
            dataMap.put("imageList", spu.getImages().split(","));
        }

        //获取规格参数
        dataMap.put("specificationList",JSON.parseObject(spu.getSpecItems(),Map.class));
        dataMap.put("spu",spu);

        //根据spuId查询Sku集合
        Sku skuCondition = new Sku();
        skuCondition.setSpuId(spu.getId());
        Result<List<Sku>> resultSku = skuFeign.findList(skuCondition);
        dataMap.put("skuList",resultSku.getData());
        return dataMap;
    }
}
