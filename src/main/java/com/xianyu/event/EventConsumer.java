package com.xianyu.event;

import com.alibaba.fastjson.JSONObject;
import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import com.xianyu.domain.DiscussPost;
import com.xianyu.domain.Event;
import com.xianyu.domain.Message;
import com.xianyu.service.DiscussPostService;
import com.xianyu.service.ElasticsearchService;
import com.xianyu.service.MessageService;
import com.xianyu.utils.CommunityConstantUtil;
import com.xianyu.utils.ToJSONUtil;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;


@Component
public class EventConsumer implements CommunityConstantUtil {

    private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);

    @Value("${wk.image.command}")
    private String wkImageCommand;

    @Value("${wk.image.storage}")
    private String wkImageStorage;

    @Value("${qiniu.key.access}")
    private String accessKey;

    @Value("${qiniu.key.secret}")
    private String secretKey;

    @Value("${qiniu.bucket.share.name}")
    private String shareBucketName;

    @Autowired
    private MessageService messageService;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private ElasticsearchService elasticsearchService;

    @Autowired
    private ThreadPoolTaskScheduler taskScheduler;

    @KafkaListener(topics = {TOPIC_COMMENT, TOPIC_FOLLOW, TOPIC_LIKE})
    public void handleCommentMessage(ConsumerRecord record) {
        if (record == null || record.value() == null) {
            logger.error("?????????????????????");
            return;
        }

        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            logger.error("??????????????????");
            return;
        }

        //????????????
        Message message = new Message();
        message.setFromId(SYSTEM_USER_ID);
        message.setToId(event.getEntityUserId());
        message.setConversationId(event.getTopic());
        message.setCreateTime(new Date());

        Map<String, Object> content = new HashMap<>();
        content.put("userId", event.getUserId());
        content.put("entityType", event.getEntityType());
        content.put("entityId", event.getEntityId());
        if (!event.getData().isEmpty()) {
            for (Map.Entry<String, Object> entry : event.getData().entrySet()) {
                content.put(entry.getKey(), entry.getValue());
            }
        }

        message.setContent(JSONObject.toJSONString(content));
        messageService.addLetter(message);

    }

    @KafkaListener(topics = {TOPIC_PUBLIC})
    public void handlePublishMessage(ConsumerRecord record) {
        if (record == null || record.value() == null) {
            logger.error("?????????????????????");
            return;
        }

        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            logger.error("??????????????????");
            return;
        }

        DiscussPost discuss = discussPostService.findDiscussById(event.getEntityId());
        elasticsearchService.saveDiscussPost(discuss);
    }

    @KafkaListener(topics = {TOPIC_DELETE})
    public void handleDeleteMessage(ConsumerRecord record) {
        if (record == null || record.value() == null) {
            logger.error("?????????????????????");
            return;
        }

        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            logger.error("??????????????????");
            return;
        }

        elasticsearchService.deleteDiscussPost(event.getEntityId());
    }

    @KafkaListener(topics = {TOPIC_SHARE})
    public void handleShareMessage(ConsumerRecord record) {
        if (record == null || record.value() == null) {
            logger.error("?????????????????????");
            return;
        }

        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            logger.error("??????????????????");
            return;
        }

        String htmlUrl = (String) event.getData().get("htmlUrl");
        String fileName = (String) event.getData().get("fileName");
        String suffix = (String) event.getData().get("suffix");

        //wkImageCommand=>?????? --quality=>75 ???????????? htmlUrl=>?????? wkImageStorage=>????????????
        String cmd = wkImageCommand + " --quality 75 "
                + htmlUrl + " " + wkImageStorage + "/" + fileName + suffix;

        try {
            Runtime.getRuntime().exec(cmd);
            logger.info("??????????????????:" + cmd);
        } catch (IOException e) {
            logger.error("??????????????????:" + e.getMessage());
        }

        // ???????????????,???????????????,???????????????,?????????????????????.
        UploadTask task = new UploadTask(fileName, suffix);
        Future future = taskScheduler.scheduleAtFixedRate(task, 500);
        task.setFuture(future);
    }

    class UploadTask implements Runnable {
        //????????????
        private String fileName;
        //????????????
        private String suffix;
        //????????????????????????
        private Future future;
        // ????????????
        private long startTime;
        // ????????????
        private int uploadTimes;

        public UploadTask(String fileName, String suffix) {
            this.fileName = fileName;
            this.suffix = suffix;
            this.startTime = System.currentTimeMillis();
        }

        public void setFuture(Future future) {
            this.future = future;
        }

        @Override
        public void run() {
            // ????????????
            if (System.currentTimeMillis() - startTime >= 30000) {
                logger.error("??????????????????,????????????:" + fileName);
                future.cancel(true);
                return;
            }
            if (uploadTimes >= 3) {
                logger.error("??????????????????,????????????:" + fileName);
                future.cancel(true);
                return;
            }

            String path = wkImageStorage + "/" + fileName + suffix;
            File file = new File(path);
            if(file.exists()){
                logger.info(String.format("?????????[%d]?????????:%s",++uploadTimes,fileName));
                // ??????????????????
                StringMap policy = new StringMap();
                policy.put("returnBady", ToJSONUtil.getJSONString(0));
                // ??????????????????
                Auth auth = Auth.create(accessKey, secretKey);
                String uploadToken = auth.uploadToken(shareBucketName, fileName, 3600, policy);
                // ??????????????????
                UploadManager manager = new UploadManager(new Configuration(Zone.zone2()));

                try {
                    // ??????????????????
                    Response response = manager.put(
                            path, fileName, uploadToken, null, "image/" + suffix, false);
                    //??????????????????
                    JSONObject jsonObject = JSONObject.parseObject(response.bodyString());
                    if(jsonObject == null && jsonObject.get("code") == null &&  !jsonObject.get("code").toString().equals("0")){
                        logger.info(String.format("???%d???????????????[%s].", uploadTimes, fileName));
                    }else {
                        logger.info(String.format("???%d???????????????[%s].", uploadTimes, fileName));
                        future.cancel(true);
                    }
                } catch (QiniuException e) {
                    logger.info(String.format("???%d???????????????[%s].", uploadTimes, fileName));
                }
            }else {
                logger.info(String.format("????????????%s??????",fileName));
            }

        }
    }
}
