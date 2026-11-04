package com.integrity.common.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 配置：仅装分页插件。
 *
 * <p>maxLimit=500 兜底防止前端传入超大 pageSize 拖垮 DB；
 * overflow=false 表示页码越界时返回空列表而非回到第一页。
 */
@Configuration
public class MybatisPlusConfig {

    /** 分页内部拦截器（MySQL 方言）。 */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        PaginationInnerInterceptor page = new PaginationInnerInterceptor(DbType.MYSQL);
        page.setMaxLimit(500L);
        page.setOverflow(false);
        interceptor.addInnerInterceptor(page);
        return interceptor;
    }
}
