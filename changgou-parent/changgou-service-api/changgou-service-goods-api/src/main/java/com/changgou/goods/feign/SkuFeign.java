package com.changgou.goods.feign;

import com.changgou.goods.pojo.Sku;
import entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(name="goods",contextId = "goods-sku")
@RequestMapping(value = "/sku")
public interface SkuFeign {

    /**
     * 扣减库存
     * @param decrmap
     * @return
     */
    @GetMapping("/decr/count")
    public Result decrCount(@RequestParam Map<String,Object> decrmap);

    /***
     * 查询所有的Sku信息
     * 在实际项目中的sku信息数据上千万，所以不能直接一次性查询出来
     * 所以需要根据状态查询或者分页查询一次导入
     * @param
     * @return
     */
    @GetMapping
    Result<List<Sku>> findAll();

    /**
     * 根据条件查询skuList
     * @param sku
     * @return
     */
    @PostMapping(value = "/search" )
    public Result<List<Sku>> findList(@RequestBody(required = false)  Sku sku);

    /***
     * 根据ID查询Sku数据
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result<Sku> findById(@PathVariable Long id);
}
