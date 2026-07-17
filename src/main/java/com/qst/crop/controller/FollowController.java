package com.qst.crop.controller;

import com.qst.crop.common.Result;
import com.qst.crop.security.SecurityUtil;
import com.qst.crop.service.FollowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 关注关系控制器（买家关注农户）
 */
@RestController
@RequestMapping("/api/v1/follow")
public class FollowController {

    @Autowired
    private FollowService followService;

    /** 关注某农户 */
    @PostMapping("/{followee}")
    public Result<Map<String, Object>> follow(@PathVariable String followee) {
        String follower = SecurityUtil.getCurrentUsername();
        boolean newFollow = followService.follow(follower, followee);
        Map<String, Object> data = new HashMap<>();
        data.put("following", true);
        data.put("newFollow", newFollow);
        return Result.success(newFollow ? "关注成功" : "已关注", data);
    }

    /** 取消关注 */
    @DeleteMapping("/{followee}")
    public Result<Map<String, Object>> unfollow(@PathVariable String followee) {
        String follower = SecurityUtil.getCurrentUsername();
        followService.unfollow(follower, followee);
        Map<String, Object> data = new HashMap<>();
        data.put("following", false);
        return Result.success("已取消关注", data);
    }

    /** 查询当前用户是否已关注某农户 */
    @GetMapping("/check")
    public Result<Map<String, Object>> check(@RequestParam String followee) {
        String follower = SecurityUtil.getCurrentUsername();
        Map<String, Object> data = new HashMap<>();
        data.put("following", followService.isFollowing(follower, followee));
        return Result.success("查询成功", data);
    }

    /** 查询某用户的粉丝数 */
    @GetMapping("/fans/count")
    public Result<Map<String, Object>> fansCount(@RequestParam String userName) {
        Map<String, Object> data = new HashMap<>();
        data.put("count", followService.countFans(userName));
        return Result.success("查询成功", data);
    }
}
