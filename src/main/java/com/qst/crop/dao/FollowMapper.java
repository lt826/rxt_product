package com.qst.crop.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qst.crop.entity.Follow;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户关注关系 Mapper 接口
 */
@Mapper
public interface FollowMapper extends BaseMapper<Follow> {
}
