package com.qst.crop.service;

import com.qst.crop.entity.Expert;

import java.util.List;

public interface ExpertService {

    /** 查询全部专家信息（供农户选择） */
    List<Expert> listAll();
}
