package com.xianyu.service;

import com.xianyu.dao.CommentMapper;
import com.xianyu.dao.DiscussPostMapper;
import com.xianyu.domain.Comment;
import com.xianyu.utils.CommunityConstantUtil;
import com.xianyu.utils.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class CommentService {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    //查找回复
    public List<Comment> findCommentsByEntityId(int entityType, int entityId, int offset, int limit){
        return commentMapper.selectCommentsByEntity(entityType,entityId,offset,limit);
    }

    public int findCommentCount(int entityType,int entityId){
        return commentMapper.selectCountByEntity(entityType,entityId);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED,propagation = Propagation.REQUIRED)
    public void  addComment(Comment comment){
        if(comment == null){
            throw new IllegalArgumentException("评论参数不为空");
        }

        // 添加评论
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
        comment.setContent(sensitiveFilter.filter(comment.getContent()));
        commentMapper.insertComment(comment);

        if(comment.getEntityType() == CommunityConstantUtil.ENTITY_TYPE_POST){
            int count = commentMapper.selectCountByEntity(comment.getEntityType(), comment.getEntityId());
            discussPostMapper.updateDiscussCount(comment.getEntityId(),count);
        }
    }

    public List<Comment> findCommentsByUserId(int userId, int offset,int limit){
        return commentMapper.selectCommentsByUserId(userId,offset,limit);
    }

    public int findCountByUser(int userId){
        return commentMapper.selectCountByUser(userId);
    }

    public Comment findCommentByEntityId(int id){
        return commentMapper.selectCommetByEntityId(id);
    }
}
