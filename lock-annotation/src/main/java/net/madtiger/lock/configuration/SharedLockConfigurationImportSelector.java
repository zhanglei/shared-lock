package net.madtiger.lock.configuration;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;

/**
 * 共享锁 配置导入类
 * @author Fenghu.Shi
 * @version 1.0
 */
@Slf4j
public class SharedLockConfigurationImportSelector implements ImportSelector {

  private static final Object lockObject = new Object();

  private static final AtomicBoolean isInit = new AtomicBoolean(false);

  @Override
  public String[] selectImports(AnnotationMetadata importingClassMetadata) {
    synchronized (lockObject) {
      if (isInit.get()) {
        log.error("SharedLock 重复加载，本次忽略");
        return new String[]{};
      }
      // 逐个加载
      for (Map.Entry<String[], String> classes : CONFIGURATION_CLASSES.entrySet()) {
        try {
          // 如果含有则初始化对应的 configuration
          for (String clazz : classes.getKey()) {
            Class.forName(clazz, false, getClass().getClassLoader());
          }
          log.debug("共享锁 {} 客户端加载成功", classes.getValue());
          isInit.set(true);
          return new String[] {classes.getValue()};
        } catch (Throwable ex) {
          log.debug("共享锁 {} 客户端装载失败，没有引用相关依赖包", classes.getValue());
        }
      }

      return new String[0];
    }
  }

  /**
   * 为了保证顺序，这里使用 linked hash map
   */
  private static final Map<String[], String> CONFIGURATION_CLASSES = new LinkedHashMap<>();

  static {
    // 注册 redis 配置
    CONFIGURATION_CLASSES.put(new String[] {"net.madtiger.lock.redis.RedisLockProvider", "org.springframework.data.redis.core.RedisTemplate"}, "net.madtiger.lock.configuration.SharedLockRedisConfiguration");
    // 优先  curator , 这里 curator 还有点问题等待处理再发布
    CONFIGURATION_CLASSES.put(new String[] {"net.madtiger.lock.zk.ZookeeperLockProvider", "org.apache.curator.framework.CuratorFramework"}, "net.madtiger.lock.configuration.SharedLockCuratorConfiguration");
  }
}
