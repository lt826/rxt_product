package com.qst.crop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qst.crop.dao.FinanceMapper;
import com.qst.crop.dao.PurchaseMapper;
import com.qst.crop.dao.UserMapper;
import com.qst.crop.entity.Finance;
import com.qst.crop.entity.User;
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
    private FinanceMapper financeMapper;

    @Autowired
    private PurchaseMapper purchaseMapper;

    @Override
    public List<User> getAllUsers() {
        return userMapper.selectList(null);
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
        LocalDateTime now = LocalDateTime.now();
        user.setCreateTime(now);
        user.setUpdateTime(now);
        if (user.getIntegral() == null) user.setIntegral(0);
        if (user.getCredit() == null) user.setCredit(0);
        int result = userMapper.insert(user);
        return result > 0;
    }

    @Override
    public boolean updateUser(User user) {
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

        // 用户总数
        long totalUsers = userMapper.selectCount(null);
        stats.put("totalUsers", totalUsers);

        // 各角色用户数
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

        // 用户分布数据（前端图表用）
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

        // 融资数据
        long totalFinances = financeMapper.selectCount(null);
        stats.put("totalFinances", totalFinances);

        // 已通过融资金额（总额）
        QueryWrapper<Finance> approvedQuery = new QueryWrapper<>();
        approvedQuery.eq("status", 2).or().eq("status", "2");
        List<Finance> approvedFinances = financeMapper.selectList(approvedQuery);
        BigDecimal totalFinancingAmount = approvedFinances.stream()
            .map(f -> f.getMoney() != null ? f.getMoney() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.put("totalFinancing", totalFinancingAmount);

        // 通过率
        long approved = approvedFinances.size();
        double approvalRate = totalFinances > 0
            ? BigDecimal.valueOf(approved * 100.0 / totalFinances).setScale(1, RoundingMode.HALF_UP).doubleValue()
            : 0;
        stats.put("approvalRate", approvalRate);

        // 月度融资趋势
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

        // 订单总数
        long totalOrders = purchaseMapper.selectCount(null);
        stats.put("totalOrders", totalOrders);

        return stats;
    }
}
