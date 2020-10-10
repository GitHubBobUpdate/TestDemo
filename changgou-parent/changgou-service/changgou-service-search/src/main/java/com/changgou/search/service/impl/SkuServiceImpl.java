package com.changgou.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.changgou.goods.feign.SkuFeign;
import com.changgou.goods.pojo.Sku;
import com.changgou.search.dao.SkuEsMapper;
import com.changgou.search.pojo.SkuInfo;
import com.changgou.search.service.SkuService;
import entity.Result;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;

@Service
public class SkuServiceImpl implements SkuService {
    //注入操作ElasticSearch的操作类，可实现es的增删改查
    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Autowired
    private SkuFeign skuFeign;

    @Autowired
    private SkuEsMapper skuEsMapper;

    /***
     * 导入SKU数据，在实际生产环境中采用分页导入实现，否则数据量过大会出现超时
     */
    @Override
    public void importSku() {
        //调用Good微服务中的Feign接口查询sku信息
        Result<List<Sku>> result = skuFeign.findAll();
        //将sku信息封装到SkuInfo中
        //将Feign查询到的数据，转成String之后转成List
        List<SkuInfo> skuInfos = JSON.parseArray(JSON.toJSONString(result.getData()), SkuInfo.class);

        //将导入到ES的数据生成动态ES域
        for (SkuInfo skuInfo : skuInfos) {
            //获取spec-->Map(String,String)map类型{"电视音响效果":"小影院","尺码":"165"}
            Map<String, Object> specMap= JSON.parseObject(skuInfo.getSpec()) ;
            skuInfo.setSpecMap(specMap);
        }
        //将数据导入Es
        skuEsMapper.saveAll(skuInfos);
    }

    /**
     * 根据关键字搜索
     * @param searchMap 将需要搜索的关键字铜鼓map传入，当然其他的条件也可以通过Map传入
     * @return 返回也可以通过一个map返回，包括查询出来的数据，以及其他的数据，比如页数，总页数....
     */
    @Override
    public Map search(Map<String, String> searchMap) {
        //构建查询条件
        NativeSearchQueryBuilder queryBuilder = buildBasicQuery(searchMap);
        /**
         * 高亮设置
         *         /**
         *          * 1.执行查询，获取所有数据--结果集【包含高亮数据和非高亮数据】
         *          * 2.分析结果集数据，获取高亮数据--只有某个域的高亮数据
         *          * 3.非高亮数据中指定的域替换成高亮数据
         *          * 4.将数据返回
         *         * */
        setHighLight(queryBuilder);
        NativeSearchQuery query = queryBuilder.build();

        //查询关键字对应的数据
        //查询出来的数据是skuInfo的javaBean，因为我们导入数据的时候，也是根据这个javaBean导入的
        AggregatedPage<SkuInfo> skuInfos = elasticsearchTemplate
                .queryForPage(
                        query,   //搜索条件封装
                        SkuInfo.class,          //数据集合要转换成类型的字节码
                        new SearchResultMapperImpl()      //执行搜索之后将结果集封装到对象中,并且将高亮数据替换
                );

        //封装数据给页面
        Map<String,Object> resultMap = new HashMap<String,Object>();

        //搜索分组数据，并且封装进入返回Map中：包含规格/分类/品牌
        searchGroupList(queryBuilder, searchMap, resultMap);

        //获取分页数据
        Pageable pageable = query.getPageable();
        resultMap.put("pageNum",pageable.getPageNumber());
        resultMap.put("pageSize",pageable.getPageSize());

        //关键字查询出来的数据
        resultMap.put("rows",skuInfos.getContent());
        //查询出来数据的总记录数
        resultMap.put("total",skuInfos.getTotalElements());
        //查询出来数据的总页数
        resultMap.put("totalPages",skuInfos.getTotalPages());

        return resultMap;
    }

    private void setHighLight(NativeSearchQueryBuilder queryBuilder) {
        //添加高亮设置
        /**
         * 指定高亮域，也就是设置哪个域需要高亮显示
         *   设置高亮域的时候，需要指定前缀和后缀，也就是关键词用什么html标签包裹，再给该标签样式
         *
         */
        HighlightBuilder.Field field = new HighlightBuilder.Field("name");//指定高亮域
        //设置前缀和后缀
        field.preTags("<em style=\"color:red\">").postTags("</em>");

        //设置碎片长度（也就是在页面显示时，显示的标题长度，在高亮前后，一共显示多长）
        field.fragmentOffset(100);

        queryBuilder.withHighlightFields(field);
    }

