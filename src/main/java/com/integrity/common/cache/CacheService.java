package com.integrity.common.cache;

import java.time.Duration;
import java.util.function.Supplier;

/**
 * 缓存抽象层（P0 用进程内实现替代 Redis 的关键解耦点）。
 *
 * <p>手册用 Redis 承载任务状态 / 结果摘要 / 证据缓存 / 去重 / 健康探针 / 限流计数，
 * 但这些本质是「多实例共享状态」的需求；P0 是单实例模块化单体，
 * 因此用 {@link InMemoryCacheService} 即可，接口保持不变，
 * 待 P1/P2 引入 Redis 时只新增实现类、改注入，业务代码零改动。
 */
public interface CacheService {

    /** 读取并转换为指定类型，未命中返回 null。 */
    <T> T get(String key, Class<T> type);

    /** 写入并指定 TTL（进程内实现忽略逐条 TTL，统一走全局过期）。 */
    void put(String key, Object value, Duration ttl);

    /** 写入（使用默认 TTL）。 */
    void put(String key, Object value);

    /** 删除单个 key。 */
    void evict(String key);

    /** 清空全部。 */
    void clear();

    /** 读缓存，未命中则用 loader 加载并回填（防击穿）。 */
    <T> T getOrLoad(String key, Duration ttl, Supplier<T> loader);
}
