package com.xianyu;

import com.xianyu.utils.MailClientUtil;
import com.xianyu.utils.SensitiveFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

@SpringBootTest
public class MailTest {
    @Autowired
    private MailClientUtil mailClient;

    @Test
    public void Test() {
        mailClient.SendMail("1561908809@qq.com", "a", "b");
    }


    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Test
    public void Test02() {
        String text = "嫖娼，赌博，喝酒，抽烟喝酒，黄片";
        text = sensitiveFilter.filter(text);
        System.out.println(text);

        text = "嫖3#娼#，赌##博，喝!!酒!，抽!烟！喝！酒，黄片！！！";
        text = sensitiveFilter.filter(text);
        System.out.println(text);

        List<Integer> integers = Arrays.asList(new Integer[]{1});
        System.out.println(integers.size());

    }
}
