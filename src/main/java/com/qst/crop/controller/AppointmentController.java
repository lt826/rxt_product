package com.qst.crop.controller;

import com.qst.crop.common.Result;
import com.qst.crop.entity.Appointment;
import com.qst.crop.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 预约控制器
 */
@RestController
@RequestMapping("/api/v1/appointment")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    /**
     * 创建预约
     */
    @PostMapping
    public Result<Appointment> create(@RequestBody Appointment appointment) {
        try {
            Appointment created = appointmentService.createAppointment(appointment);
            return Result.success("预约申请已提交，请等待专家确认", created);
        } catch (Exception e) {
            return Result.error(400, e.getMessage());
        }
    }

    /**
     * 获取农户的预约列表
     */
    @GetMapping("/farmer/{farmerName}")
    public Result<List<Appointment>> listByFarmer(@PathVariable String farmerName) {
        List<Appointment> list = appointmentService.listByFarmer(farmerName);
        return Result.success("查询成功", list);
    }

    /**
     * 获取专家的预约列表
     */
    @GetMapping("/expert/{expertName}")
    public Result<List<Appointment>> listByExpert(@PathVariable String expertName) {
        List<Appointment> list = appointmentService.listByExpert(expertName);
        return Result.success("查询成功", list);
    }

    /**
     * 专家处理预约
     */
    @PostMapping("/{id}/handle")
    public Result<String> handle(
            @PathVariable Integer id,
            @RequestParam String expertName,
            @RequestParam boolean approved,
            @RequestParam(required = false) String reply) {
        try {
            appointmentService.handleAppointment(id, expertName, approved, reply);
            return Result.success(approved ? "已同意预约" : "已拒绝预约", null);
        } catch (Exception e) {
            return Result.error(400, e.getMessage());
        }
    }

    /**
     * 取消预约
     */
    @PostMapping("/{id}/cancel")
    public Result<String> cancel(@PathVariable Integer id, @RequestParam String farmerName) {
        try {
            appointmentService.cancelAppointment(id, farmerName);
            return Result.success("已取消预约", null);
        } catch (Exception e) {
            return Result.error(400, e.getMessage());
        }
    }

    /**
     * 完成预约
     */
    @PostMapping("/{id}/complete")
    public Result<String> complete(@PathVariable Integer id, @RequestParam String expertName) {
        try {
            appointmentService.completeAppointment(id, expertName);
            return Result.success("预约已完成", null);
        } catch (Exception e) {
            return Result.error(400, e.getMessage());
        }
    }
}
