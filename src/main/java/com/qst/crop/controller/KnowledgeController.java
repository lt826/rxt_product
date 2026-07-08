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

@RestController
@RequestMapping("/api/v1/knowledge")
public class KnowledgeController {

    @Autowired
    private KnowledgeService knowledgeService;

    @Autowired
    private DiscussService discussService;

    @GetMapping("/list")
    public Result<List<Knowledge>> list(@RequestParam(required = false) String keyword) {
        return Result.success("查询成功", knowledgeService.list(keyword));
    }

    @GetMapping("/detail/{id}")
    public Result<Map<String, Object>> detail(@PathVariable Integer id) {
        Knowledge knowledge = knowledgeService.detail(id);
        List<Discuss> discusses = discussService.listByKnowledge(id);
        Map<String, Object> data = new HashMap<>();
        data.put("knowledge", knowledge);
        data.put("discusses", discusses);
        return Result.success("查询成功", data);
    }

    @PostMapping("/publish")
    public Result<Knowledge> publish(@RequestBody Knowledge knowledge) {
        knowledge.setOwnName(SecurityUtil.getCurrentUsername());
        Knowledge saved = knowledgeService.publish(knowledge);
        return Result.success("发布成功", saved);
    }

    @PutMapping("/update")
    public Result<Knowledge> update(@RequestBody Knowledge knowledge) {
        knowledge.setOwnName(SecurityUtil.getCurrentUsername());
        Knowledge updated = knowledgeService.update(knowledge);
        return Result.success("修改成功", updated);
    }

    @DeleteMapping("/delete/{id}")
    public Result<Boolean> delete(@PathVariable Integer id) {
        boolean ok = knowledgeService.delete(id, SecurityUtil.getCurrentUsername());
        return ok ? Result.success("删除成功", true) : Result.error(404, "知识不存在");
    }

    @GetMapping("/my")
    public Result<List<Knowledge>> myKnowledge() {
        return Result.success("查询成功",
                knowledgeService.listByOwner(SecurityUtil.getCurrentUsername()));
    }

    @PostMapping("/discuss")
    public Result<Discuss> discuss(@RequestBody Discuss discuss) {
        discuss.setOwnName(SecurityUtil.getCurrentUsername());
        Discuss saved = discussService.publish(discuss);
        return Result.success("评论发表成功", saved);
    }

    @GetMapping("/{knowledgeId}/discusses")
    public Result<List<Discuss>> getDiscusses(@PathVariable Integer knowledgeId) {
        return Result.success("查询成功", discussService.listByKnowledge(knowledgeId));
    }

    @GetMapping("/{id}")
    public Result<Map<String, Object>> getKnowledgeDetail(@PathVariable Integer id) {
        return detail(id);
    }
}