    /**
     * 封装查询条件
     * 当我们针对关键字查询、分类查询、品牌查询需要进行组合查询 BoolQuery must\must_not\should
     * 因为不能说我们查询华为品牌的数据，本来华为只做手机和内存，然后你查询出来的分类信息拥有户外工具，美术用品这些分类
     * @param searchMap
     * @return
     */
    private NativeSearchQueryBuilder buildBasicQuery(Map<String, String> searchMap) {
        //通过页面除传入的数据构建查询条件，分类分组数据的构建条件是一样的，只是之后的查询是查询的分组，输出分组名
        //通过SearchQuery的实现类构建查询条件
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();

        //需要查询布尔组合查询
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        //查询茶树不为空，就设置查询条件
        if(null != searchMap && searchMap.size() > 0){
            //根据关键词进行查询
            String keywords = searchMap.get("keywords");
            if(!StringUtils.isEmpty(keywords)){
                //QueryBuilders.queryStringQuery(keywords).field("name")构建QueryBuild,通过String搜索并切匹配的是name域
                //queryBuilder.withQuery(QueryBuilders.queryStringQuery(keywords).field("name"));
                boolQueryBuilder.must(QueryBuilders.queryStringQuery(keywords).field("name"));
            }

            //输入了分类，根据分类组合查询
            if(!StringUtils.isEmpty(searchMap.get("category"))){
                boolQueryBuilder.must(QueryBuilders.termQuery("categoryName",searchMap.get("category")));
            }

            //输入了品牌，根据品牌组合查询
            if(!StringUtils.isEmpty(searchMap.get("brand"))){
                boolQueryBuilder.must(QueryBuilders.termQuery("brandName",searchMap.get("brand")));
            }

            //如果存在规格数据查询，也需要添加组合查询
            for (Map.Entry<String, String> entry : searchMap.entrySet()) {
                String entryKey = entry.getKey();
                if(entryKey.startsWith("spec_")){
                    String entryValue = entry.getValue().replace("\\","");
                    boolQueryBuilder.must(QueryBuilders.termQuery("specMap." + entryKey.substring(5) +".keyword",entryValue));
                }
            }

            //针对价格进行筛选
            String price = searchMap.get("price");
            if(!StringUtils.isEmpty(price)){
                //将传入的价格进行分割，0-500，500-1000，1000
                String[] splitPrice = price.split("-");
                if(null != splitPrice && splitPrice.length > 0){
                    boolQueryBuilder.must(QueryBuilders.rangeQuery("price").gte(Integer.parseInt(splitPrice[0])));
                    if(splitPrice.length > 1){
                        boolQueryBuilder.must(QueryBuilders.rangeQuery("price").lte(Integer.parseInt(splitPrice[1])));
                    }
                }
            }

        }
        //将布尔组合填充给NativeSearchQueryBuilder查询
        queryBuilder.withQuery(boolQueryBuilder);

        //构建排序查询
        //如果在真是生产环境中根据销量排序，那么还需要增加一个季度的参数，针对某个季度下的销量
        String sortRule = searchMap.get("sortRule");
            String sortField = searchMap.get("sortField");
        if (!StringUtils.isEmpty(sortRule) && !StringUtils.isEmpty(sortField)) {
            queryBuilder.withSort(
                    new FieldSortBuilder(sortField) //根据哪个域进行排序
                            .order(sortRule.equals("ASC") ? SortOrder.ASC : SortOrder.DESC));//升降序，当传入的是乱码，那么就默认降序排序
        }

        //设置分页查询
        Integer pageNum = coverterPage(searchMap);//当前页，当没传入默认是1，并且数字必须大于等于1
        Integer size = 30;//每页显示条数
        queryBuilder.withPageable(PageRequest.of(pageNum,size));

        return queryBuilder;
    }

