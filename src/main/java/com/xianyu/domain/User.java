package com.xianyu.domain;

import lombok.Data;

import java.util.Date;

@Data
public class User {

    private int id;
    private String username;
    private String password;
    private String email;
    //加密的字符串
    private String salt;
    private int type;
    private int status;
    private Date createTime;
    private String headerUrl;
    private String activationCode;
}
