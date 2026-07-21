package com.qst.crop.service;

import com.qst.crop.entity.HomeCarousel;

import java.util.List;

/**
 * 首页轮播图服务
 */
public interface HomeCarouselService {

    /** 首页：按展示顺序获取全部轮播图 */
    List<HomeCarousel> list();

    /** 后台：新增一张轮播图（自动追加到末尾） */
    void add(HomeCarousel carousel);

    /** 后台：删除指定轮播图 */
    void delete(Long id);

    /** 后台：按传入的 id 顺序重新排序 */
    void reorder(List<Long> ids);
}
