package com.qst.crop.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qst.crop.entity.User;
import java.util.List;
import java.util.Map;

public interface AdminService {
    IPage<User> getAllUsers(int page, int size, String keyword, String role);
    User getUserByUserName(String userName);
    boolean createUser(User user);
    boolean updateUser(User user);
    boolean deleteUser(String userName);
    Map<String, Object> getStatsOverview();

    /**
     * 获取待审批用户列表
     */
    List<User> getPendingUsers();

    /**
     * 审批用户
     * @param userName 用户名
     * @param approved true-通过，false-拒绝
     * @return 是否成功
     */
    boolean approveUser(String userName, boolean approved);
}
