package com.xianyu.controller.interceptor;

import com.xianyu.domain.User;
import com.xianyu.service.DataService;
import com.xianyu.utils.HostHolderUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class DataInterceptor implements HandlerInterceptor {

    @Autowired
    private HostHolderUtil hostHolderUtil;

    @Autowired
    private DataService dataService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //uv
        String ip = request.getRemoteHost();
        dataService.recordUV(ip);

        //dvu
        User user = hostHolderUtil.getUser();
        if(user != null){
            dataService.recordDAU(user.getId());
        }

        return true;
    }
}
