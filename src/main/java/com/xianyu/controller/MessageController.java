package com.xianyu.controller;

import com.alibaba.fastjson.JSONObject;
import com.xianyu.service.MessageService;
import com.xianyu.service.UserService;
import com.xianyu.domain.Message;
import com.xianyu.domain.Page;
import com.xianyu.domain.User;
import com.xianyu.utils.CommunityConstantUtil;
import com.xianyu.utils.HostHolderUtil;
import com.xianyu.utils.ToJSONUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.HtmlUtils;

import java.util.*;

@Controller
@RequestMapping(path = "/letter")
public class MessageController implements CommunityConstantUtil {

    @Autowired
    private MessageService messageService;

    @Autowired
    private HostHolderUtil hostHolderUtil;

    @Autowired
    private UserService userService;

    /**
     * 获取私信列表
     *
     * @param model
     * @param page
     * @return
     */
    @GetMapping(path = "/list")
    public String getLetterList(Model model, Page page) {
        User user = hostHolderUtil.getUser();

        //设置分页信息
        page.setLimit(5);
        page.setPath("/letter/list");
        page.setRows(messageService.findConversationCount(user.getId()));

        // 会话列表
        List<Message> messageList = messageService.findConversions(user.getId(), page.getOffset(), page.getLimit());
        List<Map<String, Object>> conversations = new ArrayList<>();
        if (messageList != null) {
            for (Message message : messageList) {
                Map<String, Object> map = new HashMap<>();
                // 会话
                map.put("conversation", message);
                //私信数量
                map.put("letterCount", messageService.findLetterCount(message.getConversationId()));
                //未读私信数量
                map.put("unReadLetterCount", messageService.findLetterUnreadCount
                        (user.getId(), message.getConversationId()));
                //目标用户
                User target = getLetterTarget(message.getConversationId());
                map.put("target", target);

                conversations.add(map);
            }
        }

        model.addAttribute("conversations", conversations);

        // 查询未读消息数量
        //私信
        int letterUnreadCount = messageService.findLetterUnreadCount(user.getId(), null);
        model.addAttribute("letterUnreadCount",letterUnreadCount);
        //通知类
        int noticeUnreadCount = messageService.findNoticeUnreadCount(user.getId() , null);
        model.addAttribute("noticeUnreadCount",noticeUnreadCount);

        return "site/letter";
    }

    /**
     * 获取私信详情
     *
     * @param conversationId
     * @param model
     * @param page
     * @return
     */
    @GetMapping(path = "/detail/{conversationId}")
    public String getLetterDetail(@PathVariable("conversationId") String conversationId, Model model, Page page) {
        //设置分页信息
        page.setRows(messageService.findLetterCount(conversationId));
        page.setLimit(5);
        page.setPath("/letter/detail/" + conversationId);

        List<Message> letterList = messageService.findLetters(conversationId, page.getOffset(), page.getLimit());

        List<Map<String, Object>> letters = new ArrayList<>();
        if (letterList != null) {
            for (Message letter : letterList) {
                Map<String, Object> map = new HashMap<>();
                //存放私信
                map.put("letter", letter);
                map.put("fromUser", userService.findUserById(letter.getFromId()));
                letters.add(map);
            }
        }
        model.addAttribute("letters", letters);

        //存放目标信息
        model.addAttribute("target", getLetterTarget(conversationId));

        //更新已读状态
        List<Integer> ids = getLetterIds(letterList);
        if(!ids.isEmpty()){
            messageService.updateLetterStatus(ids);
        }


        return "site/letter-detail";
    }

    //发送私信
    @PostMapping(path = "/send")
    @ResponseBody
    public String sendMessage(String toName, String content) {
        User target = userService.findUserByName(toName);
        if (target == null) {
            return ToJSONUtil.getJSONString(0, "目标用户不存在!");
        }

        Message message = new Message();

        //通信用户
        message.setFromId(hostHolderUtil.getUser().getId());
        message.setToId(target.getId());
        //回复信息
        message.setContent(content);

        message.setCreateTime(new Date());

        if (message.getFromId() < message.getToId()) {
            message.setConversationId(message.getFromId() + "_" + message.getToId());
        } else {
            message.setConversationId(message.getToId() + "_" + message.getFromId());
        }

        messageService.addLetter(message);
        return ToJSONUtil.getJSONString(1);
    }



    @PostMapping(path = "/delete")
    @ResponseBody
    public String deleteLetter(int id){
        messageService.deleteLetter(id);
        return ToJSONUtil.getJSONString(1);
    }

