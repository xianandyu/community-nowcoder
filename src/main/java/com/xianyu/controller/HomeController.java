package com.xianyu.controller;

import com.xianyu.service.DiscussPostService;
import com.xianyu.service.LikeService;
import com.xianyu.service.UserService;
import com.xianyu.domain.DiscussPost;
import com.xianyu.domain.Page;
import com.xianyu.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.xianyu.utils.CommunityConstantUtil.ENTITY_TYPE_POST;

@Controller
public class HomeController {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;


    /**
     * 首页功能
     *
     * @param model
     * @param page
     * @return 返回视图层即html
     * path 访问路径
     * @responseBody注解的作用是将controller的方法返回的对象通过适当的转换器转换为指定的格式之后， 写入到response对象的body区，通常用来返回JSON数据或者是XML数据
     * public User login(User user){
     * 　　　　return user;
     * 　　}
     * 　　User字段：userName pwd
     * 　　那么在前台接收到的数据为：’{“userName”:“xxx”,“pwd”:“xxx”}’==>
     * response.getWriter.write(JSONObject.fromObject(user).toString());
     * <p>
     * <p>
     * 该方法相应的是网页因此不用responseBody
     */
    @GetMapping(path = "/index")
    public String getIndexPage(Model model, Page page, @RequestParam(name = "orderMode", defaultValue = "0") int orderMode) {
        //springMvc利用DispatcherServlet会自动封装
        page.setPath("/index?orderMode=" + orderMode);
        page.setRows(discussPostService.findDiscussPostRows(0));

        //找到帖子数量
        List<DiscussPost> list = discussPostService.findDiscussPosts(0, page.getOffset(), page.getLimit(), orderMode);
        List<Map<String, Object>> discussPosts = new ArrayList();
        //将DiscussPost与User分别装进，方便取出
        if (list != null) {
            for (DiscussPost post : list) {
                Map<String, Object> map = new HashMap<>();
                map.put("post", post);
                //作者
                User user = userService.findUserById(post.getUserId());
                map.put("user", user);
                discussPosts.add(map);

                //点赞详情
                long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId());

                map.put("likeCount", likeCount);
            }
        }
        model.addAttribute("discussPosts", discussPosts);
        model.addAttribute("orderMode",orderMode);

        return "/index";
    }

    @GetMapping(path = "/error")
    public String getErrorPage() {
        return "/error/500";
    }
}
