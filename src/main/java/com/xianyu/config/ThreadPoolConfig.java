package com.xianyu.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@EnableAsync //使用@EnableAsync来开启异步的支持，使用@Async来对某个方法进行异步执行。
public class ThreadPoolConfig {
}
