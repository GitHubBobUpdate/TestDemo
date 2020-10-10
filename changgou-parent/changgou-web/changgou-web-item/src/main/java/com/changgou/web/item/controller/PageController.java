package com.changgou.web.item.controller;

import com.changgou.web.item.service.PageService;
import entity.Result;
import entity.StatusCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 生成静态模板页
 * 1、通过传入的spuID,因为我们在页面看到的只是一个公共属性，详情页会看到具体某些规格，根据id调用feign查询数据
 *      包含三块数据，categroy数据、spu数据、skuList数据
 * 2、通过thymleaf的自带API生成静态模板
 * 3、填充静态模板
 *
 */
@RestController
@RequestMapping("/page")
public class PageController {
    @Autowired
    private PageService pageService;
    /**
     * 生成静态页面
     * @param id
     * @return
     */
    @RequestMapping("/createHtml/{id}")
    public Result createHtml(@PathVariable("id") Long id){
        pageService.createPageHtml(id);
        return new Result(true, StatusCode.OK,"创建静态页成功");
    }

    /**
     * 删除指定的静态页
     */
    @RequestMapping("/deleteHtml/{id}")
    public Result deleteHtml(@PathVariable("id") Long id){
        pageService.deleteHtml(id);
        return new Result(true, StatusCode.OK,"删除静态页成功");
    }

}
