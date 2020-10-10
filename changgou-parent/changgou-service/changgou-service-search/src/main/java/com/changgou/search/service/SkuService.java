package com.changgou.search.service;

import com.changgou.goods.pojo.Sku;

import java.util.List;
import java.util.Map;

public interface SkuService {
    /***
     * 导入SKU数据
     */
    void importSku();

    /**
     * 根据关键字搜索
     * @param searchMap 将需要搜索的关键字铜鼓map传入，当然其他的条件也可以通过Map传入
     * @return 返回也可以通过一个map返回，包括查询出来的数据，以及其他的数据，比如页数，总页数....
     */
    Map search(Map<String, String> searchMap);
}
