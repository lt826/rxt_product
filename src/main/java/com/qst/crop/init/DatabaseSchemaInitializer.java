package com.qst.crop.init;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

/**
 * 应用启动时自检并补齐数据库表结构。
 * <p>
 * 项目历史上靠手动导入 schema.sql 建表，但该脚本含 DROP 语句、不能重复执行，
 * 导致后来新增的表（消息、联合贷款邀请、通知等）在已部署的库里长期缺失，
 * 对应接口运行时报 “Table doesn't exist”。
 * <p>
 * 这里在启动时读取 classpath:schema.sql，将其中的每条 CREATE TABLE 语句
 * 改写为 CREATE TABLE IF NOT EXISTS 后执行：缺失的表会被补建，已存在的表
 * 保持不变（不丢数据）。DROP / USE / CREATE DATABASE 等语句一律跳过。
 * <p>
 * 以最高优先级运行，确保表在其他 CommandLineRunner（如 AdminInitializer）之前就绪。
 */
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@Component
public class DatabaseSchemaInitializer implements CommandLineRunner {

    private static final String SCHEMA_RESOURCE = "schema.sql";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        String sql = readClasspath(SCHEMA_RESOURCE);
        if (sql == null) {
            return;
        }
        int executed = 0;
        int failed = 0;
        for (String raw : sql.split(";")) {
            String stmt = raw.trim();
            if (stmt.isEmpty()) {
                continue;
            }
            // 只处理 CREATE TABLE 语句，其余（DROP / USE / CREATE DATABASE / 注释）跳过
            if (!stmt.toUpperCase().startsWith("CREATE TABLE")) {
                continue;
            }
            String ifNotExists = stmt.replaceFirst("(?i)CREATE\\s+TABLE", "CREATE TABLE IF NOT EXISTS");
            try {
                jdbcTemplate.execute(ifNotExists);
                executed++;
            } catch (Exception e) {
                failed++;
                log.warn("建表语句执行失败，已跳过：{}", e.getMessage());
            }
        }
        log.info("数据库结构自检完成：成功 {} 条，失败 {} 条（缺失表已补建，已存在表保持不变）", executed, failed);
        applyMigrations();
    }

    /**
     * 执行增量迁移：给已存在的表补充字段（如订单物流字段 consignee/consignee_phone/ship_name/ship_phone）。
     * 重复执行安全——列已存在时该条会被捕获跳过。对应脚本：order_logistics_schema.sql
     */
    private void applyMigrations() {
        String[] alters = {
                "ALTER TABLE tb_purchase ADD COLUMN consignee VARCHAR(100) DEFAULT NULL COMMENT '收货人姓名' AFTER address",
                "ALTER TABLE tb_purchase ADD COLUMN consignee_phone VARCHAR(20) DEFAULT NULL COMMENT '收货人电话' AFTER consignee",
                "ALTER TABLE tb_sell_purchase ADD COLUMN ship_name VARCHAR(100) DEFAULT NULL COMMENT '发货人姓名' AFTER address",
                "ALTER TABLE tb_sell_purchase ADD COLUMN ship_phone VARCHAR(20) DEFAULT NULL COMMENT '发货人电话' AFTER ship_name"
        };
        int applied = 0;
        int skipped = 0;
        for (String ddl : alters) {
            try {
                jdbcTemplate.execute(ddl);
                applied++;
            } catch (Exception e) {
                skipped++;
                log.debug("迁移语句跳过（列可能已存在）：{}", e.getMessage());
            }
        }
        log.info("数据库迁移自检完成：应用 {} 条，跳过 {} 条", applied, skipped);
    }

    private String readClasspath(String location) {
        try (Reader reader = new InputStreamReader(
                new ClassPathResource(location).getInputStream(), StandardCharsets.UTF_8)) {
            return FileCopyUtils.copyToString(reader);
        } catch (IOException e) {
            log.warn("无法读取 {}，跳过数据库结构自检：{}", location, e.getMessage());
            return null;
        }
    }
}
