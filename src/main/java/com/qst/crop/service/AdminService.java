package com.qst.crop.service;

import com.qst.crop.entity.User;
import java.util.List;
import java.util.Map;

public interface AdminService {
    List<User> getAllUsers();
    User getUserByUserName(String userName);
    boolean createUser(User user);
    boolean updateUser(User user);
    boolean deleteUser(String userName);
    Map<String, Object> getStatsOverview();
}
