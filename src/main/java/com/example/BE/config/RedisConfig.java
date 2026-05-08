package com.example.BE.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane;

@Configuration
@RequiredArgsConstructor
public class RedisConfig {
    private final RedisTemplate redisTemplate;
    @Bean
    public RedisTemplate<String,Object> redisTempalte(RedisConnectionFactory connectionFactory
    ){
        RedisTemplate<String, Object> template = new RedisTemplate();
        template.setConnectionFactory(connectionFactory);
        template.afterPropertiesSet();

        return  template;

    }

    //test
    @PostConstruct
    public void testRedis(){

        redisTemplate.opsForValue().set("test","hello");
        System.out.println(redisTemplate.opsForValue().get("test"));
    }
}


