package com.qst.crop.service;

import com.qst.crop.entity.Expert;

import java.util.List;

public interface ExpertService {
    List<Expert> listAll();
    boolean createExpert(Expert expert);
}