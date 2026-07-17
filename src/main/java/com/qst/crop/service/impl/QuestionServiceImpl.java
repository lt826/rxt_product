package com.qst.crop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qst.crop.dao.QuestionMapper;
import com.qst.crop.dao.QuestionMessageMapper;
import com.qst.crop.dao.UserMapper;
import com.qst.crop.entity.Question;
import com.qst.crop.entity.QuestionMessage;
import com.qst.crop.entity.User;
import com.qst.crop.security.SecurityUtil;
import com.qst.crop.service.NotificationService;
import com.qst.crop.service.QuestionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class QuestionServiceImpl implements QuestionService {

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private QuestionMessageMapper questionMessageMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private NotificationService notificationService;

    @Override
    public Question ask(Question question) {
        // 若农户未填手机号，则从用户表自动补全
        if ((question.getPhone() == null || question.getPhone().isEmpty())
                && question.getQuestioner() != null) {
            User user = userMapper.selectById(question.getQuestioner());
            if (user != null) {
                question.setPhone(user.getPhone());
            }
        }
        // 若未填写作物名称，设置默认值
        if (question.getPlantName() == null || question.getPlantName().isEmpty()) {
            question.setPlantName("通用");
        }
        question.setStatus(0);
        questionMapper.insert(question);

        // 把问题正文作为首条消息写入对话表，开启持续聊天
        if (question.getQuestion() != null && !question.getQuestion().isEmpty()) {
            QuestionMessage msg = new QuestionMessage();
            msg.setQuestionId(question.getId());
            msg.setSender(question.getQuestioner());
            msg.setSenderRole("farmer");
            msg.setContent(question.getQuestion());
            msg.setCreateTime(LocalDateTime.now());
            questionMessageMapper.insert(msg);
        }

        // 通知被提问的专家有新问题待处理
        if (question.getExpertName() != null) {
            try {
                String farmer = question.getQuestioner() != null ? question.getQuestioner() : "农户";
                String title = question.getTitle() != null ? question.getTitle() : "新问题";
                String content = "农户「" + farmer + "」向您提问：《" + title + "》，请前往问答处理。";
                notificationService.send(question.getExpertName(), "新增问题待处理", content, "question_new", question.getId());
            } catch (Exception e) {
                log.warn("新问题通知发送失败: {}", e.getMessage());
            }
        }
        return question;
    }

    @Override
    public List<Question> listForExpert(String expertName) {
        QueryWrapper<Question> wrapper = new QueryWrapper<>();
        wrapper.eq("expert_name", expertName).orderByDesc("id");
        return questionMapper.selectList(wrapper);
    }

    @Override
    public List<Question> listByQuestioner(String questioner) {
        QueryWrapper<Question> wrapper = new QueryWrapper<>();
        wrapper.eq("questioner", questioner).orderByDesc("id");
        return questionMapper.selectList(wrapper);
    }

    @Override
    public Question answer(Integer id, String expertName, String answer) {
        Question q = questionMapper.selectById(id);
        if (q == null) {
            throw new RuntimeException("问题不存在");
        }
        // 仅受理该专家名下的问题
        if (expertName != null && !expertName.equals(q.getExpertName())) {
            throw new RuntimeException("无权回复该问题");
        }
        q.setAnswer(answer);
        q.setStatus(1);
        questionMapper.updateById(q);
        // 旧接口回复也同步落一条对话消息，保证聊天历史完整
        appendMessage(q, expertName, "expert", answer);
        return q;
    }

    @Override
    public List<QuestionMessage> listMessages(Integer questionId) {
        Question q = questionMapper.selectById(questionId);
        if (q == null) {
            return new ArrayList<>();
        }
        QueryWrapper<QuestionMessage> w = new QueryWrapper<>();
        w.eq("question_id", questionId).orderByAsc("create_time");
        List<QuestionMessage> msgs = questionMessageMapper.selectList(w);
        // 兼容旧数据：若对话表为空但问题表本身有旧 question/answer，则按这两条合成返回（不落库）
        if ((msgs == null || msgs.isEmpty()) && (hasText(q.getQuestion()) || hasText(q.getAnswer()))) {
            List<QuestionMessage> legacy = new ArrayList<>();
            if (hasText(q.getQuestion())) {
                QuestionMessage m = new QuestionMessage();
                m.setQuestionId(questionId);
                m.setSender(q.getQuestioner());
                m.setSenderRole("farmer");
                m.setContent(q.getQuestion());
                legacy.add(m);
            }
            if (hasText(q.getAnswer())) {
                QuestionMessage m = new QuestionMessage();
                m.setQuestionId(questionId);
                m.setSender(q.getExpertName());
                m.setSenderRole("expert");
                m.setContent(q.getAnswer());
                legacy.add(m);
            }
            return legacy;
        }
        return msgs;
    }

    @Override
    public QuestionMessage reply(Integer questionId, String content) {
        Question q = questionMapper.selectById(questionId);
        if (q == null) {
            throw new RuntimeException("问题不存在");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new RuntimeException("回复内容不能为空");
        }
        String sender = SecurityUtil.getCurrentUsername();
        boolean byExpert = sender != null && sender.equals(q.getExpertName());
        QuestionMessage msg = new QuestionMessage();
        msg.setQuestionId(questionId);
        msg.setSender(sender);
        msg.setSenderRole(byExpert ? "expert" : "farmer");
        msg.setContent(content.trim());
        msg.setCreateTime(LocalDateTime.now());
        questionMessageMapper.insert(msg);

        // 同步更新问题表：专家回复时把 answer/status 刷新（兼容旧前端/筛选）
        if (byExpert) {
            q.setAnswer(content.trim());
            if (q.getStatus() == null || q.getStatus() == 0) {
                q.setStatus(1);
                questionMapper.updateById(q);
            }
            // 通知农户收到问题回复
            try {
                String title = q.getTitle() != null ? q.getTitle() : "您的问题";
                notificationService.send(q.getQuestioner(), "问题回复消息",
                        "专家已回复您的问题《" + title + "》，请前往专家问答查看。", "question_reply", questionId);
            } catch (Exception e) {
                log.warn("问题回复通知发送失败: {}", e.getMessage());
            }
        }
        return msg;
    }

    /** 内部：落一条对话消息 */
    private void appendMessage(Question q, String sender, String role, String content) {
        if (content == null || content.trim().isEmpty()) return;
        QuestionMessage msg = new QuestionMessage();
        msg.setQuestionId(q.getId());
        msg.setSender(sender);
        msg.setSenderRole(role);
        msg.setContent(content.trim());
        msg.setCreateTime(LocalDateTime.now());
        questionMessageMapper.insert(msg);
    }

    private boolean hasText(String s) {
        return s != null && !s.trim().isEmpty();
    }
}
