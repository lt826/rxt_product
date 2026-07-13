package com.qst.crop.service;

import com.qst.crop.entity.Finance;
import com.qst.crop.model.FinanceApplyDTO;
import com.qst.crop.model.FinanceApprovalVO;
import com.qst.crop.model.RecommendPartnerVO;
import java.util.List;

public interface FinanceService {
    List<RecommendPartnerVO> recommendPartners(String currentUserName, Integer amount, String item);
    Finance apply(String userName, FinanceApplyDTO dto);
    List<Finance> getMyFinances(String userName);
    Finance getFinanceById(Integer financeId);
    /** 获取所有融资申请（银行用） */
    List<Finance> getAllFinances();

    /** 获取融资审批列表（含联合贷款人、信誉分、匹配度，银行端用） */
    List<FinanceApprovalVO> getApprovalList();

    /** 审核融资申请 */
    boolean updateFinanceStatus(Integer financeId, Integer status, String remark);
}

