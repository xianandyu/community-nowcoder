package com.xianyu.quartz;

import com.xianyu.domain.DiscussPost;
import com.xianyu.service.DiscussPostService;
import com.xianyu.service.ElasticsearchService;
import com.xianyu.service.LikeService;
import com.xianyu.utils.CommunityConstantUtil;
import com.xianyu.utils.RedisKeyUtil;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PostScoreRefreshJob implements CommunityConstantUtil, Job {

    private Logger logger = LoggerFactory.getLogger(PostScoreRefreshJob.class);

    //牛客纪元
    private static final Date epoch;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private ElasticsearchService elasticsearchService;

    static {
        try {
            epoch = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2014-08-01 00:00:00");
        } catch (ParseException e) {
            throw new RuntimeException("初始化牛客纪元失败!", e);
        }
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        String redisKey = RedisKeyUtil.getPostScoreKey();
        BoundSetOperations operations = redisTemplate.boundSetOps(redisKey);

        if (operations == null) {
            logger.info("[任务取消] 没有需要刷新的帖子!");
            return;
        }

        logger.info("[任务开始] 需要刷新的帖子数量:" + operations.size());
        while (operations.size() > 0){
            this.refresh((Integer) operations.pop());
        }
        logger.info("[任务结束] 需要刷新的帖子完毕");

    }

    private void refresh(int postId) {
        DiscussPost post = discussPostService.findDiscussById(postId);

        if (post == null) {
            logger.info("需要刷新帖子不在");
            return;
        }

        //是否精华
        boolean wonderful = post.getStatus() == 1;
        //评论数量
        int commentCount = post.getCommentCount();
        //点赞数量
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, postId);

        //计算权重
        double w = ((wonderful) ? 75 : 0) + commentCount * 10 + likeCount * 2;
        double score = Math.log10(Math.max(w, 1)) +
                (post.getCreateTime().getTime() - epoch.getTime()) / (1000 * 3600 * 24);

        //更新帖子分数
        discussPostService.updateDiscussScore(postId,score);

        // 同步搜索数据
        post.setScore(score);
        elasticsearchService.saveDiscussPost(post);
    }
}
