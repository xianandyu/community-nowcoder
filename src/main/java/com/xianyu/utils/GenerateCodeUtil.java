package com.xianyu.utils;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.util.UUID;

public class GenerateCodeUtil {

    /**
     * 生成随机字符串
     * 可能生成 ’-‘ ==> ‘’替换
     */
    public static String generateUUID(){
        return UUID.randomUUID().toString().replaceAll("-","");
    }

    // MD5加密
    // hello -> abc123def456
    // hello(password) + 3e4a8(salt) -> abc123def456abc
    public static String md5(String key){
        if(StringUtils.isBlank(key)){
            return null;
        }
        //加密方法
        return DigestUtils.md5DigestAsHex(key.getBytes());
    }
}
