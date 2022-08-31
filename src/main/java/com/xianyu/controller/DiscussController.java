package com.xianyu.controller;

import com.xianyu.domain.*;
import com.xianyu.event.EventProducer;
import com.xianyu.service.CommentService;
import com.xianyu.service.DiscussPostService;
import com.xianyu.service.LikeService;
import com.xianyu.service.UserService;
import com.xianyu.utils.CommunityConstantUtil;
import com.xianyu.utils.HostHolderUtil;
import com.xianyu.utils.RedisKeyUtil;
import com.xianyu.utils.ToJSONUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import static com.xianyu.utils.CommunityConstantUtil.*;

@Controller
@RequestMapping(path = "/discuss")
public class DiscussController {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private HostHolderUtil hostHolderUtil;

    @Autowired
    private CommentService commentService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private RedisTemplate redisTemplate;

    @PostMapping(path = "/add")
    @ResponseBody//返回的不是网页
    public String addDiscussPost(String title, String content) {
        if (StringUtils.isBlank(title)) {
            return ToJSONUtil.getJSONString(0, "标题不能为空");
        }
        User user = hostHolderUtil.getUser();
        if (user == null) {
            return ToJSONUtil.getJSONString(403, "你还没有登录哦!");
        }

        DiscussPost discussPost = new DiscussPost();
        discussPost.setUserId(user.getId());
        discussPost.setContent(content);
        discussPost.setTitle(title);
        discussPost.setCreateTime(new Date());
        discussPostService.addDiscussPort(discussPost);

        // 触发发帖事件
        Event event = new Event()
                .setTopic(TOPIC_PUBLIC)
                .setUserId(user.getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(discussPost.getId());
        eventProducer.fireEvent(event);

        // 计算帖子分数
        String redisKey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(redisKey,discussPost.getId());

        // 报错的情况,将来统一处理.
        return ToJSONUtil.getJSONString(1, "发布成功");
    }

    //查看帖子详情
    @GetMapping(path = "/detail/{id}")
    public String selectDiscussById(@PathVariable("id") int id, Model model, Page page) {
        //帖子
        DiscussPost post = discussPostService.findDiscussById(id);
        //判断帖子是否被删除
        if(post.getStatus() == 2){
            throw new IllegalArgumentException("该帖子不存在");
        }

        model.addAttribute("post", post);
        //作者
        User user = userService.findUserById(post.getUserId());
        model.addAttribute("user", user);

        //点赞详情
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, id);
        int likeStatus = hostHolderUtil.getUser() == null ? 0 :
                likeService.findEntityLikeStatus(hostHolderUtil.getUser().getId(), ENTITY_TYPE_POST, id);
        model.addAttribute("likeCount", likeCount);
        model.addAttribute("likeStatus", likeStatus);


        //设置分页
        page.setLimit(5);
        page.setPath("/discuss/detail/" + id);
        page.setRows(post.getCommentCount());

        //总回帖详情
        List<Comment> commentList = commentService.findCommentsByEntityId
                (ENTITY_TYPE_POST, id, page.getOffset(), page.getLimit());
        //总回帖详情
        List<Map<String, Object>> commentVoList = new ArrayList<>();
        for (Comment comment : commentList) {
            //存放子回帖详情的map
            Map<String, Object> commentVo = new HashMap<>();

            //回帖
            commentVo.put("comment", comment);
            //回帖人
            commentVo.put("user", userService.findUserById(comment.getUserId()));

            //查询回复列表
            List<Comment> replyList = commentService.findCommentsByEntityId
                    (CommunityConstantUtil.ENTITY_TYPE_COMMENT, comment.getId(), 0, Integer.MAX_VALUE);

            //点赞详情
            likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, comment.getId());
            likeStatus = hostHolderUtil.getUser() == null ? 0 :
                    likeService.findEntityLikeStatus(hostHolderUtil.getUser().getId(), ENTITY_TYPE_COMMENT, comment.getId());
            commentVo.put("likeCount", likeCount);
            commentVo.put("likeStatus", likeStatus);
            //存放回复详情的map
            List<Map<String, Object>> replyVoList = new ArrayList<>();
            if (replyList != null) {
                for (Comment reply : replyList) {
                    Map<String, Object> replyVo = new HashMap<>();
                    //回复
                    replyVo.put("reply", reply);
                    //作者
                    replyVo.put("user", userService.findUserById(reply.getUserId()));
                    //回帖目标用户
                    User target = reply.getTargetId() == 0 ? null : userService.findUserById(reply.getTargetId());
                    replyVo.put("target", target);

                    //点赞详情
                    likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, reply.getId());
                    likeStatus = hostHolderUtil.getUser() == null ? 0 :
                            likeService.findEntityLikeStatus(hostHolderUtil.getUser().getId(), ENTITY_TYPE_COMMENT, reply.getId());
                    replyVo.put("likeCount", likeCount);
                    replyVo.put("likeStatus", likeStatus);

                    replyVoList.add(replyVo);
                }
            }
            commentVo.put("replys", replyVoList);

            // 回复数量
            int replyCount = commentService.findCommentCount(CommunityConstantUtil.ENTITY_TYPE_COMMENT, comment.getId());
            commentVo.put("replyCount", replyCount);

            commentVoList.add(commentVo);
        }

        model.addAttribute("commentVoList", commentVoList);
        return "/site/discuss-detail";
    }

    //置顶
    @PostMapping(path = "/top")
    @ResponseBody//异步请求
    public String setTop(int id) {
        DiscussPost post = discussPostService.findDiscussById(id);
        int type = post.getType() ^ 1;
        discussPostService.updateDiscussType(id, type);

        // 触发发帖事件
        Event event = new Event()
                .setTopic(TOPIC_PUBLIC)
                .setUserId(hostHolderUtil.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);

        //发送给前端
        Map<String,Object> map = new HashMap();
        map.put("type",type);

        return ToJSONUtil.getJSONString(1,"操作成功",map);
    }

    //加精
    @PostMapping(path = "/wonderful")
    @ResponseBody//异步请求
    public String setWonderFul(int id) {
        DiscussPost post = discussPostService.findDiscussById(id);
        int status = post.getStatus();
        if (status == 2) {
            return ToJSONUtil.getJSONString(404, "该帖子已经被删除");
        }

        status = status ^ 1;
        int i = discussPostService.updateDiscussStatus(id, status);

        // 触发发帖事件
        Event event = new Event()
                .setTopic(TOPIC_PUBLIC)
                .setUserId(hostHolderUtil.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);

        //发送给前端
        Map<String,Object> map = new HashMap();
        map.put("status",status);

        return ToJSONUtil.getJSONString(1,"操作成功",map);
    }

    @PostMapping(path = "/delete")
    @ResponseBody
    public String setDelete(int id){
        discussPostService.updateDiscussStatus(id,2);

        // 触发删帖事件
        Event event = new Event()
                .setTopic(TOPIC_DELETE)
                .setUserId(hostHolderUtil.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);

        return ToJSONUtil.getJSONString(1,"删除成功,返回主页");
    }
}
