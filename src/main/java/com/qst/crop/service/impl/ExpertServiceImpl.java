package com.qst.crop.service.impl;

import com.qst.crop.dao.ExpertMapper;
import com.qst.crop.entity.Expert;
import com.qst.crop.service.ExpertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExpertServiceImpl implements ExpertService {

    @Autowired
    private ExpertMapper expertMapper;

    @Override
    public List<Expert> listAll() {
        return expertMapper.selectList(null);
    }
}
