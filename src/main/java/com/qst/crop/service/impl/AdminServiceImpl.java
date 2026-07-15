package com.qst.crop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qst.crop.dao.ExpertMapper;
import com.qst.crop.dao.FinanceMapper;
import com.qst.crop.dao.PurchaseMapper;
import com.qst.crop.dao.UserMapper;
import com.qst.crop.entity.Expert;
import com.qst.crop.entity.Finance;
import com.qst.crop.entity.User;
import com.qst.crop.security.SecurityUtil;
import com.qst.crop.service.AdminService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ExpertMapper expertMapper;

    @Autowired
    private FinanceMapper financeMapper;

    @Autowired
    private PurchaseMapper purchaseMapper;

    @Override
    public IPage<User> getAllUsers(int page, int size, String keyword, String role) {
        Page<User> pageParam = new Page<>(page, size);
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w.like("user_name", keyword).or().like("nick_name", keyword));
        }
        if (role != null && !role.trim().isEmpty()) {
            wrapper.eq("role", role);
        }
        wrapper.orderByDesc("create_time");
        return userMapper.selectPage(pageParam, wrapper);
    }

    @Override
    public User getUserByUserName(String userName) {
        return userMapper.selectById(userName);
    }

    @Override
    public boolean createUser(User user) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("user_name", user.getUserName());
        if (userMapper.selectOne(wrapper) != null) {
            return false;
        }
        // 加密密码
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(SecurityUtil.encryptPassword(user.getPassword()));
        }
        LocalDateTime now = LocalDateTime.now();
        user.setCreateTime(now);
        user.setUpdateTime(now);
        if (user.getIntegral() == null) user.setIntegral(0);
        if (user.getCredit() == null) user.setCredit(0);
        int result = userMapper.insert(user);
        if (result > 0 && "expert".equalsIgnoreCase(user.getRole())) {
            Expert expert = new Expert();
            expert.setUserName(user.getUserName());
            expert.setRealName(user.getRealName());
            expert.setPhone(user.getPhone());
            expert.setProfession("农业技术");
            expert.setPosition("专家");
            expert.setBelong("融销通平台");
            try {
                expertMapper.insert(expert);
            } catch (Exception e) {
                log.warn("创建专家信息失败（不影响用户创建）: {}", e.getMessage());
            }
        }
        return result > 0;
    }

    @Override
    public boolean updateUser(User user) {
        // 如果密码不为空且不是已加密的密码（BCrypt密文以$2a$或$2b$开头），则进行加密
        if (user.getPassword() != null && !user.getPassword().isEmpty()
                && !user.getPassword().startsWith("$2")) {
            user.setPassword(SecurityUtil.encryptPassword(user.getPassword()));
        }
        user.setUpdateTime(LocalDateTime.now());
        int result = userMapper.updateById(user);
        return result > 0;
    }

    @Override
    public boolean deleteUser(String userName) {
        int result = userMapper.deleteById(userName);
        return result > 0;
    }

    @Override
    public Map<String, Object> getStatsOverview() {
        Map<String, Object> stats = new HashMap<>();
        long totalUsers = userMapper.selectCount(null);
        stats.put("totalUsers", totalUsers);

        List<User> allUsers = userMapper.selectList(null);
        Map<String, Long> roleCounts = new HashMap<>();
        for (User u : allUsers) {
            String role = u.getRole() != null ? u.getRole() : "unknown";
            roleCounts.merge(role, 1L, Long::sum);
        }

        long farmers = roleCounts.getOrDefault("farmer", 0L);
        long buyers = roleCounts.getOrDefault("buyer", 0L);
        long banks = roleCounts.getOrDefault("bank", 0L);
        long experts = roleCounts.getOrDefault("expert", 0L);
        long admins = roleCounts.getOrDefault("admin", 0L);

        stats.put("activeFarmers", farmers);
        stats.put("activeBuyers", buyers);
        stats.put("activeBanks", banks);
        stats.put("activeExperts", experts);
        stats.put("activeAdmins", admins);

        String[][] colorMap = {
            {"farmer", "农户", "#409eff"},
            {"buyer", "买家", "#67c23a"},
            {"bank", "银行", "#e6a23c"},
            {"expert", "专家", "#f56c6c"},
            {"admin", "管理员", "#909399"}
        };
        List<Map<String, Object>> distribution = new ArrayList<>();
        long distributionTotal = farmers + buyers + banks + experts + admins;
        for (String[] mapping : colorMap) {
            long count = roleCounts.getOrDefault(mapping[0], 0L);
            Map<String, Object> item = new HashMap<>();
            item.put("role", mapping[1]);
            item.put("count", count);
            item.put("percent", distributionTotal > 0
                ? BigDecimal.valueOf(count * 100.0 / distributionTotal).setScale(1, RoundingMode.HALF_UP).doubleValue()
                : 0);
            item.put("color", mapping[2]);
            distribution.add(item);
        }
        stats.put("userDistribution", distribution);

        long totalFinances = financeMapper.selectCount(null);
        stats.put("totalFinances", totalFinances);

        QueryWrapper<Finance> approvedQuery = new QueryWrapper<>();
        approvedQuery.eq("status", 2).or().eq("status", "2");
        List<Finance> approvedFinances = financeMapper.selectList(approvedQuery);
        BigDecimal totalFinancingAmount = approvedFinances.stream()
            .map(f -> f.getMoney() != null ? f.getMoney() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.put("totalFinancing", totalFinancingAmount);

        long approved = approvedFinances.size();
        double approvalRate = totalFinances > 0
            ? BigDecimal.valueOf(approved * 100.0 / totalFinances).setScale(1, RoundingMode.HALF_UP).doubleValue()
            : 0;
        stats.put("approvalRate", approvalRate);

        QueryWrapper<Finance> allFinanceQuery = new QueryWrapper<>();
        allFinanceQuery.orderByAsc("create_time");
        List<Finance> allFinances = financeMapper.selectList(allFinanceQuery);

        Map<String, List<Finance>> monthlyGroup = allFinances.stream()
            .collect(Collectors.groupingBy(
                f -> f.getCreateTime() != null
                    ? f.getCreateTime().toLocalDate().withDayOfMonth(1).toString().substring(0, 7)
                    : "unknown",
                LinkedHashMap::new,
                Collectors.toList()
            ));

        List<Map<String, Object>> monthlyTrend = new ArrayList<>();
        for (Map.Entry<String, List<Finance>> entry : monthlyGroup.entrySet()) {
            List<Finance> monthFinances = entry.getValue();
            long monthTotal = monthFinances.size();
            long monthApproved = monthFinances.stream()
                .filter(f -> f.getStatus() != null && (f.getStatus() == 2 || f.getStatus().toString().equals("2")))
                .count();
            BigDecimal monthAmount = monthFinances.stream()
                .filter(f -> f.getStatus() != null && (f.getStatus() == 2 || f.getStatus().toString().equals("2")))
                .map(f -> f.getMoney() != null ? f.getMoney() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            Map<String, Object> trendItem = new HashMap<>();
            trendItem.put("month", entry.getKey());
            trendItem.put("applications", monthTotal);
            trendItem.put("approved", monthApproved);
            trendItem.put("totalAmount", monthAmount.divide(BigDecimal.valueOf(10000), 2, RoundingMode.HALF_UP));
            monthlyTrend.add(trendItem);
        }
        stats.put("monthlyTrend", monthlyTrend);

        long totalOrders = purchaseMapper.selectCount(null);
        stats.put("totalOrders", totalOrders);

        return stats;
    }

    @Override
    public List<User> getPendingUsers() {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("status", "pending");
        wrapper.orderByDesc("create_time");
        return userMapper.selectList(wrapper);
    }

    @Override
    public boolean approveUser(String userName, boolean approved) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("user_name", userName);
        User user = userMapper.selectOne(wrapper);
        if (user == null) {
            return false;
        }

        user.setStatus(approved ? "approved" : "rejected");
        user.setUpdateTime(LocalDateTime.now());

        // 如果审批通过且是专家角色，创建专家信息
        if (approved && "expert".equalsIgnoreCase(user.getRole())) {
            QueryWrapper<Expert> expertWrapper = new QueryWrapper<>();
            expertWrapper.eq("user_name", userName);
            if (expertMapper.selectCount(expertWrapper) == 0) {
                Expert expert = new Expert();
                expert.setUserName(user.getUserName());
                expert.setRealName(user.getRealName());
                expert.setPhone(user.getPhone());
                expert.setProfession("农业技术");
                expert.setPosition("专家");
                expert.setBelong("融销通平台");
                try {
                    expertMapper.insert(expert);
                } catch (Exception e) {
                    log.warn("创建专家信息失败: {}", e.getMessage());
                }
            }
        }

        int result = userMapper.updateById(user);
        return result > 0;
    }
}