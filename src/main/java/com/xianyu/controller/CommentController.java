package com.xianyu.controller;

import com.xianyu.domain.DiscussPost;
import com.xianyu.domain.Event;
import com.xianyu.event.EventProducer;
import com.xianyu.service.CommentService;
import com.xianyu.domain.Comment;
import com.xianyu.domain.User;
import com.xianyu.service.DiscussPostService;
import com.xianyu.utils.CommunityConstantUtil;
import com.xianyu.utils.HostHolderUtil;
import com.xianyu.utils.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Date;

@Controller
@RequestMapping(path = "/comment")
public class CommentController implements CommunityConstantUtil {

    @Autowired
    private HostHolderUtil hostHolderUtil;

    @Autowired
    private CommentService commentService;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private RedisTemplate redisTemplate;

    //评论回复功能
    @PostMapping(path = "/add/{discussPortId}")
    public String addComment(Comment comment, @PathVariable("discussPortId") int discussPortId) {
        User user = hostHolderUtil.getUser();
        if (user == null) {
            throw new IllegalArgumentException("您尚未登录");
        }
        comment.setCreateTime(new Date());
        comment.setStatus(0);
        comment.setUserId(user.getId());
        //添加评论
        commentService.addComment(comment);

        // 触发评论事件
        Event event = new Event()
                .setTopic(TOPIC_COMMENT)
                .setUserId(user.getId())
                .setEntityType(comment.getEntityType())
                .setEntityId(comment.getEntityId())
                .setData("postId", discussPortId);

        if (ENTITY_TYPE_POST == comment.getEntityType()) {
            DiscussPost target = discussPostService.findDiscussById(comment.getEntityId());//discussPortId
            event.setEntityUserId(target.getUserId());
        } else if (ENTITY_TYPE_COMMENT == comment.getEntityType()) {
            Comment target = commentService.findCommentByEntityId(comment.getEntityId());
            event.setEntityUserId(target.getUserId());
        }
        eventProducer.fireEvent(event);

        // 触发帖子事件
        if (ENTITY_TYPE_POST == comment.getEntityType()) {
            event = new Event()
                    .setTopic(TOPIC_PUBLIC)
                    .setUserId(user.getId())
                    .setEntityType(ENTITY_TYPE_POST)
                    .setEntityId(discussPortId);
            eventProducer.fireEvent(event);

            // 计算帖子分数
            String redisKey = RedisKeyUtil.getPostScoreKey();
            redisTemplate.opsForSet().add(redisKey,discussPortId);
        }

        return "redirect:/discuss/detail/" + discussPortId;
    }
}
