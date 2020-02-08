package cn.madtiger.shared.lock.redis.configuration;

import cn.madtiger.shared.lock.redis.ISharedLock;
import cn.madtiger.shared.lock.redis.SpinSetLockArgs;
import cn.madtiger.shared.lock.redis.LockRedisClient;
import cn.madtiger.shared.lock.redis.RedisLockService;
import cn.madtiger.shared.lock.redis.SpringRedisLockClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 自动装载
 * @author Fenghu.Shi
 * @version 1.0
 */
@Configuration
public class SharedLockRedisConfiguration implements ISharedLockConfiguration {

  /**
   * 初始化 redis template bean
   * @param redisTemplate
   * @return
   */
  @Bean
  public SpringRedisLockClient defaultLockClient(ObjectProvider<RedisTemplate> redisTemplate){
    return new SpringRedisLockClient(redisTemplate.getIfAvailable());
  }

  /**
   * 配置 共享锁
   * @param lockRedisClient
   * @return
   */
  @Bean
  public ISharedLock<SpinSetLockArgs> defaultSharedLock(ObjectProvider<LockRedisClient> lockRedisClient){
    return new RedisLockService(lockRedisClient.getIfAvailable());
  }

  /**
   * 创建 共享锁 拦截器
   * @param context
   * @return
   */
  @Bean
  public SharedLockInterceptor annotationSharedLoadInterceptor(ApplicationContext context){
    return new SharedLockInterceptor(context);
  }
}