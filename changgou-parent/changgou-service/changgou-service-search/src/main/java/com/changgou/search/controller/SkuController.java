package com.changgou.search.controller;

import com.changgou.search.service.SkuService;
import entity.Result;
import entity.StatusCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping(value = "/search")
@CrossOrigin
public class SkuController {

    @Autowired
    private SkuService skuService;

    /**
     * 导入数据
     * @return
     */
    @GetMapping("/import")
    public Result importDate(){
        skuService.importSku();
        return new Result(true, StatusCode.OK,"导入数据到索引库中成功！");
    }

    /**
     * 根据关键字搜索
     * @param searchMap 将需要搜索的关键字铜鼓map传入，当然其他的条件也可以通过Map传入
     * @return 返回也可以通过一个map返回，包括查询出来的数据，以及其他的数据，比如页数，总页数....
     */
    @GetMapping
    public Map search(@RequestParam(required = false) Map searchMap){
        return skuService.search(searchMap);
    }
}
