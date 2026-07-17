package com.qst.crop.service;

import com.qst.crop.entity.Question;
import com.qst.crop.entity.QuestionMessage;

import java.util.List;

public interface QuestionService {

    /** 农户向专家提问 */
    Question ask(Question question);

    /** 专家查看提问给自己的问题列表 */
    List<Question> listForExpert(String expertName);

    /** 农户查看自己提出的问题 */
    List<Question> listByQuestioner(String questioner);

    /** 专家回复问题（留言板，旧接口保留兼容） */
    Question answer(Integer id, String expertName, String answer);

    /** 获取某个问题的全部对话消息（农户与专家持续聊天） */
    List<QuestionMessage> listMessages(Integer questionId);

    /** 在某个问题下追加一条对话消息（农户追问或专家回复）。发送者取当前登录用户 */
    QuestionMessage reply(Integer questionId, String content);
}
