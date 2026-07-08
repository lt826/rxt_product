package com.qst.crop.service;

import com.qst.crop.entity.Goods;
import java.util.List;

public interface GoodsService {
    List<Goods> listByType(String type, String keyword);
    Goods publish(Goods goods);
    Goods update(Goods goods);
    boolean delete(Integer id, String ownName);
    List<Goods> listByOwner(String ownName, String type);
    Goods detail(Integer id);
}
