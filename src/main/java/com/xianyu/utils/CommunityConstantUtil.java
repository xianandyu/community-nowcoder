package com.xianyu.utils;


public interface CommunityConstantUtil {

    //激活成功
    int ACTIVATION_SUCCESS = 0;

    //激活失败
    int ACTIVATION_ERROR = 1;

    //重复激活
    int ACTIVATION_REPEAT = 2;

    //”记住我“时间:TRUE==>记住我 FALSE==>不记住
    int REMEMBER_FALSE = 3600 * 6;
    int REMEMBER_TRUE = 3600 * 24 * 12;

    //忘记密码邮箱验证码生成类型
    int EMAIL_NULL = 0;
    int EMAIL_NO_EXIST = 1;
    int EMAIL_SUCCESS = 2;

    /**
     * 实体类型: 帖子
     */
    int ENTITY_TYPE_POST = 1;

    /**
     * 实体类型: 评论
     */
    int ENTITY_TYPE_COMMENT = 2;

    /**
     * 实体类型: 用户
     */
    int ENTITY_TYPE_USER = 3;

    /**
     * 主题: 评论,关注,点赞,发帖,删帖.分享
     */
    String TOPIC_COMMENT = "COMMENT";
    String TOPIC_FOLLOW = "FOLLOW";
    String TOPIC_LIKE = "LIKE";
    String TOPIC_PUBLIC = "PUBLIC";
    String TOPIC_DELETE = "DELETE";
    String TOPIC_SHARE = "share";
    /**
     * 系统用户ID
     */
    int SYSTEM_USER_ID = 1;

    /**
     * 权限
     * 用户
     * 版主
     * 管理员
     */
    String AUTHORITY_USER = "user";
    String AUTHORITY_ADMIN = "admin";
    String AUTHORITY_MODERATOR = "moderator";

}
