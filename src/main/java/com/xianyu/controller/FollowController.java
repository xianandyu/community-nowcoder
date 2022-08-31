package com.xianyu.controller;

import com.xianyu.domain.Event;
import com.xianyu.domain.Page;
import com.xianyu.domain.User;
import com.xianyu.event.EventProducer;
import com.xianyu.service.FollowService;
import com.xianyu.service.UserService;
import com.xianyu.utils.CommunityConstantUtil;
import com.xianyu.utils.HostHolderUtil;
import com.xianyu.utils.ToJSONUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
public class FollowController implements CommunityConstantUtil {

    @Autowired
    private FollowService followService;

    @Autowired
    private HostHolderUtil hostHolderUtil;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private UserService userService;

    @PostMapping(path = "/follow")
    @ResponseBody
    public String follow(int entityType, int entityId) {
        User user = hostHolderUtil.getUser();

        followService.follow(user.getId(), entityType, entityId);

        // 触发关注事件
        Event event = new Event()
                .setTopic(TOPIC_FOLLOW)
                .setUserId(user.getId())
                .setEntityType(entityType)
                .setEntityId(entityId)//以后可以开发关注帖子等，目前只关注人
                .setEntityUserId(entityId);
        eventProducer.fireEvent(event);

        return ToJSONUtil.getJSONString(1, "关注成功");
    }

    @PostMapping(path = "/unfollow")
    @ResponseBody
    public String unfollow(int entityType, int entityId) {
        User user = hostHolderUtil.getUser();

        followService.unfollow(user.getId(), entityType, entityId);

        return ToJSONUtil.getJSONString(1, "取消关注");
    }

    @GetMapping(path = "/followees/{userId}")
    public String getFollowEes(@PathVariable("userId") int userId, Model model, Page page) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在");
        }
        model.addAttribute("user", user);

        page.setLimit(5);
        page.setPath("/followees/" + userId);
        page.setRows((int) followService.findFolloweeCount(userId, ENTITY_TYPE_USER));

        List<Map<String, Object>> userList = followService.findFollowEes(userId, page.getOffset(), page.getLimit());

        if (userList != null) {
            for (Map<String, Object> map : userList) {
                User u = (User) map.get("user");
                map.put("hasFollowed",hasFollowed(u.getId()));
            }
        }

        model.addAttribute("users",userList);
        return "/site/followee";
    }



    @GetMapping(path = "/followers/{userId}")
    public String getFollowErs(@PathVariable("userId") int userId, Model model, Page page) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在");
        }
        model.addAttribute("user", user);

        page.setLimit(5);
        page.setPath("/followers/" + userId);
        page.setRows((int) followService.findFollowerCount(ENTITY_TYPE_USER,userId));

        List<Map<String, Object>> userList = followService.findFollowErs(userId,page.getOffset(),page.getLimit());

        if (userList != null) {
            for (Map<String, Object> map : userList) {
                User u = (User) map.get("user");
                map.put("hasFollowed",hasFollowed(u.getId()));
            }
        }

        model.addAttribute("users",userList);
        return "/site/follower";
    }

    private boolean hasFollowed(int entityId) {
        User user = hostHolderUtil.getUser();
        if (user == null) {
            return false;
        }
        return followService.hasFollowed(user.getId(), ENTITY_TYPE_USER, entityId);
    }
}
