package com.qst.crop.model;

import lombok.Data;

import java.util.List;

/**
 * 后台「主页内容管理」某板块的候选与已选配置
 */
@Data
public class SectionConfigVO {

    /** 板块标识 */
    private String section;

    /** 可选内容（全量候选，已解析为实体/VO） */
    private List<Object> candidates;

    /** 当前已精选的内容（refId + 顺序） */
    private List<FeatureItemDTO> selected;
}
