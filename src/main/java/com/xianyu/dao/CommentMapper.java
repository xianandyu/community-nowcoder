package com.xianyu.dao;

import com.xianyu.domain.Comment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CommentMapper {

    /**
     *
     * @param entityType 评论类型 1==> 回帖 2==>回帖中的评论
     * @param entityId 评论或发帖员的作者
     * @param offset
     * @param limit
     * @return
     */
   List<Comment> selectCommentsByEntity(int entityType, int entityId, int offset, int limit);

   //查找回复的数量
   int selectCountByEntity(int entityType, int entityId);

   int insertComment(Comment comment);

   //查找用户评论
    List<Comment> selectCommentsByUserId(int userId,int offset,int limit);

    //查找用户评论数量
    int selectCountByUser(int userId);

    //查找评论
    Comment selectCommetByEntityId(int id);
}
