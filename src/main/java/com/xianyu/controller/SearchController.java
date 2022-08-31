package com.xianyu.controller;

import com.xianyu.domain.DiscussPost;
import com.xianyu.domain.Page;
import com.xianyu.service.ElasticsearchService;
import com.xianyu.service.LikeService;
import com.xianyu.service.UserService;
import com.xianyu.utils.CommunityConstantUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchPage;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class SearchController implements CommunityConstantUtil {

    @Autowired
    private ElasticsearchService elasticsearchService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private UserService userService;

    @GetMapping(path = "/search")
    public String search(String keyword, Page page, Model model) {
        SearchPage<DiscussPost> searchPage = elasticsearchService.searchDiscussPost(keyword, page.getCurrent() - 1, page.getLimit());

        List<Map<String, Object>> discussPosts = new ArrayList<Map<String, Object>>();
        if (searchPage != null) {
            for (SearchHit<DiscussPost> discussPostSearchHit : searchPage) {
                Map<String, Object> map = new HashMap<>();
                //帖子
                DiscussPost post = discussPostSearchHit.getContent();
                map.put("post", post);
                // 作者
                map.put("user", userService.findUserById(post.getUserId()));
                //点赞
                map.put("likeCount", likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId()));

                discussPosts.add(map);
            }
        }

        model.addAttribute("discussPosts", discussPosts);
        model.addAttribute("keyword", keyword);

        // 分页信息
        page.setPath("/search?keyword=" + keyword);
        page.setRows(searchPage == null ? 0 : (int) searchPage.getTotalElements());

        return "/site/search";
    }
}
