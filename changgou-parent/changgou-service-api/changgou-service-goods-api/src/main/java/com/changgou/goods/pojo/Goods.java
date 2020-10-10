package com.changgou.goods.pojo;

import java.io.Serializable;
import java.util.List;

/**
 * 描述
 *
 * @author www.itheima.com
 * @version 1.0
 * @package com.changgou.goods.pojo *
 * @since 1.0
 */
public class Goods implements Serializable {
    //SPU信息
    private Spu spu;
    //sku信息，因为sku信息时商品的特殊属性，所以是一个List
    private List<Sku> skuList;

    public Spu getSpu() {
        return spu;
    }

    public void setSpu(Spu spu) {
        this.spu = spu;
    }

    public List<Sku> getSkuList() {
        return skuList;
    }

    public void setSkuList(List<Sku> skuList) {
        this.skuList = skuList;
    }
}
