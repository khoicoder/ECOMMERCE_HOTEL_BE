package com.example.BE.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;


import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

//critical infrastructure
//Khoi1@
@Configuration

public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(
            RedisConnectionFactory connectionFactory
    ) {

        RedisTemplate<String, Object> template =
                new RedisTemplate<>();

        template.setConnectionFactory(connectionFactory);

        template.setKeySerializer(
                new StringRedisSerializer()
        );

        template.setHashKeySerializer(
                new StringRedisSerializer()
        );

//        template.setValueSerializer(
//                new GenericJacksonJsonRedisSerializer()
//        );
//
//        template.setHashValueSerializer(
//                new
//        );

        template.afterPropertiesSet();

        return template;
    }

}


