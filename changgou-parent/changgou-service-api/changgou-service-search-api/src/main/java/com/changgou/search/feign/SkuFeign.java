package com.changgou.search.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "search")
@RequestMapping("/search")
public interface SkuFeign {
    /**
     * 根据关键字搜索
     * @param searchMap 将需要搜索的关键字铜鼓map传入，当然其他的条件也可以通过Map传入
     * @return 返回也可以通过一个map返回，包括查询出来的数据，以及其他的数据，比如页数，总页数....
     */
    @GetMapping
    public Map search(@RequestParam(required = false) Map searchMap);

}
