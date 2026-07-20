package com.qst.crop.service;

import com.qst.crop.model.FeatureItemDTO;
import com.qst.crop.model.HomepageFeatureVO;
import com.qst.crop.model.SectionConfigVO;

import java.util.List;

/**
 * 首页精选内容服务接口
 */
public interface HomepageFeatureService {

    /**
     * 首页：解析全部板块的精选内容为实体，供首页直接渲染。
     */
    HomepageFeatureVO resolveForHome();

    /**
     * 后台：获取某板块的候选内容与当前已选配置。
     */
    SectionConfigVO getSectionConfig(String section);

    /**
     * 后台：整体替换某板块的精选内容（先删后插，顺序按入参顺序）。
     */
    void saveSection(String section, List<FeatureItemDTO> items);
}
