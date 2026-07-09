package com.qst.crop.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qst.crop.entity.Message;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MessageMapper extends BaseMapper<Message> {
}
