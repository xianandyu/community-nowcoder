package com.xianyu.utils;

import com.xianyu.domain.User;
import org.springframework.stereotype.Component;

/**
 * 持有用户信息,用于代替session对象.(多线程)
 */
@Component
public class HostHolderUtil {

    private ThreadLocal<User> users = new ThreadLocal<>();

    public User getUser() {
        return users.get();
    }

    public void setUser(User user){
        users.set(user);
    }

    public void clear(){
        users.remove();
    }

}
