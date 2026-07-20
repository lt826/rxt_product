package com.qst.crop.model;

import lombok.Data;

/**
 * 保存某板块精选内容时的单条入参
 */
@Data
public class FeatureItemDTO {

    /** 关联实体主键（专家为 user_name，其余为数字 id） */
    private String refId;

    /** 展示顺序 */
    private Integer sortOrder;
}
