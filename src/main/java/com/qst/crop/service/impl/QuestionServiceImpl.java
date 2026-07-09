package com.qst.crop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qst.crop.dao.QuestionMapper;
import com.qst.crop.dao.UserMapper;
import com.qst.crop.entity.Question;
import com.qst.crop.entity.User;
import com.qst.crop.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuestionServiceImpl implements QuestionService {

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private UserMapper userMapper;

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
        return q;
    }
}
