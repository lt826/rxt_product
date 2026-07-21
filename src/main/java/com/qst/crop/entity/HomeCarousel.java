package com.qst.crop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 首页轮播图实体类
 * <p>
 * 管理员上传的首页轮播图片，sort_order 升序决定展示顺序，
 * link 为可空的点击跳转链接。
 */
@Data
@TableName("tb_home_carousel")
public class HomeCarousel {

    /** 主键 */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 图片地址 */
    private String imageUrl;

    /** 点击跳转链接（可空） */
    private String link;

    /** 展示顺序，升序 */
    private Integer sortOrder;

    /** 创建时间 */
    private LocalDateTime createTime;
}
