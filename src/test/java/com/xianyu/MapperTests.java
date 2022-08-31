package com.xianyu;

import com.google.code.kaptcha.Producer;
import com.xianyu.service.CommentService;
import com.xianyu.service.MessageService;
import com.xianyu.dao.CommentMapper;
import com.xianyu.dao.DiscussPostMapper;
import com.xianyu.dao.LoginTicketMapper;
import com.xianyu.dao.UserMapper;
import com.xianyu.domain.*;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@SpringBootTest
public class MapperTests {

    @Autowired
    private UserMapper userMapper;

    @Test
    public void select() {
        User user;
        user = userMapper.selectById(138);
        System.out.println(user);

        user = userMapper.selectByEmail("nowcoder101@sina.com");
        System.out.println(user);

        user = userMapper.selectByName("liubei");
        System.out.println(user);
    }

    @Test
    public void insert(){
        User user = new User();
        user.setUsername("zhangsan");
        user.setPassword("123456");
        user.setSalt("abc");
        user.setEmail("144423@qq.com");
        user.setHeaderUrl("https://www.nowcoder.com/101.png");
        user.setCreateTime(new Date());

        userMapper.insertUser(user);
    }


    @Test
    public void update(){
        int i;
        i = userMapper.updateStatus(150, 0);
        System.out.println(i);

        i = userMapper.updatePassword(150,"1235678");
        System.out.println(i);

        i = userMapper.updateHeader(150,"http://images.nowcoder.com/head/104t.png");
        System.out.println(i);
    }






    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Test
    public void selectByDiscussPort(){
//        List<DiscussPost> discussPosts = discussPostMapper.selectDiscussPosts(149, 0, 3);
//        for (DiscussPost discussPost : discussPosts) {
//            System.out.println(discussPost.getClass());
//        }
//        int i = discussPostMapper.selectDiscussPostRows(149);
//        System.out.println(i);
        DiscussPost discussPost = discussPostMapper.selectDiscussById(280);
        System.out.println(discussPost);
    }

    @Test
    public void selectByDiscussPort01(){
        DiscussPost discussPost = new DiscussPost();
        discussPost.setUserId(111);
        discussPost.setCreateTime(new Date());
        discussPost.setContent("xxxx");
        discussPost.setTitle("xxx");
        discussPostMapper.insertDiscussPost(discussPost);
    }

    @Autowired
    private Producer producer;

    @Test
    public void test01(){
        String text = producer.createText();
        System.out.println(text);
    }

    @Autowired
    private LoginTicketMapper loginTicketMapper;

    @Test
    public void text02(){
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(101);
        loginTicket.setTicket("abc");
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + 1000 * 60 * 10));

        loginTicketMapper.insertLoginTicket(loginTicket);
    }

    @Test
    public void testSelectLoginTicket() {
        LoginTicket loginTicket = loginTicketMapper.selectByTicket("abc");
        System.out.println(loginTicket);

        loginTicketMapper.updateStatus("abc", 1);
        loginTicket = loginTicketMapper.selectByTicket("abc");
        System.out.println(loginTicket);
    }

    @Autowired
    private CommentMapper commentMapper;
    @Autowired
    private CommentService commentService;

    @Test
    public void testSelectLoginTicket01() {
        List<Comment> comments = commentMapper.selectCommentsByEntity(1, 275, 0, 10);
        System.out.println(comments);
        System.out.println("============================");
        List<Comment> commentByEntityId = commentService.findCommentsByEntityId(1, 275, 0, 10);
        System.out.println(commentByEntityId);

        int i = commentMapper.selectCountByEntity(1, 228);
        System.out.println(i);
    }

    @Autowired
    private MessageService messageService;
    @Test
    public void test011(){
        List<Integer> integers = new ArrayList<>();

    }

}
