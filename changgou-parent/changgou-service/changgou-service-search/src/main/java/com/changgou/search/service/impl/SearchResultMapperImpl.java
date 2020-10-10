package com.changgou.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.changgou.search.pojo.SkuInfo;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;

import java.util.ArrayList;
import java.util.List;

/**
 * 实现高亮设置时，非高亮数据替换高亮数据实现
 */
public class SearchResultMapperImpl implements SearchResultMapper {
    @Override
    public <T> AggregatedPage<T> mapResults(SearchResponse searchResponse, Class<T> aClass, Pageable pageable) {
        //存放替换完之后的数据
        List<T> resultList = new ArrayList<T>();

        //循环搜索出来的数据【非高亮数据和高亮数据】
        for (SearchHit hit : searchResponse.getHits()) {
            //分析数据获取非高亮数据,并将数据映射成相应对象
            SkuInfo skuInfo = JSON.parseObject(hit.getSourceAsString(), SkuInfo.class);

            //获取高亮数据
            HighlightField highlightField = hit.getHighlightFields().get("name");
            if(null != highlightField && null != highlightField.getFragments()){
                StringBuffer buffer = new StringBuffer();
                Text[] fragments = highlightField.getFragments();
                for (Text fragment : fragments) {
                    buffer.append(fragment.toString());
                }
                //将高亮数据替换非高亮数据
                skuInfo.setName(buffer.toString());
            }
            resultList.add((T) skuInfo);
        }
        //将高亮数据返回
        //AggregatedPageImpl(List<T> content, Pageable pageable, long total)
        return new AggregatedPageImpl(resultList,pageable,searchResponse.getHits().getTotalHits());
    }
}
