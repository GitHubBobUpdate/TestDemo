package com.changgou.goods.dao;
import com.changgou.goods.pojo.Sku;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import tk.mybatis.mapper.common.Mapper;

/****
 * @Author:admin
 * @Description:Sku的Dao
 * @Date 2019/6/14 0:12
 *****/
public interface SkuMapper extends Mapper<Sku> {
    /**
     * 扣减库存
     * @param id
     * @param num
     * @return
     *
     * @Param用于绑定别名，防止在编译之后将参数名编译成args0
     */
    @Update("update tb_sku set num = num-#{num} where id=#{id} and num>=#{num}")
    int decrCount(@Param("id") String id, @Param("num") Integer num);
}
