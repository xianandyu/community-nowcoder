package com.xianyu.controller;

import com.google.code.kaptcha.Producer;
import com.xianyu.service.UserService;
import com.xianyu.domain.User;
import com.xianyu.utils.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 重定向默认是Get请求
 */
@Controller
public class RegisterController {

    @Autowired
    private UserService userService;

    //验证码图片生成
    @Autowired
    private Producer producer;

    //网页
    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private MailClientUtil mailClient;

    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${server.servlet.context-path}")
    private String contextPath;



    private static final Logger logger = LoggerFactory.getLogger(RegisterController.class);

    @GetMapping(path = "/register")
    public String getRegisterPae() {
        return "/site/register";
    }

    @GetMapping(path = "/login")
    public String login() {
        return "/site/login";
    }

    @GetMapping(path = "/forget")
    public String toForgetPage(){
        return "/site/forget";
    }

    /**
     * 注册
     *
     * @param model
     * @param user
     * @return
     */
    @PostMapping("/register")
    public String register(Model model, User user) {
        Map<String, Object> map = userService.register(user);

        if (map == null || map.isEmpty()) {
            model.addAttribute("msg", "恭喜您，注册成功，请尽快激活");
            model.addAttribute("target", "/index");
            return "/site/operate-result";
        } else {
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            model.addAttribute("emailMsg", map.get("emailMsg"));
            return "/site/register";
        }
    }

    /**
     * 从路径中获取值，并传给相应的方法，完成激活
     *
     * @param model
     * @param userId
     * @param code
     * @return
     */
    @GetMapping("/activation/{userId}/{code}")
    public String activation(Model model, @PathVariable("userId") int userId, @PathVariable("code") String code) {
        int activation = userService.activation(userId, code);
        if (activation == CommunityConstantUtil.ACTIVATION_SUCCESS) {
            model.addAttribute("msg", "恭喜您，激活成功，将要跳转到登录页面");
            model.addAttribute("target", "/login");
        } else if (activation == CommunityConstantUtil.ACTIVATION_ERROR) {
            model.addAttribute("msg", "不好意思，您注册失败，激活码不正确");
            model.addAttribute("target", "/index");
        } else {
            model.addAttribute("msg", "无效操作,该账号已经激活过了!");
            model.addAttribute("target", "/login");
        }
        return "site/operate-result";
    }

    /**
     * 验证码的生成
     *
     * @param response
     */
    @GetMapping("/kaptcha")
    public void getKaptcha(HttpServletResponse response/*, HttpSession session*/) {
        //生成验证码与图片
        String text = producer.createText();
        BufferedImage image = producer.createImage(text);

        // 将验证码存入session
//        session.setAttribute("kaptcha", text);


        String ownerUUID = GenerateCodeUtil.generateUUID();
        // 验证码的归属(验证码从session改成redis)
        Cookie cookie = new Cookie("ownerUUID",ownerUUID);
        cookie.setMaxAge(60);
        cookie.setPath(contextPath);
        response.addCookie(cookie);
        // 将验证码存入Redis
        String redisKey = RedisKeyUtil.getKaptchaKey(ownerUUID);
        redisTemplate.opsForValue().set(redisKey,text,60, TimeUnit.SECONDS);


        // 将突图片输出给浏览器
        response.setContentType("image/png");
        try {
            ServletOutputStream os = response.getOutputStream();
            ImageIO.write(image, "png", os);
        } catch (IOException e) {
            logger.error("响应验证码失败:" + e.getMessage());
        }
    }

    /**
     * 登录验证
     *
     * @param username
     * @param password
     * @param code
     * @param rememberMe
     * @param model
     * @param response
     * @return
     */
    @PostMapping(path = "/login")
    public String login(String username, String password, String code, boolean rememberMe,
                        Model model, /*HttpSession session, */HttpServletResponse response,@CookieValue("ownerUUID")String ownerUUID) {

        //验证码 不符合返回登录页面
//        String kaptcha = (String) session.getAttribute("kaptcha");

        //redis版本
        String kaptcha = null;
        if(StringUtils.isNotBlank(ownerUUID)){
            String redisKey = RedisKeyUtil.getKaptchaKey(ownerUUID);
            kaptcha = (String) redisTemplate.opsForValue().get(redisKey);
        }

        if (StringUtils.isBlank(code) || StringUtils.isBlank(kaptcha) || !kaptcha.equalsIgnoreCase(code)) {
            model.addAttribute("codeMsg", "验证码错误");
            return "/site/login";
        }

        //验证账号信息
        int expiredSeconds = rememberMe ? CommunityConstantUtil.REMEMBER_TRUE : CommunityConstantUtil.REMEMBER_FALSE;
        Map<String, Object> map = userService.login(username, password, expiredSeconds);
        if (map.containsKey("ticket")) {
            Cookie cookie = new Cookie("ticket", map.get("ticket").toString());
            cookie.setPath(contextPath);
            cookie.setMaxAge(expiredSeconds);
            response.addCookie(cookie);
            return "redirect:/index";
        } else {
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            return "/site/login";
        }
    }

    @GetMapping(path = "/logout")
    public String logout(@CookieValue("ticket") String ticker) {
        userService.logout(ticker);
        SecurityContextHolder.clearContext();
        return "redirect:/login";
    }

    /**
     * 忘记密码---生成重置密码的验证码
     * 1:success 0 :false
     * @param email
     * @return
     */
    @GetMapping(path = "/forget/code")
    @ResponseBody
    public String getForgetCode(String email,HttpSession session) {
        if (StringUtils.isBlank(email)) {
            return ToJSONUtil.getJSONString(0,"邮箱不为空");
        }
        //验证邮箱是否存在
        if (userService.findUserByEmail(email) == null) {
            return ToJSONUtil.getJSONString(0,"邮箱不存在");
        }

        //获取验证码
        String verifyCode = GenerateCodeUtil.generateUUID().substring(0, 4);

        //绑定内容
        Context context = new Context();
        context.setVariable("email",email);
        context.setVariable("verifyCode",verifyCode);
        //发送
        String content = templateEngine.process("/mail/forget", context);
        mailClient.SendMail(email,"找回密码",content);


        // 保存验证码
        session.setAttribute("verifyCode", verifyCode);
        session.setMaxInactiveInterval(300);
        return ToJSONUtil.getJSONString(1);
    }

    /**
     * 重置密码并验证
     * @param email
     * @param password
     * @param verifyCode
     * @param model
     * @param session
     * @return
     */
    @PostMapping(path = "/forget/password")
    public String resetPassword(String email,String password,String verifyCode,Model model,HttpSession session){
        String code = (String) session.getAttribute("verifyCode");
        if(StringUtils.isBlank(code) || StringUtils.isBlank(verifyCode) || !code.equalsIgnoreCase(verifyCode)){
            model.addAttribute("codeMsg","验证码错误");
            return "/site/forget";
        }

        Map<String, Object> map = userService.resetPassword(email, password);
        if(map == null){
            return "redirect:/login";
        }else {
            model.addAttribute("emailMsg",map.get("emailMsg"));
            model.addAttribute("passwordMsg",map.get("passwordMsg"));
            return "/site/forget";
        }
    }

    //权限不足
    @GetMapping(path = "/denied")
    public String getDeniedPage(){
        return "/error/404";
    }
}


