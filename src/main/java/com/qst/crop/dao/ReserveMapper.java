package com.qst.crop.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qst.crop.entity.Reserve;
import org.apache.ibatis.annotations.Mapper;

/**
 * 专家预约表 Mapper 接口
 */
@Mapper
public interface ReserveMapper extends BaseMapper<Reserve> {
}
