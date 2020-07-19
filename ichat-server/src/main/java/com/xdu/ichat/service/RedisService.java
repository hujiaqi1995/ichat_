package com.xdu.ichat.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author hujiaqi
 * @create 2020/6/26
 */
@Service
public class RedisService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static final long EXPIRE_TIME = 3 * 60 * 1000L;

    public String getString(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public void setString(String key, String val) {
        redisTemplate.opsForValue().set(key, val, EXPIRE_TIME, TimeUnit.MILLISECONDS);
    }

    public String getHash(String key, String hKey) {
        return (String) redisTemplate.opsForHash().get(key, hKey);
    }

    public List<Object> getHashs(String key, List<Object> hKeys) {
        return redisTemplate.opsForHash().multiGet(key, hKeys);
    }

    public void setHash(String key, String hkey, String val) {
        redisTemplate.opsForHash().put(key, hkey, val);
    }

    public void incrementValue(String key, long val) {
        redisTemplate.opsForValue().increment(key, val);
    }

    public void incrementHash(String key, String hKey, long val) {
        redisTemplate.opsForHash().increment(key, hKey, val);
    }

    public void deleteHashKey(String key, String hKey) {
        redisTemplate.opsForHash().delete(key, hKey);
    }

    public void convertAndSend(String channel, Object message) {
        redisTemplate.convertAndSend(channel, message);
    }

    public void deleteKey(String key) {
        redisTemplate.delete(key);
    }
}