    @GetMapping(path = "/notice")
    public String getNoticeList(Model model){
        User user = hostHolderUtil.getUser();


        // 查询评论类通知
        Message message = messageService.findLatestNotice(user.getId(), TOPIC_COMMENT);
        Map<String, Object> messageVO = new HashMap<>();
        if(message != null){
            messageVO.put("message",message);

            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);

            messageVO.put("user",userService.findUserById((Integer) data.get("userId")));
            messageVO.put("entityType",data.get("entityType"));
            messageVO.put("entityId",data.get("entityId"));
            messageVO.put("post",data.get("post"));

            int count = messageService.findNoticeCount(user.getId(), TOPIC_COMMENT);
            messageVO.put("count",count);
            int unread = messageService.findNoticeUnreadCount(user.getId(), TOPIC_COMMENT);
            messageVO.put("unread",unread);

            model.addAttribute("commentNotice",messageVO);
        }

        // 查询点赞类通知
        message = messageService.findLatestNotice(user.getId(), TOPIC_LIKE);
        messageVO = new HashMap<>();
        if(message != null){
            messageVO.put("message",message);

            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);

            messageVO.put("user",userService.findUserById((Integer) data.get("userId")));
            messageVO.put("entityType",data.get("entityType"));
            messageVO.put("entityId",data.get("entityId"));
            messageVO.put("post",data.get("post"));

            int count = messageService.findNoticeCount(user.getId(), TOPIC_LIKE);
            messageVO.put("count",count);
            int unread = messageService.findNoticeUnreadCount(user.getId(), TOPIC_LIKE);
            messageVO.put("unread",unread);

            model.addAttribute("likeNotice",messageVO);
        }

        //关注类通知
        message = messageService.findLatestNotice(user.getId(), TOPIC_FOLLOW);
        messageVO = new HashMap<>();
        if(message != null){
            messageVO.put("message",message);

            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);

            messageVO.put("user",userService.findUserById((Integer) data.get("userId")));
            messageVO.put("entityType",data.get("entityType"));
            messageVO.put("entityId",data.get("entityId"));

            int count = messageService.findNoticeCount(user.getId(), TOPIC_FOLLOW);
            messageVO.put("count",count);
            int unread = messageService.findNoticeUnreadCount(user.getId(), TOPIC_FOLLOW);
            messageVO.put("unread",unread);

            model.addAttribute("followNotice",messageVO);
        }


        // 查询未读消息数量
        //私信
        int letterUnreadCount = messageService.findLetterUnreadCount(user.getId(), null);
        model.addAttribute("letterUnreadCount",letterUnreadCount);
        //通知类
        int noticeUnreadCount = messageService.findNoticeUnreadCount(user.getId() , null);
        model.addAttribute("noticeUnreadCount",noticeUnreadCount);

        return "/site/notice";
    }

    @GetMapping(path = "/notice/detail/{topic}")
    public String getNoticeDetail(Model model,@PathVariable("topic")String topic,Page page){
        User user = hostHolderUtil.getUser();

        page.setLimit(5);
        page.setPath("/letter/notice/detail/" + topic);
        page.setRows(messageService.findNoticeCount(user.getId(), topic));

        List<Message> notices = messageService.findNotices(user.getId(), topic, page.getOffset(), page.getLimit());
        List<Map<String,Object>> noticeVoList = new ArrayList<>();
        if(notices!=null){
            for (Message notice : notices) {
                Map<String, Object> map = new HashMap<>();
                // 通知
                map.put("notice",notice);

                //内容
                //转格式
                String content = HtmlUtils.htmlUnescape(notice.getContent());
                Map data = JSONObject.parseObject(content, HashMap.class);
                //点赞者
                map.put("user",userService.findUserById((Integer) data.get("userId")));
                map.put("entityType",data.get("entityType"));
                map.put("entityId",data.get("entityId"));
                map.put("postId",data.get("postId"));

                //通知者==>系统
                map.put("fromUser",userService.findUserById(notice.getFromId()));

                noticeVoList.add(map);
            }
        }
        model.addAttribute("notices", noticeVoList);

        // 设置已读
        List<Integer> Ids = getLetterIds(notices);
        if(!Ids.isEmpty()){
            messageService.updateLetterStatus(Ids);
        }


        return "/site/notice-detail";
    }

    private User getLetterTarget(String conversationId) {
        String[] ids = conversationId.split("_");
        int id0 = Integer.parseInt(ids[0]);
        int id1 = Integer.parseInt(ids[1]);
        int id = hostHolderUtil.getUser().getId() == id0 ? id1 : id0;
        return userService.findUserById(id);
    }

    private List<Integer> getLetterIds(List<Message> letterList) {
        List<Integer> ids = new ArrayList<>();
        if (letterList != null) {
            for (Message message : letterList) {
                if (hostHolderUtil.getUser().getId() == message.getToId() && message.getStatus() == 0) {
                    ids.add(message.getId());
                }
            }
        }
        return ids;
    }
}
