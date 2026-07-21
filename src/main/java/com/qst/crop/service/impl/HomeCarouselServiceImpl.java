package com.qst.crop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qst.crop.dao.HomeCarouselMapper;
import com.qst.crop.entity.HomeCarousel;
import com.qst.crop.service.HomeCarouselService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 首页轮播图服务实现
 */
@Service
public class HomeCarouselServiceImpl implements HomeCarouselService {

    @Autowired
    private HomeCarouselMapper homeCarouselMapper;

    @Override
    public List<HomeCarousel> list() {
        // 按展示顺序升序，同序按创建时间倒序（新上传靠前可见）
        QueryWrapper<HomeCarousel> wrapper = new QueryWrapper<>();
        wrapper.orderByAsc("sort_order").orderByDesc("create_time");
        return homeCarouselMapper.selectList(wrapper);
    }

    @Override
    public void add(HomeCarousel carousel) {
        if (carousel == null || carousel.getImageUrl() == null || carousel.getImageUrl().trim().isEmpty()) {
            throw new IllegalArgumentException("图片地址不能为空");
        }
        // 自动追加到末尾：取当前最大 sort_order + 1
        List<HomeCarousel> all = list();
        int maxOrder = all.stream()
                .mapToInt(c -> c.getSortOrder() == null ? 0 : c.getSortOrder())
                .max()
                .orElse(-1);
        carousel.setSortOrder(maxOrder + 1);
        carousel.setCreateTime(LocalDateTime.now());
        homeCarouselMapper.insert(carousel);
    }

    @Override
    public void delete(Long id) {
        if (id == null) {
            return;
        }
        homeCarouselMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void reorder(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        // 按入参顺序重写 sort_order
        int order = 0;
        for (Long id : ids) {
            if (id == null) {
                continue;
            }
            HomeCarousel update = new HomeCarousel();
            update.setId(id);
            update.setSortOrder(order++);
            homeCarouselMapper.updateById(update);
        }
    }
}
