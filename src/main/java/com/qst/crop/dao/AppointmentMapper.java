package com.qst.crop.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qst.crop.entity.Appointment;
import org.apache.ibatis.annotations.Mapper;

/**
 * 预约表 Mapper 接口
 */
@Mapper
public interface AppointmentMapper extends BaseMapper<Appointment> {
}
