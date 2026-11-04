package com.integrity;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 学鉴 · 课程作业 AI 生成内容检测与学术诚信工具（P0 后端）
 *
 * <p>P0 为模块化单体，包结构按微服务边界切分（auth / assignment / detect /
 * evidence / report / appeal / config），便于 P2 拆分为独立服务。
 */
@EnableAsync
@MapperScan("com.integrity.**.mapper")
@SpringBootApplication
public class IntegrityApplication {

    public static void main(String[] args) {
        SpringApplication.run(IntegrityApplication.class, args);
    }
}
