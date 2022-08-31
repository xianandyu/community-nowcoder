package com.xianyu.dao;

import com.xianyu.domain.LoginTicket;
import org.apache.ibatis.annotations.*;

@Mapper
@Deprecated
public interface LoginTicketMapper {

    @Insert("insert into login_ticket (user_id, ticket, status,expired) " +
            "value (#{userId},#{ticket},#{status},#{expired})")
    @Options(useGeneratedKeys = true,keyProperty = "id")
    int insertLoginTicket(LoginTicket loginTicker);

    @Select("select id, user_id, ticket,status, expired from " +
            "login_ticket where ticket = #{ticket}")
    LoginTicket selectByTicket(String ticket);

    @Update("update login_ticket set status = #{status} where ticket = #{ticket}")
    int updateStatus(String ticket, int status);

    @Update("update login_ticket set status = #{status},ticket = #{ticket}, expired = #{expired}" +
            " where user_id = #{userId}")
    int update(LoginTicket loginTicket);

    @Select("select id, user_id, ticket,status, expired from " +
            "login_ticket where user_id = #{userId}")
    LoginTicket selectByUserId(int userId);
}
