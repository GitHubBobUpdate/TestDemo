package com.changgou;

import com.changgou.seckill.pojo.SeckillGoods;
import com.netflix.discovery.converters.Auto;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import sun.plugin2.os.windows.SECURITY_ATTRIBUTES;

import java.util.ArrayList;
import java.util.List;
@RunWith(SpringRunner.class)
@SpringBootTest
public class redisDemo {
    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    public void test(){
        List<SeckillGoods> list = new ArrayList<SeckillGoods>();
        SeckillGoods seckillGoods = new SeckillGoods();
        seckillGoods.setId(1165161651651651L);
        seckillGoods.setName("seckillGoods_1");
        seckillGoods.setCostPrice("88888");
        seckillGoods.setNum(99);
        list.add(seckillGoods);

        SeckillGoods seckillGoods1 = new SeckillGoods();
        seckillGoods1.setId(11654464565646L);
        seckillGoods.setName("seckillGoods_2");
        seckillGoods.setCostPrice("88888");
        seckillGoods.setNum(100);
        list.add(seckillGoods1);

        SeckillGoods seckillGoods2 = new SeckillGoods();
        seckillGoods1.setId(11654464565555L);
        seckillGoods.setCostPrice("88888");
        seckillGoods.setNum(101);
        list.add(seckillGoods2);

        SeckillGoods seckillGoods3 = new SeckillGoods();
        seckillGoods1.setId(11654464666666L);
        seckillGoods.setCostPrice("88888");
        seckillGoods.setNum(99);
        list.add(seckillGoods3);

        for (SeckillGoods goods : list) {
            Boolean price_8888 = redisTemplate.boundHashOps("price_8888").putIfAbsent(goods.getName(), goods);
            System.out.println("当前的值" +goods.getId() +  "是否成功：" + price_8888);
        }



    }
}
