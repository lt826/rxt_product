package com.qst.crop.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qst.crop.entity.Expert;
import org.apache.ibatis.annotations.Mapper;

/**
 * 专家信息表 Mapper 接口
 */
@Mapper
public interface ExpertMapper extends BaseMapper<Expert> {
}
