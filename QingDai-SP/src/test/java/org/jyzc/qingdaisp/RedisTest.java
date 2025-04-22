package org.jyzc.qingdaisp;

import com.qingdai.QingDaiSpApplication;
import com.qingdai.entity.Photo;
import com.qingdai.entity.User;
import com.qingdai.service.PhotoService;
import com.qingdai.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;

@SpringBootTest(classes = QingDaiSpApplication.class)
public class RedisTest {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private PhotoService photoService;
    
    @Autowired
    private CacheTestService cacheTestService;

    @Test
    public void testRedisConnection() {
        // 测试Redis连接
        stringRedisTemplate.opsForValue().set("test:key", "HelloRedis");
        String value = stringRedisTemplate.opsForValue().get("test:key");
        System.out.println("Redis读写测试: " + value);
        
        // 查看当前所有键
        Set<String> keys = stringRedisTemplate.keys("*");
        System.out.println("当前Redis中的键: " + keys);
    }
    
    @Test
    public void testCaching() {
        // 第一次调用，应该从数据库获取并缓存
        System.out.println("首次调用getUserById方法:");
        User user = cacheTestService.getUserById("1");
        System.out.println("用户名: " + (user != null ? user.getUsername() : "未找到"));
        
        // 第二次调用，应该从缓存获取
        System.out.println("\n再次调用getUserById方法:");
        user = cacheTestService.getUserById("1");
        System.out.println("用户名: " + (user != null ? user.getUsername() : "未找到"));
        
        // 查看缓存键
        Set<String> keys = stringRedisTemplate.keys("CACHE_user::*");
        System.out.println("\n缓存中的用户相关键: " + keys);
        
        // 测试缓存清除
        System.out.println("\n清除缓存后再调用:");
        cacheTestService.clearUserCache("1");
        user = cacheTestService.getUserById("1");
        System.out.println("用户名: " + (user != null ? user.getUsername() : "未找到"));
    }
    
    @Test
    public void testPhotoCaching() {
        // 获取一个存在的照片ID进行测试
        String photoId = "1"; // 请替换为实际存在的照片ID
        
        // 第一次调用
        System.out.println("首次调用getPhotoById方法:");
        Photo photo = cacheTestService.getPhotoById(photoId);
        System.out.println("照片标题: " + (photo != null ? photo.getTitle() : "未找到"));
        
        // 第二次调用
        System.out.println("\n再次调用getPhotoById方法:");
        photo = cacheTestService.getPhotoById(photoId);
        System.out.println("照片标题: " + (photo != null ? photo.getTitle() : "未找到"));
        
        // 查看缓存键
        Set<String> keys = stringRedisTemplate.keys("CACHE_photo::*");
        System.out.println("\n缓存中的照片相关键: " + keys);
    }
}

/**
 * 用于测试缓存的服务
 */
@Component
class CacheTestService {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private PhotoService photoService;
    
    @Cacheable(value = "user", key = "#id")
    public User getUserById(String id) {
        System.out.println("从数据库获取用户，ID: " + id);
        return userService.getById(id);
    }
    
    @CacheEvict(value = "user", key = "#id")
    public void clearUserCache(String id) {
        System.out.println("清除用户缓存，ID: " + id);
    }
    
    @Cacheable(value = "photo", key = "#id")
    public Photo getPhotoById(String id) {
        System.out.println("从数据库获取照片，ID: " + id);
        return photoService.getById(id);
    }
} 