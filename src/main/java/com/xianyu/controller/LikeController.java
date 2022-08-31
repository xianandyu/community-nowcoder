package com.xianyu.controller;

import com.xianyu.domain.Event;
import com.xianyu.domain.User;
import com.xianyu.event.EventProducer;
import com.xianyu.service.LikeService;
import com.xianyu.utils.CommunityConstantUtil;
import com.xianyu.utils.HostHolderUtil;
import com.xianyu.utils.RedisKeyUtil;
import com.xianyu.utils.ToJSONUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
public class LikeController implements CommunityConstantUtil {

    @Autowired
    private LikeService likeService;

    @Autowired
    private HostHolderUtil hostHolderUtil;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private RedisTemplate redisTemplate;

    @PostMapping(path = "/like")
    @ResponseBody
    public String like(int entityType,int entityId,int entityUserId,int postId){
        User user = hostHolderUtil.getUser();

        //点赞
        likeService.like(user.getId(),entityType,entityId,entityUserId);
        //点赞数量
        long likeCount = likeService.findEntityLikeCount(entityType, entityId);
        //点赞与否
        int likeStatus = likeService.findEntityLikeStatus(user.getId(), entityType, entityId);

        // 返回的结果
        Map<String, Object> map = new HashMap<>();
        map.put("likeCount",likeCount);
        map.put("likeStatus",likeStatus);
        // 触发点赞事件
        if(likeStatus == 1){
            Event event = new Event()
                    .setTopic(TOPIC_LIKE)
                    .setUserId(user.getId())
                    .setEntityType(entityType)
                    .setEntityId(entityId)//评论回帖帖子的id
                    .setEntityUserId(entityUserId)
                    .setData("postId",postId);
            eventProducer.fireEvent(event);
        }

        // 计算帖子分数
        if(ENTITY_TYPE_POST == entityType){
            String redisKey = RedisKeyUtil.getPostScoreKey();
            redisTemplate.opsForSet().add(redisKey,postId);
        }

        return  ToJSONUtil.getJSONString(1, null, map);
    }
}
