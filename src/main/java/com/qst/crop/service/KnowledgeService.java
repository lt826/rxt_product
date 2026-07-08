package com.qst.crop.service;

import com.qst.crop.entity.Knowledge;

import java.util.List;

public interface KnowledgeService {

    /** 查询所有农业知识（农户可看），支持关键字搜索 */
    List<Knowledge> list(String keyword);

    /** 知识详情 */
    Knowledge detail(Integer id);

    /** 专家发布知识 */
    Knowledge publish(Knowledge knowledge);

    /** 专家修改自己的知识 */
    Knowledge update(Knowledge knowledge);

    /** 专家删除自己的知识 */
    boolean delete(Integer id, String ownName);

    /** 专家查看自己发布的知识 */
    List<Knowledge> listByOwner(String ownName);
}
