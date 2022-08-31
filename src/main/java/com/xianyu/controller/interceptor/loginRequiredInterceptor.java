package com.xianyu.controller.interceptor;

import com.xianyu.annotation.LoginRequired;
import com.xianyu.utils.HostHolderUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

@Component
public class loginRequiredInterceptor implements HandlerInterceptor {

    @Autowired
    private HostHolderUtil hostHolderUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        if(handler instanceof HandlerMethod){

            HandlerMethod handlerMethod = (HandlerMethod) handler;
            //获取拦截的对象
            Method method = handlerMethod.getMethod();
            //获取拦截器
            LoginRequired loginRequired = method.getAnnotation(LoginRequired.class);
            //判断拦截器是否为空 空==>不需要拦截，跳过
            //判断用户是否为登录 空==>没登陆，拦截
            if(loginRequired != null && hostHolderUtil.getUser() == null){
                String contextPath = request.getContextPath();//"/community"
                response.sendRedirect(contextPath + "/login");
                return false;
            }
        }
        return true;
    }
}
