package com.qst.crop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qst.crop.dao.AppointmentMapper;
import com.qst.crop.dao.ExpertMapper;
import com.qst.crop.entity.Appointment;
import com.qst.crop.entity.Expert;
import com.qst.crop.service.AppointmentService;
import com.qst.crop.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class AppointmentServiceImpl implements AppointmentService {

    @Autowired
    private AppointmentMapper appointmentMapper;

    @Autowired
    private ExpertMapper expertMapper;

    @Autowired
    private NotificationService notificationService;

    @Override
    public Appointment createAppointment(Appointment appointment) {
        // 获取专家真实姓名
        if (appointment.getExpertName() != null) {
            Expert expert = expertMapper.selectById(appointment.getExpertName());
            if (expert != null) {
                appointment.setExpertRealName(expert.getRealName());
            }
        }

        LocalDateTime now = LocalDateTime.now();
        appointment.setCreateTime(now);
        appointment.setUpdateTime(now);
        appointment.setStatus("pending");

        appointmentMapper.insert(appointment);
        // 通知专家有新的预约待处理
        if (appointment.getExpertName() != null) {
            try {
                String farmer = appointment.getFarmerName() != null ? appointment.getFarmerName() : "农户";
                String when = appointment.getAppointmentTime() != null
                        ? String.valueOf(appointment.getAppointmentTime()).replace("T", " ") : "未指定时间";
                String content = "农户「" + farmer + "」预约了您（时间：" + when + "），请前往处理。";
                notificationService.send(appointment.getExpertName(), "新增预约待处理", content, "appointment_new", appointment.getId());
            } catch (Exception e) {
                log.warn("预约通知发送失败: {}", e.getMessage());
            }
        }
        return appointment;
    }

    @Override
    public List<Appointment> listByFarmer(String farmerName) {
        QueryWrapper<Appointment> wrapper = new QueryWrapper<>();
        wrapper.eq("farmer_name", farmerName).orderByDesc("create_time");
        return appointmentMapper.selectList(wrapper);
    }

    @Override
    public List<Appointment> listByExpert(String expertName) {
        QueryWrapper<Appointment> wrapper = new QueryWrapper<>();
        wrapper.eq("expert_name", expertName).orderByDesc("create_time");
        return appointmentMapper.selectList(wrapper);
    }

    @Override
    public Appointment handleAppointment(Integer id, String expertName, boolean approved, String reply) {
        Appointment appointment = appointmentMapper.selectById(id);
        if (appointment == null) {
            throw new RuntimeException("预约不存在");
        }
        if (!expertName.equals(appointment.getExpertName())) {
            throw new RuntimeException("无权处理该预约");
        }
        if (!"pending".equals(appointment.getStatus())) {
            throw new RuntimeException("该预约已被处理");
        }

        appointment.setStatus(approved ? "approved" : "rejected");
        appointment.setReply(reply);
        appointment.setUpdateTime(LocalDateTime.now());

        appointmentMapper.updateById(appointment);
        // 通知农户预约处理结果
        if (appointment.getFarmerName() != null) {
            try {
                String title = approved ? "预约已通过" : "预约已拒绝";
                String result = approved ? "已通过，请按约定时间等候专家。" : "已被拒绝。";
                String tail = (reply != null && !reply.trim().isEmpty()) ? ("专家回复：" + reply) : "";
                String content = ("您对专家「" + (appointment.getExpertRealName() != null ? appointment.getExpertRealName() : appointment.getExpertName())
                        + "」的预约" + result + (tail.isEmpty() ? "" : ("\n" + tail)));
                notificationService.send(appointment.getFarmerName(), "预约结果通知", content, "appointment_result", appointment.getId());
            } catch (Exception e) {
                log.warn("预约结果通知发送失败: {}", e.getMessage());
            }
        }
        return appointment;
    }

    @Override
    public boolean cancelAppointment(Integer id, String farmerName) {
        Appointment appointment = appointmentMapper.selectById(id);
        if (appointment == null) {
            throw new RuntimeException("预约不存在");
        }
        if (!farmerName.equals(appointment.getFarmerName())) {
            throw new RuntimeException("无权取消该预约");
        }
        if (!"pending".equals(appointment.getStatus()) && !"approved".equals(appointment.getStatus())) {
            throw new RuntimeException("该预约无法取消");
        }

        appointment.setStatus("cancelled");
        appointment.setUpdateTime(LocalDateTime.now());

        return appointmentMapper.updateById(appointment) > 0;
    }

    @Override
    public boolean completeAppointment(Integer id, String expertName) {
        Appointment appointment = appointmentMapper.selectById(id);
        if (appointment == null) {
            throw new RuntimeException("预约不存在");
        }
        if (!expertName.equals(appointment.getExpertName())) {
            throw new RuntimeException("无权完成该预约");
        }
        if (!"approved".equals(appointment.getStatus())) {
            throw new RuntimeException("该预约状态不正确");
        }

        appointment.setStatus("completed");
        appointment.setUpdateTime(LocalDateTime.now());

        return appointmentMapper.updateById(appointment) > 0;
    }
}
