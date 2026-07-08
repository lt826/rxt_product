package com.qst.crop.controller;

import com.qst.crop.common.Result;
import com.qst.crop.entity.Discuss;
import com.qst.crop.entity.Knowledge;
import com.qst.crop.security.SecurityUtil;
import com.qst.crop.service.DiscussService;
import com.qst.crop.service.KnowledgeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 农业知识：专家发布/管理、农户浏览、通用评论
 */
@RestController
@RequestMapping("/api/v1/knowledge")
public class KnowledgeController {

    @Autowired
    private KnowledgeService knowledgeService;

    @Autowired
    private DiscussService discussService;

    /** 查询所有农业知识（农户可看），支持关键字搜索 */
    @GetMapping("/list")
    public Result<List<Knowledge>> list(@RequestParam(required = false) String keyword) {
        return Result.success("查询成功", knowledgeService.list(keyword));
    }

    /** 知识详情（含评论列表） */
    @GetMapping("/detail/{id}")
    public Result<Map<String, Object>> detail(@PathVariable Integer id) {
        Knowledge knowledge = knowledgeService.detail(id);
        List<Discuss> discusses = discussService.listByKnowledge(id);
        Map<String, Object> data = new HashMap<>();
        data.put("knowledge", knowledge);
        data.put("discusses", discusses);
        return Result.success("查询成功", data);
    }

    /** 专家发布新文章 */
    @PostMapping("/publish")
    public Result<Knowledge> publish(@RequestBody Knowledge knowledge) {
        knowledge.setOwnName(SecurityUtil.getCurrentUsername());
        Knowledge saved = knowledgeService.publish(knowledge);
        return Result.success("发布成功", saved);
    }

    /** 专家修改自己的文章 */
    @PutMapping("/update")
    public Result<Knowledge> update(@RequestBody Knowledge knowledge) {
        knowledge.setOwnName(SecurityUtil.getCurrentUsername());
        Knowledge updated = knowledgeService.update(knowledge);
        return Result.success("修改成功", updated);
    }

    /** 专家删除自己的文章 */
    @DeleteMapping("/delete/{id}")
    public Result<Boolean> delete(@PathVariable Integer id) {
        boolean ok = knowledgeService.delete(id, SecurityUtil.getCurrentUsername());
        return ok ? Result.success("删除成功", true) : Result.error(404, "知识不存在");
    }

    /** 专家查看自己发布的文章 */
    @GetMapping("/my")
    public Result<List<Knowledge>> myKnowledge() {
        return Result.success("查询成功",
                knowledgeService.listByOwner(SecurityUtil.getCurrentUsername()));
    }

    /** 发表知识评论（留言板） */
    @PostMapping("/discuss")
    public Result<Discuss> discuss(@RequestBody Discuss discuss) {
        discuss.setOwnName(SecurityUtil.getCurrentUsername());
        Discuss saved = discussService.publish(discuss);
        return Result.success("评论发表成功", saved);
    }
}
