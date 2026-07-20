package com.qst.crop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qst.crop.dao.BankMapper;
import com.qst.crop.dao.FinancingProductMapper;
import com.qst.crop.dao.GoodsMapper;
import com.qst.crop.dao.HomepageFeatureMapper;
import com.qst.crop.dao.KnowledgeMapper;
import com.qst.crop.dao.QuestionMapper;
import com.qst.crop.entity.Bank;
import com.qst.crop.entity.Expert;
import com.qst.crop.entity.FinancingProduct;
import com.qst.crop.entity.Goods;
import com.qst.crop.entity.HomepageFeature;
import com.qst.crop.entity.Knowledge;
import com.qst.crop.entity.Question;
import com.qst.crop.model.FinanceFeatureVO;
import com.qst.crop.model.FeatureItemDTO;
import com.qst.crop.model.HomepageFeatureVO;
import com.qst.crop.model.SectionConfigVO;
import com.qst.crop.service.ExpertService;
import com.qst.crop.service.HomepageFeatureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 首页精选内容服务实现
 */
@Service
public class HomepageFeatureServiceImpl implements HomepageFeatureService {

    private static final List<String> VALID_SECTIONS =
            Arrays.asList("finance", "expert", "answer", "knowledge", "goods");

    @Autowired
    private HomepageFeatureMapper homepageFeatureMapper;

    @Autowired
    private FinancingProductMapper financingProductMapper;

    @Autowired
    private BankMapper bankMapper;

    @Autowired
    private ExpertService expertService;

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private KnowledgeMapper knowledgeMapper;

    @Autowired
    private GoodsMapper goodsMapper;

    @Override
    public HomepageFeatureVO resolveForHome() {
        List<HomepageFeature> all = homepageFeatureMapper.selectList(null);
        Map<String, List<HomepageFeature>> grouped = all.stream()
                .collect(Collectors.groupingBy(HomepageFeature::getSectionKey));

        HomepageFeatureVO vo = new HomepageFeatureVO();
        vo.setFinance(resolveFinance(grouped.getOrDefault("finance", Collections.emptyList())));
        vo.setExpert(resolveExpert(grouped.getOrDefault("expert", Collections.emptyList())));

        // 热门问答只展示已有回答的
        List<Question> answers = resolveByIntIds(grouped.getOrDefault("answer", Collections.emptyList()),
                ids -> questionMapper.selectBatchIds(ids), Question::getId);
        answers = answers.stream()
                .filter(q -> q.getAnswer() != null && !q.getAnswer().trim().isEmpty())
                .collect(Collectors.toList());
        vo.setAnswer(answers);

        vo.setKnowledge(resolveByIntIds(grouped.getOrDefault("knowledge", Collections.emptyList()),
                ids -> knowledgeMapper.selectBatchIds(ids), Knowledge::getKnowledgeId));
        vo.setGoods(resolveByIntIds(grouped.getOrDefault("goods", Collections.emptyList()),
                ids -> goodsMapper.selectBatchIds(ids), Goods::getGoodsId));
        return vo;
    }

    @Override
    public SectionConfigVO getSectionConfig(String section) {
        if (!VALID_SECTIONS.contains(section)) {
            throw new IllegalArgumentException("无效的板块：" + section);
        }
        SectionConfigVO vo = new SectionConfigVO();
        vo.setSection(section);
        vo.setCandidates(loadCandidates(section));
        vo.setSelected(loadSelected(section));
        return vo;
    }

    @Override
    @Transactional
    public void saveSection(String section, List<FeatureItemDTO> items) {
        if (!VALID_SECTIONS.contains(section)) {
            throw new IllegalArgumentException("无效的板块：" + section);
        }
        // 删除该板块旧数据
        QueryWrapper<HomepageFeature> wrapper = new QueryWrapper<>();
        wrapper.eq("section_key", section);
        homepageFeatureMapper.delete(wrapper);

        if (items == null || items.isEmpty()) {
            return;
        }
        // 按入参顺序写入，去重，避免触发 (section_key, ref_id) 唯一约束
        Set<String> seen = new HashSet<>();
        int order = 0;
        for (FeatureItemDTO item : items) {
            if (item == null || item.getRefId() == null) {
                continue;
            }
            String refId = item.getRefId().trim();
            if (refId.isEmpty() || !seen.add(refId)) {
                continue;
            }
            HomepageFeature feature = new HomepageFeature();
            feature.setSectionKey(section);
            feature.setRefId(refId);
            feature.setSortOrder(order++);
            feature.setCreateTime(LocalDateTime.now());
            homepageFeatureMapper.insert(feature);
        }
    }

    // ===== 候选内容 =====

    private List<Object> loadCandidates(String section) {
        switch (section) {
            case "finance":
                return new ArrayList<>(buildFinanceVOs(financingProductMapper.selectList(
                        new QueryWrapper<FinancingProduct>().orderByDesc("create_time"))));
            case "expert":
                return new ArrayList<>(expertService.listAll());
            case "answer":
                return new ArrayList<>(questionMapper.selectList(
                        new QueryWrapper<Question>().eq("status", 1).orderByDesc("id")));
            case "knowledge":
                return new ArrayList<>(knowledgeMapper.selectList(
                        new QueryWrapper<Knowledge>().orderByDesc("create_time")));
            case "goods":
                return new ArrayList<>(goodsMapper.selectList(
                        new QueryWrapper<Goods>().eq("type", "1").orderByDesc("create_time")));
            default:
                return new ArrayList<>();
        }
    }

