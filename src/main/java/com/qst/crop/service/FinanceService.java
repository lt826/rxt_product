package com.qst.crop.service;

import com.qst.crop.entity.Finance;
import com.qst.crop.model.FinanceApplyDTO;
import com.qst.crop.model.RecommendPartnerVO;
import java.util.List;

public interface FinanceService {
    List<RecommendPartnerVO> recommendPartners(Integer amount, String item);
    Finance apply(String userName, FinanceApplyDTO dto);
    List<Finance> getMyFinances(String userName);
    Finance getFinanceById(Integer financeId);
}
