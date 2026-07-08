package com.qst.crop.service;

import com.qst.crop.entity.Question;

import java.util.List;

public interface QuestionService {

    /** 农户向专家提问 */
    Question ask(Question question);

    /** 专家查看提问给自己的问题列表 */
    List<Question> listForExpert(String expertName);

    /** 农户查看自己提出的问题 */
    List<Question> listByQuestioner(String questioner);

    /** 专家回复问题 */
    Question answer(Integer id, String expertName, String answer);
}
