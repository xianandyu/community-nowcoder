package com.xianyu;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

@SpringBootTest
public class RedisTest {

    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    public void Test01() {
//        String redisKey = "test:count";
//        redisTemplate.opsForValue().set(redisKey,1);
//
//        System.out.println(redisTemplate.opsForValue().get(redisKey));
//        System.out.println(redisTemplate.opsForValue().increment(redisKey));
//        System.out.println(redisTemplate.opsForValue().decrement(redisKey));
        System.out.println(1 ^ 0);
        System.out.println(0 ^ 0);
        System.out.println(1 ^ 1);
        System.out.println(0 ^ 1);

    }
}
