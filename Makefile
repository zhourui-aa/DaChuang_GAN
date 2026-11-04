# 学鉴 P0 · 常用命令
# 注意：本项目必须用 JDK 21 构建（默认 JDK 26 会让 Lombok 失效），故显式指定 JAVA_HOME。

JAVA_HOME ?= /Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home
export JAVA_HOME

MVN := mvn
DB  := mysql -uroot

.PHONY: help run build test clean health db-reset db-shell

help:
	@echo "make run       # 本地启动后端 (http://localhost:8080/doc.html)"
	@echo "make build     # 编译打包"
	@echo "make test      # 跑单测"
	@echo "make health    # 探活"
	@echo "make db-reset  # 删库重建（重跑 schema.sql + data.sql）"
	@echo "make db-shell  # 进入 integrity 库"

run:
	$(MVN) -DskipTests spring-boot:run

build:
	$(MVN) -DskipTests package

test:
	$(MVN) test

clean:
	$(MVN) clean

health:
	@curl -s http://localhost:8080/api/health || echo "服务未启动"

db-reset:
	$(DB) -e "DROP DATABASE IF EXISTS integrity; CREATE DATABASE integrity DEFAULT CHARACTER SET utf8mb4;"
	@echo "已重建 integrity 库，下次启动自动灌 schema + data"

db-shell:
	$(DB) integrity
