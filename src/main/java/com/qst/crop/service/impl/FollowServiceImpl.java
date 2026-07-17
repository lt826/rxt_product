package com.qst.crop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qst.crop.dao.FollowMapper;
import com.qst.crop.dao.UserMapper;
import com.qst.crop.entity.Follow;
import com.qst.crop.entity.User;
import com.qst.crop.service.FollowService;
import com.qst.crop.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 关注关系服务实现类
 */
@Slf4j
@Service
public class FollowServiceImpl implements FollowService {

    @Autowired
    private FollowMapper followMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private NotificationService notificationService;

    @Override
    public boolean follow(String follower, String followee) {
        if (follower == null || followee == null || follower.equals(followee)) {
            return false;
        }
        // 幂等：已关注则直接返回 false（非新增）
        if (isFollowing(follower, followee)) {
            return false;
        }
        Follow f = new Follow();
        f.setFollower(follower);
        f.setFollowee(followee);
        f.setCreateTime(LocalDateTime.now());
        followMapper.insert(f);

        // 关注成功后，给农户发站内通知「XXX 关注了你」
        try {
            User fan = userMapper.selectById(follower);
            String displayName = (fan != null && fan.getRealName() != null && !fan.getRealName().isEmpty())
                    ? fan.getRealName()
                    : ((fan != null && fan.getNickName() != null && !fan.getNickName().isEmpty())
                        ? fan.getNickName() : follower);
            String content = "用户「" + displayName + "」关注了您。";
            notificationService.send(followee, "新增粉丝", content, "new_follower", null);
        } catch (Exception e) {
            log.warn("关注通知发送失败: follower={}, followee={}, err={}", follower, followee, e.getMessage());
        }
        return true;
    }

    @Override
    public boolean unfollow(String follower, String followee) {
        if (follower == null || followee == null) {
            return false;
        }
        Follow cond = new Follow();
        cond.setFollower(follower);
        cond.setFollowee(followee);
        return followMapper.delete(new QueryWrapper<>(cond)) > 0;
    }

    @Override
    public boolean isFollowing(String follower, String followee) {
        if (follower == null || followee == null) {
            return false;
        }
        QueryWrapper<Follow> w = new QueryWrapper<>();
        w.eq("follower", follower).eq("followee", followee);
        return followMapper.selectCount(w) > 0;
    }

    @Override
    public int countFans(String followee) {
        if (followee == null) {
            return 0;
        }
        QueryWrapper<Follow> w = new QueryWrapper<>();
        w.eq("followee", followee);
        return Math.toIntExact(followMapper.selectCount(w));
    }

    @Override
    public List<String> listFollowerNames(String followee) {
        if (followee == null) {
            return new ArrayList<>();
        }
        QueryWrapper<Follow> w = new QueryWrapper<>();
        w.eq("followee", followee);
        List<Follow> rows = followMapper.selectList(w);
        List<String> names = new ArrayList<>();
        for (Follow f : rows) {
            if (f.getFollower() != null) {
                names.add(f.getFollower());
            }
        }
        return names;
    }
}
