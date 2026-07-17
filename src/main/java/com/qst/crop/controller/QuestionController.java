package com.qst.crop.controller;

import com.qst.crop.common.Result;
import com.qst.crop.entity.Question;
import com.qst.crop.entity.QuestionMessage;
import com.qst.crop.security.SecurityUtil;
import com.qst.crop.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 在线问答：农户提问、专家回复
 */
@RestController
@RequestMapping("/api/v1/question")
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    /** 农户向指定专家提问 */
    @PostMapping("/ask")
    public Result<Question> ask(@RequestBody Question question) {
        // 以登录身份作为提问者，避免前端伪造
        question.setQuestioner(SecurityUtil.getCurrentUsername());
        Question saved = questionService.ask(question);
        return Result.success("提问成功，等待专家解答", saved);
    }

    /** 专家查看提问给自己的问题 */
    @GetMapping("/list")
    public Result<List<Question>> listForExpert() {
        String expert = SecurityUtil.getCurrentUsername();
        return Result.success("查询成功", questionService.listForExpert(expert));
    }

    /** 农户查看自己的提问记录 */
    @GetMapping("/my")
    public Result<List<Question>> myQuestions() {
        String user = SecurityUtil.getCurrentUsername();
        return Result.success("查询成功", questionService.listByQuestioner(user));
    }

    /** 专家回复问题（留言板，旧接口保留兼容） */
    @PutMapping("/answer")
    public Result<Question> answer(@RequestBody Question body) {
        String expert = SecurityUtil.getCurrentUsername();
        Question updated = questionService.answer(body.getId(), expert, body.getAnswer());
        return Result.success("回复成功", updated);
    }

    /** 获取某个问题的全部对话消息（农户与专家持续聊天） */
    @GetMapping("/{id}/messages")
    public Result<List<QuestionMessage>> messages(@PathVariable Integer id) {
        return Result.success("查询成功", questionService.listMessages(id));
    }

    /** 在某个问题下追加一条对话消息（农户追问 / 专家回复） */
    @PostMapping("/{id}/messages")
    public Result<QuestionMessage> reply(@PathVariable Integer id, @RequestBody QuestionMessage body) {
        QuestionMessage saved = questionService.reply(id, body.getContent());
        return Result.success("发送成功", saved);
    }
}
