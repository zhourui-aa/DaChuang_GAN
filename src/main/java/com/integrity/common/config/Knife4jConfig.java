package com.integrity.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 接口文档配置（Knife4j / OpenAPI 3）。
 *
 * <p>访问地址：http://localhost:8080/doc.html
 */
@Configuration
public class Knife4jConfig {

    /** 文档元信息。 */
    @Bean
    public OpenAPI integrityOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("学鉴 · 课程作业 AI 内容检测 API")
                .description("P0 后端接口。只输出特征证据与置信度，不输出定性结论。")
                .version("0.1.0"));
    }
}
