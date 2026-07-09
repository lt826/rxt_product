package com.qst.crop.init;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qst.crop.dao.UserMapper;
import com.qst.crop.entity.User;
import com.qst.crop.security.SecurityUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 应用启动时初始化默认管理员
 */
@Slf4j
@Component
public class AdminInitializer implements CommandLineRunner {

    @Autowired
    private UserMapper userMapper;

    @Override
    public void run(String... args) {
        initDefaultAdmin();
    }

    /**
     * 初始化默认管理员账号（如果不存在）
     */
    private void initDefaultAdmin() {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("user_name", "admin");
        User existingAdmin = userMapper.selectOne(wrapper);

        if (existingAdmin == null) {
            log.info("默认管理员不存在，开始创建...");

            User admin = new User();
            admin.setUserName("admin");
            // 使用加密后的密码：admin123
            admin.setPassword(SecurityUtil.encryptPassword("admin123"));
            admin.setNickName("系统管理员");
            admin.setRole("admin");
            admin.setIntegral(0);
            admin.setCredit(0);
            admin.setStatus("approved");

            LocalDateTime now = LocalDateTime.now();
            admin.setCreateTime(now);
            admin.setUpdateTime(now);

            int result = userMapper.insert(admin);
            if (result > 0) {
                log.info("========================================");
                log.info("默认管理员创建成功！");
                log.info("用户名: admin");
                log.info("密码: admin123");
                log.info("========================================");
            } else {
                log.error("默认管理员创建失败！");
            }
        } else {
            log.info("默认管理员已存在，跳过初始化");
        }
    }
}
