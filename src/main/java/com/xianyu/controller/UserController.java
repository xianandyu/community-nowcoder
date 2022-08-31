package com.xianyu.controller;

import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import com.xianyu.domain.Comment;
import com.xianyu.domain.DiscussPost;
import com.xianyu.domain.Page;
import com.xianyu.service.*;
import com.xianyu.annotation.LoginRequired;
import com.xianyu.domain.User;
import com.xianyu.utils.CommunityConstantUtil;
import com.xianyu.utils.GenerateCodeUtil;
import com.xianyu.utils.HostHolderUtil;
import com.xianyu.utils.ToJSONUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/User")
public class UserController implements CommunityConstantUtil {

    private Logger log = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private HostHolderUtil hostHolderUtil;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private FollowService followService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private DiscussPostService discussPostService;


    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Value("${qiniu.key.access}")
    private String accessKey;

    @Value("${qiniu.key.secret}")
    private String secretKey;

    @Value("${qiniu.bucket.header.url}")
    private String headerBucketUrl;

    @Value("${qiniu.bucket.header.name}")
    private String headerBucketName;

    @LoginRequired
    @GetMapping(path = "/setting")
    public String getSettingPage(Model model) {
        // 上传文件名称
        String fileName = GenerateCodeUtil.generateUUID();
        // 设置响应信息
        StringMap policy = new StringMap();
        policy.put("returnBody", ToJSONUtil.getJSONString(0));
        // 生成上传凭证
        Auth auth = Auth.create(accessKey, secretKey);
        String uploadToken = auth.uploadToken(headerBucketName, fileName, 3600, policy);

        model.addAttribute("uploadToken", uploadToken);
        model.addAttribute("fileName", fileName);

        System.out.println(fileName);
        return "/site/setting";


    }

    // 更新头像路径
    @PostMapping(path = "/header/url")
    @ResponseBody
    public String updateHeaderUrl(String fileName) {
        if (StringUtils.isBlank(fileName)) {
            return ToJSONUtil.getJSONString(1, "文件名不能为空!");
        }

        System.out.println(fileName);
        String url = headerBucketUrl + "/" + fileName;
        userService.updateHeaderUrl(hostHolderUtil.getUser().getId(), url);
        System.out.println(url);

        return ToJSONUtil.getJSONString(0);
    }

    @LoginRequired
    @PostMapping(path = "/updatePassword")
    public String updatePassword(String oldPassword, String newPassword, Model model) {
        User user = hostHolderUtil.getUser();
        Map<String, Object> map = userService.updatePassword(user.getId(), oldPassword, newPassword);

        if (map == null || map.isEmpty()) {
            return "redirect:/logout";
        } else {
            model.addAttribute("oldPasswordMsg", map.get("oldPasswordMsg"));
            model.addAttribute("newPasswordMsg", map.get("newPasswordMsg"));
            return "/site/setting";

        }
    }

    @GetMapping(path = "/profile/{userId}")
    public String getProfilePage(@PathVariable("userId") int userId, Model model) {
        User user = userService.findUserById(userId);

        if (user == null)
            throw new RuntimeException("用户不存在");

        model.addAttribute("user", user);

        //点赞
        int likeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount", likeCount);


        // 关注数量
        long followeeCount = followService.findFolloweeCount(userId, ENTITY_TYPE_USER);
        model.addAttribute("followeeCount", followeeCount);
        // 粉丝数量
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER, userId);
        model.addAttribute("followerCount", followerCount);
        // 是否已关注
        boolean hasFollowed = false;
        if (hostHolderUtil.getUser() != null) {
            hasFollowed = followService.hasFollowed(hostHolderUtil.getUser().getId(), ENTITY_TYPE_USER, userId);
        }
        model.addAttribute("hasFollowed", hasFollowed);
        return "site/profile";
    }

    @GetMapping(path = "/mypost/{userId}")
    public String getMyPost(@PathVariable("userId") int userId, Model model, Page page) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在！");
        }

        model.addAttribute("user", user);
        //分页信息
        page.setRows(discussPostService.findDiscussPostRows(userId));
        page.setPath("/User/mypost/" + userId);
        page.setLimit(5);

        List<DiscussPost> posts = discussPostService.findDiscussPosts(userId, page.getOffset(), page.getLimit(),0);
        List<Map<String, Object>> discussVOList = new ArrayList<>();
        if (posts != null) {
            for (DiscussPost post : posts) {
                Map<String, Object> map = new HashMap<>();
                map.put("post", post);
                map.put("likeCount", likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId()));
                discussVOList.add(map);
            }
        }

        model.addAttribute("discussPosts", discussVOList);
        return "/site/my-post";
    }

    @GetMapping(path = "/myreply/{userId}")
    public String getMyReply(@PathVariable("userId") int userId, Page page, Model model) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在");
        }
        model.addAttribute("user",user);
        // 分页信息
        page.setPath("/User/myreply/" + userId);
        page.setRows(commentService.findCountByUser(userId));
        page.setLimit(3);

        List<Comment> comments = commentService.findCommentsByUserId(userId, page.getOffset(), page.getLimit());
        List<Map<String,Object>> list = new ArrayList<>();
        if (comments != null) {
            for (Comment comment : comments) {
                Map<String, Object> map = new HashMap<>();
                map.put("comment", comment);
                DiscussPost post = discussPostService.findDiscussById(comment.getEntityId());
                map.put("discussPost", post);
                list.add(map);
            }
        }
        model.addAttribute("comments", list);
        return "site/my-reply";
    }

    // 废弃
    /**
     * 上传头像，并更新url
     */
    @LoginRequired
    @PostMapping(path = "/setting/upload")
    public String uploadHeader(MultipartFile headerImg, Model model) {
        if (headerImg == null) {
            model.addAttribute("error", "头像不能为空");
            return "/site/setting";
        }

        //得到源文件名字
        String filename = headerImg.getOriginalFilename();
        //文件后缀
        String suffix = filename.substring(filename.lastIndexOf("."));
        if (StringUtils.isBlank(suffix)) {
            model.addAttribute("error", "文件格式错误");
            return "/site/setting";
        }

        // 生成随机文件名
        filename = GenerateCodeUtil.generateUUID() + filename;
        // 确定文件存放的路径
        File file = new File(uploadPath + "/" + filename);
        try {
            //存进去
            headerImg.transferTo(file);
        } catch (IOException e) {
            log.error("文件上传错误:" + e.getMessage());
            throw new RuntimeException("上传文件失败,服务器发生异常!", e);
        }

        //获取当前用户
        User user = hostHolderUtil.getUser();
        String headerUrl = domain + contextPath + "/User/header/" + filename;
        //更新
        userService.updateHeaderUrl(user.getId(), headerUrl);
        return "redirect:/index";
    }

    // 废弃
    /**
     * 刷新头像
     *
     * @param filename
     * @param response
     */
    @GetMapping(path = "/header/{filename}")
    public void getHeader(@PathVariable("filename") String filename, HttpServletResponse response) {
        // 服务器存放路径
        filename = uploadPath + "/" + filename;
        // 文件后缀
        String suffix = filename.substring(filename.lastIndexOf("."));
        // 响应图片类型
        response.setContentType("image/" + suffix);
        try (
                FileInputStream fis = new FileInputStream(filename);
                OutputStream os = response.getOutputStream()
        ) {
            byte[] bytes = new byte[1024];
            int b = 0;
            while ((b = fis.read(bytes)) != -1) {
                os.write(bytes, 0, b);
            }
        } catch (IOException e) {
            log.error("读取头像失败: " + e.getMessage());
        }
    }
}
