package com.qst.crop.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qst.crop.entity.QuestionMessage;
import org.apache.ibatis.annotations.Mapper;

/**
 * 问答对话消息 Mapper 接口
 */
@Mapper
public interface QuestionMessageMapper extends BaseMapper<QuestionMessage> {
}
