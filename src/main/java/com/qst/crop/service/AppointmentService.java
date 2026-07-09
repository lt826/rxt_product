package com.qst.crop.service;

import com.qst.crop.entity.Appointment;

import java.util.List;

/**
 * 预约服务接口
 */
public interface AppointmentService {

    /**
     * 创建预约
     */
    Appointment createAppointment(Appointment appointment);

    /**
     * 获取农户的预约列表
     */
    List<Appointment> listByFarmer(String farmerName);

    /**
     * 获取专家的预约列表
     */
    List<Appointment> listByExpert(String expertName);

    /**
     * 专家处理预约（同意/拒绝）
     * @param id 预约ID
     * @param expertName 专家用户名
     * @param approved true-同意，false-拒绝
     * @param reply 回复/原因
     */
    Appointment handleAppointment(Integer id, String expertName, boolean approved, String reply);

    /**
     * 取消预约
     */
    boolean cancelAppointment(Integer id, String farmerName);

    /**
     * 完成预约
     */
    boolean completeAppointment(Integer id, String expertName);
}
