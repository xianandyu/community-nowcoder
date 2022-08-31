package com.xianyu;

import com.xianyu.dao.elasticsearch.DiscussPostRepository;
import com.xianyu.dao.DiscussPostMapper;
import com.xianyu.domain.DiscussPost;
import com.xianyu.service.DiscussPostService;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.*;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@SpringBootTest
public class elasticsearchTest {

    @Autowired
    private DiscussPostRepository repository;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private DiscussPostService postService;

    @Autowired
    private ElasticsearchRestTemplate elasticTemplate;


    @Test
    public void Test() {
        repository.saveAll(discussPostMapper.selectDiscussPosts(101,0,100,0));
        repository.saveAll(discussPostMapper.selectDiscussPosts(102,0,100,0));
        repository.saveAll(discussPostMapper.selectDiscussPosts(103,0,100,0));
        repository.saveAll(discussPostMapper.selectDiscussPosts(111,0,100,0));
        repository.saveAll(discussPostMapper.selectDiscussPosts(112,0,100,0));
        repository.saveAll(discussPostMapper.selectDiscussPosts(131,0,100,0));
        repository.saveAll(discussPostMapper.selectDiscussPosts(132,0,100,0));
        repository.saveAll(discussPostMapper.selectDiscussPosts(133,0,100,0));
        repository.saveAll(discussPostMapper.selectDiscussPosts(134,0,100,0));


//        //修改
//        DiscussPost discussPost = discussPostMapper.selectDiscussById(184);
//        discussPost.setType(0);
//        repository.save(discussPost);


        //删除
//        repository.deleteById(224);
    }

    @Test//搜索
    public void Test01() {
        NativeSearchQuery searchQueryBuilder = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery("互联网寒冬", "title", "content"))
                .withSorts(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .withSorts(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .withSorts(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                .withPageable(PageRequest.of(0, 10))
                .withHighlightFields(
                        new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
                        new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
                ).build();
        SearchHits<DiscussPost> search = elasticTemplate.search(searchQueryBuilder, DiscussPost.class);
        SearchPage<DiscussPost> page = SearchHitSupport.searchPageFor(search, searchQueryBuilder.getPageable());

        System.out.println("===========01===========");
        for (SearchHit<DiscussPost> discussPostSearchHit : page) {
            DiscussPost discussPost = discussPostSearchHit.getContent();
            System.out.println(discussPost);
        }
//
//        System.out.println("======02===========");
//        for (SearchHit<DiscussPost> discussPostSearchHit : page) {
//            DiscussPost discussPost = discussPostSearchHit.getContent();
//            List<String> title = discussPostSearchHit.getHighlightFields().get("title");
//            if(title != null){
//                discussPost.setTitle(title.get(0));
//            }
//            List<String> content = discussPostSearchHit.getHighlightFields().get("content");
//            if(content != null){
//                discussPost.setContent(content.get(0));
//            }
//            System.out.println(discussPost.getTitle() + "===========" + discussPost.getContent());
//        }
    }
}
