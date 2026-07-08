package com.qst.crop.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qst.crop.entity.Knowledge;
import org.apache.ibatis.annotations.Mapper;

/**
 * 农业知识表 Mapper 接口
 */
@Mapper
public interface KnowledgeMapper extends BaseMapper<Knowledge> {
}
