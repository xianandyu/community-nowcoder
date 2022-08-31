package com.xianyu.dao;

import com.xianyu.domain.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface DiscussPostMapper {

    public List<DiscussPost> selectDiscussPosts(int userId, int offset, int limit,int orderMode);

    //@Param用于取别名
    //当使用<if>必须使用别名
    public int selectDiscussPostRows(@Param("userId") int userId);

    //添加帖子
    public int insertDiscussPost(DiscussPost discussPost);

    public DiscussPost selectDiscussById(int id);

    @Update("update discuss_post set comment_count = #{commentCount} where id = #{id}")
    public int updateDiscussCount(int id, int commentCount);

    @Update("update discuss_post set type = #{type} where id = #{id}")
    public int updateDiscussType(int id,int type);

    @Update("update discuss_post set status = #{status} where id = #{id}")
    public int updateDiscussStatus(int id,int status);

    @Update("update discuss_post set score = #{score} where id = #{id}")
    public int updateDiscussScore(int id,double score);
}
