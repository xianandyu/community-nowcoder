package com.xianyu.controller.interceptor;

import com.xianyu.service.MessageService;
import com.xianyu.service.UserService;
import com.xianyu.domain.User;
import com.xianyu.domain.LoginTicket;
import com.xianyu.utils.CookieUtil;
import com.xianyu.utils.HostHolderUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.Date;

/**
 * login拦截器
 */
@Component
public class loginTicketInterceptor implements HandlerInterceptor {

    @Autowired
    private HostHolderUtil hostHolderUtil;

    @Autowired
    private UserService userService;

    @Autowired
    private MessageService messageService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String ticket = CookieUtil.getValue(request, "ticket");
        if(ticket != null){
            // 查询凭证
            LoginTicket loginTicket = userService.findLoginTicketByTicket(ticket);
            // 检查凭证是否有效
            if(loginTicket.getStatus() == 0 && loginTicket != null &&
                    loginTicket.getExpired().after(new Date())){

                // 根据凭证查询用户
                User user = userService.findUserById(loginTicket.getUserId());
                // 在本次请求中持有用户
                hostHolderUtil.setUser(user);
            }
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        User user = hostHolderUtil.getUser();
        if(user != null && modelAndView != null){
            //返回给前端
            //用户
            modelAndView.addObject("loginUser", user);

            //通知消息
            int noticeUnreadCount = messageService.findNoticeUnreadCount(user.getId(), null);
            int letterUnreadCount = messageService.findLetterUnreadCount(user.getId(), null);
            modelAndView.addObject("count",noticeUnreadCount + letterUnreadCount);

            // 构建用户认证的结果,并存入SecurityContext,以便于Security进行授权.
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    user, user.getPassword(), userService.getAuthorities(user.getId()));
            SecurityContextHolder.setContext(new SecurityContextImpl(authentication));

        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        hostHolderUtil.clear();
    }
}
