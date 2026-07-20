package com.qst.crop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 首页精选内容表实体类
 * <p>
 * 记录首页各板块被精选展示的内容，按 (板块 section_key + 引用主键 ref_id) 唯一，
 * sort_order 升序决定展示顺序。ref_id 对专家为 user_name，其余板块为数字主键。
 */
@Data
@TableName("tb_homepage_feature")
public class HomepageFeature {

    /** 主键 */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 板块标识：finance/expert/answer/knowledge/goods */
    private String sectionKey;

    /** 关联实体主键（专家为 user_name，其余为数字 id） */
    private String refId;

    /** 展示顺序，升序 */
    private Integer sortOrder;

    /** 创建时间 */
    private LocalDateTime createTime;
}
