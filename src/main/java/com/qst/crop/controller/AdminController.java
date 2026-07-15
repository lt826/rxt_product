package com.qst.crop.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qst.crop.common.Result;
import com.qst.crop.entity.User;
import com.qst.crop.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    /**
     * 获取用户列表（支持分页和搜索）
     * @param page 页码，默认1
     * @param size 每页数量，默认10
     * @param keyword 关键词（用户名/昵称）
     * @param role 角色过滤
     */
    @GetMapping("/users")
    public Result<IPage<User>> getAllUsers(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role) {
        IPage<User> userPage = adminService.getAllUsers(page, size, keyword, role);
        return Result.success("查询成功", userPage);
    }

    @GetMapping("/users/{userName}")
    public Result<User> getUserDetail(@PathVariable String userName) {
        User user = adminService.getUserByUserName(userName);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }
        return Result.success("查询成功", user);
    }

    @PostMapping("/users")
    public Result<String> createUser(@RequestBody User user) {
        boolean success = adminService.createUser(user);
        if (success) {
            return Result.success("创建用户成功", null);
        }
        return Result.error(400, "用户名已存在");
    }

    @PutMapping("/users/{userName}")
    public Result<String> updateUser(@PathVariable String userName, @RequestBody User user) {
        user.setUserName(userName);
        boolean success = adminService.updateUser(user);
        if (success) {
            return Result.success("更新成功", null);
        }
        return Result.error(404, "用户不存在");
    }

    @DeleteMapping("/users/{userName}")
    public Result<String> deleteUser(@PathVariable String userName) {
        boolean success = adminService.deleteUser(userName);
        if (success) {
            return Result.success("删除成功", null);
        }
        return Result.error(404, "用户不存在");
    }

    @GetMapping("/stats/overview")
    public Result<Map<String, Object>> getStatsOverview() {
        Map<String, Object> stats = adminService.getStatsOverview();
        return Result.success("查询成功", stats);
    }

    /**
     * 获取待审批用户列表
     */
    @GetMapping("/users/pending")
    public Result<List<User>> getPendingUsers() {
        List<User> list = adminService.getPendingUsers();
        return Result.success("查询成功", list);
    }

    /**
     * 审批用户
     * @param userName 用户名
     * @param approved true-通过，false-拒绝
     */
    @PostMapping("/users/{userName}/approve")
    public Result<String> approveUser(@PathVariable String userName, @RequestParam boolean approved) {
        boolean success = adminService.approveUser(userName, approved);
        if (success) {
            return Result.success(approved ? "审批通过" : "已拒绝", null);
        }
        return Result.error(404, "用户不存在");
    }
}