    private List<FeatureItemDTO> loadSelected(String section) {
        QueryWrapper<HomepageFeature> wrapper = new QueryWrapper<>();
        wrapper.eq("section_key", section).orderByAsc("sort_order");
        return homepageFeatureMapper.selectList(wrapper).stream().map(f -> {
            FeatureItemDTO dto = new FeatureItemDTO();
            dto.setRefId(f.getRefId());
            dto.setSortOrder(f.getSortOrder());
            return dto;
        }).collect(Collectors.toList());
    }

    // ===== 各板块解析 =====

    private List<FinanceFeatureVO> resolveFinance(List<HomepageFeature> features) {
        List<String> refIds = orderedRefIds(features);
        if (refIds.isEmpty()) {
            return new ArrayList<>();
        }
        List<Integer> intIds = parseIntIds(refIds);
        if (intIds.isEmpty()) {
            return new ArrayList<>();
        }
        Map<Integer, FinancingProduct> byId = financingProductMapper.selectBatchIds(intIds).stream()
                .collect(Collectors.toMap(FinancingProduct::getId, p -> p, (a, b) -> a));
        Map<Integer, String> bankNames = bankNameMap();
        List<FinanceFeatureVO> result = new ArrayList<>();
        for (String refId : refIds) {
            FinancingProduct product = getById(byId, refId);
            if (product == null) {
                continue;
            }
            result.add(toFinanceVO(product, bankNames));
        }
        return result;
    }

    private List<Expert> resolveExpert(List<HomepageFeature> features) {
        List<String> userNames = orderedRefIds(features);
        if (userNames.isEmpty()) {
            return new ArrayList<>();
        }
        // listAll() 已与 tb_user 关联并过滤审批通过的专家
        Map<String, Expert> byName = expertService.listAll().stream()
                .collect(Collectors.toMap(Expert::getUserName, e -> e, (a, b) -> a));
        List<Expert> result = new ArrayList<>();
        for (String userName : userNames) {
            Expert expert = byName.get(userName);
            if (expert != null) {
                result.add(expert);
            }
        }
        return result;
    }

    /**
     * 通用：按数字主键解析并保持精选顺序，跳过已删除的引用。
     */
    private <T> List<T> resolveByIntIds(List<HomepageFeature> features,
                                        Function<List<Integer>, List<T>> fetcher,
                                        Function<T, Integer> idGetter) {
        List<String> refIds = orderedRefIds(features);
        if (refIds.isEmpty()) {
            return new ArrayList<>();
        }
        List<Integer> intIds = parseIntIds(refIds);
        if (intIds.isEmpty()) {
            return new ArrayList<>();
        }
        Map<Integer, T> byId = fetcher.apply(intIds).stream()
                .collect(Collectors.toMap(idGetter, t -> t, (a, b) -> a));
        List<T> result = new ArrayList<>();
        for (String refId : refIds) {
            T entity = getById(byId, refId);
            if (entity != null) {
                result.add(entity);
            }
        }
        return result;
    }

    // ===== 工具方法 =====

    private List<FinanceFeatureVO> buildFinanceVOs(List<FinancingProduct> products) {
        Map<Integer, String> bankNames = bankNameMap();
        return products.stream().map(p -> toFinanceVO(p, bankNames)).collect(Collectors.toList());
    }

    private FinanceFeatureVO toFinanceVO(FinancingProduct product, Map<Integer, String> bankNames) {
        FinanceFeatureVO vo = new FinanceFeatureVO();
        vo.setId(product.getId());
        vo.setBankName(bankNames.getOrDefault(product.getBankId(), "金融服务"));
        vo.setName(product.getName());
        vo.setMaxQuota(product.getMaxQuota());
        vo.setMinQuota(product.getMinQuota());
        vo.setRate(product.getRate());
        vo.setMaxTerm(product.getMaxTerm());
        vo.setMinTerm(product.getMinTerm());
        vo.setDescription(product.getDescription());
        return vo;
    }

    private Map<Integer, String> bankNameMap() {
        return bankMapper.selectList(null).stream()
                .filter(b -> b.getBankId() != null)
                .collect(Collectors.toMap(Bank::getBankId, b -> b.getBankName() == null ? "" : b.getBankName(),
                        (a, b) -> a));
    }

    private List<String> orderedRefIds(List<HomepageFeature> features) {
        return features.stream()
                .sorted(Comparator.comparingInt(f -> f.getSortOrder() == null ? 0 : f.getSortOrder()))
                .map(HomepageFeature::getRefId)
                .collect(Collectors.toList());
    }

    private List<Integer> parseIntIds(List<String> refIds) {
        List<Integer> ids = new ArrayList<>();
        for (String refId : refIds) {
            try {
                ids.add(Integer.valueOf(refId.trim()));
            } catch (NumberFormatException ignored) {
                // 非数字主键（理论上不应出现在这些板块）跳过
            }
        }
        return ids;
    }

    private <T> T getById(Map<Integer, T> map, String refId) {
        try {
            return map.get(Integer.valueOf(refId.trim()));
        } catch (NumberFormatException ignored) {
            return null;
        }
    }
}