    /**
     * 搜索分组集合数据
     * @param queryBuilder
     * @return
     */
    private void searchGroupList(NativeSearchQueryBuilder queryBuilder, Map<String, String> searchMap, Map<String,Object> resultMap) {
        //添加查询条件
        //根据哪个域进行分组查询,terms是设置别名
        //size表示从多少条数据中进行分组查询，默认是10条
        //优化代码，批量分组查询，防止多次调用es查询
        if(null == searchMap || StringUtils.isEmpty(searchMap.get("category"))){
            NativeSearchQueryBuilder aggregationBuilder = queryBuilder.addAggregation(AggregationBuilders.terms("skuCategorygroup").field("categoryName").size(50));
        }
        if(null == searchMap || StringUtils.isEmpty(searchMap.get("brand"))){
            NativeSearchQueryBuilder aggregationBuilder = queryBuilder.addAggregation(AggregationBuilders.terms("skuBrandgroup").field("brandName").size(50));
        }
        //规格必须查询
        NativeSearchQueryBuilder aggregationBuilder = queryBuilder.addAggregation(AggregationBuilders.terms("skuSpecgroup").field("spec.keyword").size(10000));

        //查询分类分组数据
        AggregatedPage<SkuInfo> aggregateds = elasticsearchTemplate.queryForPage(aggregationBuilder.build(), SkuInfo.class);

        /**
         * //获取分组数据
         *  aggregatedPage.getAggregations()获取的是一个集合，表示可以多个域进行设置
         *  get("skuCategorygroup")获取集合中对应的分组
         *  本来返回的是Aggregation，我们可以找他对应的实现类,StringTerms我们返回的是一个个String的数据
         *  .getBuckets()就是获取的返回集合数据，因为分组都是一个集合
         */
        //优化代码，批量分组查询，防止多次调用es查询
        if(null == searchMap || StringUtils.isEmpty(searchMap.get("category"))){
            StringTerms categoryTerms = aggregateds.getAggregations().get("skuCategorygroup");
            List<String> categoryList = getGroupList(categoryTerms);
            resultMap.put("categoryList",categoryList);
        }
        if(null == searchMap || StringUtils.isEmpty(searchMap.get("brand"))){
            StringTerms brandTerms = aggregateds.getAggregations().get("skuBrandgroup");
            List<String> brandList = getGroupList(brandTerms);
            resultMap.put("brandList",brandList);
        }
        StringTerms specTerms = aggregateds.getAggregations().get("skuSpecgroup");
        List<String> specList = getGroupList(specTerms);
        //将规格参数进行合并操作
        Map<String, Set<String>> specMap = getSpecMap(specList);
        resultMap.put("specMap",specMap);
    }

    /**
     * 从StringTerm中获取分组数据
     * @param stringTerms
     * @return
     */
    private List<String> getGroupList(StringTerms stringTerms) {
        List<String> groupList = new ArrayList<String>();
        for (StringTerms.Bucket bucket : stringTerms.getBuckets()) {
            groupList.add(bucket.getKeyAsString());
        }
        return groupList;
    }

    /**
     * 通过关键字查询出来的数据，查询响应的规格参数
     * @param specList
     * @return
     */
    private Map<String,Set<String>> getSpecMap(List<String> specList) {
        //1.定义一个map<String,Set<String>>存放返回数据
        Map<String,Set<String>> resultMap = new HashMap<String,Set<String>>();

        //2.将所有规格数据转换成Map
        for (String specJson : specList) {
            Map<String,String> map = JSON.parseObject(specJson, Map.class);
            for (Map.Entry<String, String> entry : map.entrySet()) {
                String entryKey = entry.getKey();//当前规格的key
                String entryValue = entry.getValue();//当前规格的value值

                //获取当前规格名字对应的规格数据,当为空，表示未出现过这种规格，重新添加
                //用于存放规格值，用set去重
                Set<String> specValues = resultMap.get(entryKey);
                if (null == specValues) {
                    specValues = new HashSet<String>();
                }
                //3.循环规格的Map，将数据填充到定义的Map<String,Set>中
                specValues.add(entryValue);
                //将规格存放到结果map中
                resultMap.put(entryKey,specValues);
            }
        }
        return resultMap;
    }

    private Integer coverterPage(Map<String, String> searchMap){
        int defaultPageNum = 1;
        if(null != searchMap && searchMap.size() > 0){
            String pageNum = searchMap.get("pageNum");
            try {
                if(!StringUtils.isEmpty(pageNum)){
                    if(Integer.parseInt(pageNum) < 1){
                        return defaultPageNum;
                    }
                    return Integer.parseInt(pageNum);
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return defaultPageNum;
    }
}
