package com.qst.crop.service;

import java.util.List;

/**
 * 关注关系服务（买家关注农户）
 */
public interface FollowService {

    /** 关注：幂等插入，成功（新关注）后给农户发通知，返回是否为新增关注 */
    boolean follow(String follower, String followee);

    /** 取消关注 */
    boolean unfollow(String follower, String followee);

    /** 是否已关注 */
    boolean isFollowing(String follower, String followee);

    /** 粉丝数量 */
    int countFans(String followee);

    /** 某农户的全部粉丝用户名（用于商品发布时按关注推送） */
    List<String> listFollowerNames(String followee);
}
