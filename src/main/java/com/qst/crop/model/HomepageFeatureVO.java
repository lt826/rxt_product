package com.qst.crop.model;

import com.qst.crop.entity.Expert;
import com.qst.crop.entity.Goods;
import com.qst.crop.entity.Knowledge;
import com.qst.crop.entity.Question;
import lombok.Data;

import java.util.List;

/**
 * 首页各板块精选内容（已解析为实体，供首页直接渲染）
 */
@Data
public class HomepageFeatureVO {

    /** 金融产品 */
    private List<FinanceFeatureVO> finance;

    /** 农业专家团队 */
    private List<Expert> expert;

    /** 热门问答 */
    private List<Question> answer;

    /** 农业知识 */
    private List<Knowledge> knowledge;

    /** 助农电商 */
    private List<Goods> goods;
}
