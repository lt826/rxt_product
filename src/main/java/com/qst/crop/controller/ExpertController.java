package com.qst.crop.controller;

import com.qst.crop.common.Result;
import com.qst.crop.entity.Expert;
import com.qst.crop.service.ExpertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 专家信息：农户选择专家时查询
 */
@RestController
@RequestMapping("/api/v1/expert")
public class ExpertController {

    @Autowired
    private ExpertService expertService;

    /** 查询全部专家（供农户提问/预约时选择） */
    @GetMapping("/list")
    public Result<List<Expert>> list() {
        return Result.success("查询成功", expertService.listAll());
    }
}
