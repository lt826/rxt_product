package com.qst.crop.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qst.crop.entity.Question;
import org.apache.ibatis.annotations.Mapper;

/**
 * 在线问答表 Mapper 接口
 */
@Mapper
public interface QuestionMapper extends BaseMapper<Question> {
}
