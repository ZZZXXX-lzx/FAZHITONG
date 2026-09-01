package com.fazhitong.contract.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fazhitong.common.ai.AiClient;
import com.fazhitong.common.dto.PageParam;
import com.fazhitong.common.dto.PageResult;
import com.fazhitong.common.exception.BusinessException;
import com.fazhitong.contract.entity.ContractRecord;
import com.fazhitong.contract.entity.ContractReviewRisk;
import com.fazhitong.contract.entity.EnterpriseContract;
import com.fazhitong.contract.mapper.ContractRecordMapper;
import com.fazhitong.contract.mapper.ContractReviewRiskMapper;
import com.fazhitong.contract.mapper.EnterpriseContractMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ContractService {

    private static final ObjectMapper JSON = new ObjectMapper();

    /** 审查维度中文名 */
    private static final Map<String, String> DIMENSION_NAMES = new LinkedHashMap<>();
    static {
        DIMENSION_NAMES.put("GENERAL", "通用审查");
        DIMENSION_NAMES.put("LABOR", "劳动用工");
        DIMENSION_NAMES.put("SALE", "买卖交易");
        DIMENSION_NAMES.put("LEASE", "租赁合同");
        DIMENSION_NAMES.put("EQUITY", "股权协议");
    }

    /** 让大模型输出 JSON 结构化风险点 */
    private static final String CONTRACT_REVIEW_SYSTEM =
            "你是资深企业法务合同审查专家。请对用户提供的合同文本进行风险审查，"
                    + "并严格按照以下 JSON 数组格式输出，不要输出任何多余文字：\n"
                    + "[{\"level\":\"HIGH|MEDIUM|LOW\",\"clause\":\"条款原文片段\",\"description\":\"风险说明\","
                    + "\"suggestion\":\"修改建议\",\"legalBasis\":\"法律依据\"}]\n"
                    + "要求：风险点应覆盖违约责任、解除条款、知识产权、保密、竞业、付款、交付、争议解决等维度；"
                    + "legalBasis 只能引用真实存在的法律条文，无法确定时填写空字符串。";

    private final ContractRecordMapper recordMapper;
    private final EnterpriseContractMapper enterpriseContractMapper;
    private final ContractReviewRiskMapper riskMapper;

    public ContractRecord uploadReview(Long userId, Long enterpriseId, String title, String fileUrl) {
        ContractRecord record = new ContractRecord();
        record.setUserId(userId);
        record.setEnterpriseId(enterpriseId);
        record.setTitle(title);
        record.setFileUrl(fileUrl);
        record.setStatus(0);
        recordMapper.insert(record);
        return record;
    }

    public ContractRecord reviewResult(Long id, String riskReport, String riskLevel) {
        ContractRecord record = recordMapper.selectById(id);
        if (record == null) {
            throw new BusinessException("合同审查记录不存在");
        }
        record.setRiskReport(riskReport);
        record.setRiskLevel(riskLevel);
        record.setStatus(1);
        record.setReviewTime(LocalDateTime.now());
        recordMapper.updateById(record);
        return record;
    }

    public PageResult<ContractRecord> listRecords(Long userId, Long enterpriseId, PageParam pageParam) {
        LambdaQueryWrapper<ContractRecord> wrapper = new LambdaQueryWrapper<ContractRecord>()
                .orderByDesc(ContractRecord::getCreateTime);
        if (userId != null) wrapper.eq(ContractRecord::getUserId, userId);
        if (enterpriseId != null) wrapper.eq(ContractRecord::getEnterpriseId, enterpriseId);
        Page<ContractRecord> page = recordMapper.selectPage(new Page<>(pageParam.getPage(), pageParam.getSize()), wrapper);
        return PageResult.of(page.getRecords(), page.getTotal(), (int) page.getCurrent(), (int) page.getSize());
    }

    public EnterpriseContract createEnterpriseContract(EnterpriseContract contract) {
        if (contract.getStatus() == null || contract.getStatus().isBlank()) {
            contract.setStatus("DRAFT");
        }
        enterpriseContractMapper.insert(contract);
        return contract;
    }

    public PageResult<EnterpriseContract> listEnterpriseContracts(Long enterpriseId, PageParam pageParam) {
        Page<EnterpriseContract> page = enterpriseContractMapper.selectPage(
                new Page<>(pageParam.getPage(), pageParam.getSize()),
                new LambdaQueryWrapper<EnterpriseContract>()
                        .eq(EnterpriseContract::getEnterpriseId, enterpriseId)
                        .orderByDesc(EnterpriseContract::getCreateTime));
        for (EnterpriseContract c : page.getRecords()) {
            c.setDaysToExpire(daysToExpire(c.getExpireDate()));
        }
        return PageResult.of(page.getRecords(), page.getTotal(), (int) page.getCurrent(), (int) page.getSize());
    }

    /**
     * 合同状态流转：提交签署、签署、归档、作废。
     * 电子签章：签署时生成签章凭据号并记录签署人与时间。
     */
    public EnterpriseContract transition(Long id, String action, String signerName) {
        EnterpriseContract c = enterpriseContractMapper.selectById(id);
        if (c == null) {
            throw new BusinessException("合同不存在");
        }
        switch (action) {
            case "SUBMIT" -> c.setStatus("PENDING_SIGN");
            case "SIGN" -> {
                if (!"PENDING_SIGN".equals(c.getStatus()) && !"DRAFT".equals(c.getStatus())) {
                    throw new BusinessException("当前状态不可签署");
                }
                c.setStatus("SIGNED");
                c.setSignerName(signerName == null || signerName.isBlank() ? "企业签章" : signerName);
                c.setSignCertNo(generateCertNo(c));
                c.setSignTime(LocalDateTime.now());
            }
            case "ARCHIVE" -> {
                if (!"SIGNED".equals(c.getStatus())) {
                    throw new BusinessException("仅已签署合同可归档");
                }
                c.setStatus("ARCHIVED");
                c.setArchiveTime(LocalDateTime.now());
            }
            case "VOID" -> c.setStatus("VOID");
            default -> throw new BusinessException("非法的状态操作");
        }
        enterpriseContractMapper.updateById(c);
        return c;
    }

    /**
     * 到期预警：查询某企业距离到期不足 days 天的合同。
     */
    public List<EnterpriseContract> listExpiring(Long enterpriseId, int days) {
        LocalDateTime threshold = LocalDateTime.now().plusDays(days);
        List<EnterpriseContract> list = enterpriseContractMapper.selectList(
                new LambdaQueryWrapper<EnterpriseContract>()
                        .eq(EnterpriseContract::getEnterpriseId, enterpriseId)
                        .in(EnterpriseContract::getStatus, "SIGNED", "PENDING_SIGN")
                        .isNotNull(EnterpriseContract::getExpireDate)
                        .le(EnterpriseContract::getExpireDate, threshold)
                        .orderByAsc(EnterpriseContract::getExpireDate));
        for (EnterpriseContract c : list) {
            c.setDaysToExpire(daysToExpire(c.getExpireDate()));
        }
        return list;
    }

    /** 生成电子签章凭据号：FZT + 时间戳 + 合同序号，模拟 CA 证书编号 */
    private String generateCertNo(EnterpriseContract c) {
        long ts = System.currentTimeMillis();
        long seq = (c.getId() == null ? 0 : c.getId()) % 10000;
        return String.format("FZT-%d-%04d", ts, seq);
    }

    private Integer daysToExpire(LocalDateTime expireDate) {
        if (expireDate == null) return null;
        return (int) java.time.Duration.between(LocalDateTime.now(), expireDate).toDays();
    }

    /**
     * 合同智能审查：优先调用大模型输出结构化风险点，未配置或解析失败时降级为规则引擎。
     * 审查结果写入 contract_record 与 contract_review_risk，供历史追溯。
     */
    public Map<String, Object> aiReview(String contractText, String dimension, Long userId, Long enterpriseId) {
        String dim = DIMENSION_NAMES.containsKey(dimension) ? dimension : "GENERAL";
        List<ContractReviewRisk> risks = new ArrayList<>();
        boolean aiMode = false;

        if (AiClient.isEnabled()) {
            String raw = AiClient.chat(CONTRACT_REVIEW_SYSTEM, "审查维度：" + DIMENSION_NAMES.get(dim)
                    + "\n\n合同文本：\n" + contractText);
            List<ContractReviewRisk> parsed = parseStructuredRisks(raw);
            if (parsed != null) {
                risks = parsed;
                aiMode = true;
            }
        }
        if (risks.isEmpty()) {
            risks = heuristicRisks(contractText, dim);
            aiMode = false;
        }

        // 落库
        ContractRecord record = new ContractRecord();
        record.setUserId(userId);
        record.setEnterpriseId(enterpriseId);
        record.setTitle("合同智能审查 · " + DIMENSION_NAMES.get(dim));
        record.setRiskLevel(evaluateRiskLevel(risks));
        record.setRiskReport(buildReportText(risks));
        record.setStatus(1);
        record.setReviewTime(LocalDateTime.now());
        recordMapper.insert(record);
        for (ContractReviewRisk r : risks) {
            r.setTaskId(record.getId());
            riskMapper.insert(r);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("taskId", record.getId());
        result.put("riskLevel", record.getRiskLevel());
        result.put("mode", aiMode ? "AI" : "RULE");
        result.put("risks", risks);
        return result;
    }

    /**
     * 查询历史审查任务的风险点
     */
    public List<ContractReviewRisk> listRisks(Long taskId) {
        return riskMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ContractReviewRisk>()
                        .eq(ContractReviewRisk::getTaskId, taskId));
    }

    /** 解析大模型返回的 JSON 风险数组，解析失败返回 null */
    private List<ContractReviewRisk> parseStructuredRisks(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            String json = raw.trim();
            int start = json.indexOf('[');
            int end = json.lastIndexOf(']');
            if (start < 0 || end <= start) return null;
            JsonNode arr = JSON.readTree(json.substring(start, end + 1));
            if (!arr.isArray()) return null;
            List<ContractReviewRisk> risks = new ArrayList<>();
            for (JsonNode node : arr) {
                ContractReviewRisk r = new ContractReviewRisk();
                r.setLevel(normalizeLevel(node.path("level").asText("LOW")));
                r.setClause(node.path("clause").asText(""));
                r.setDescription(node.path("description").asText(""));
                r.setSuggestion(node.path("suggestion").asText(""));
                r.setLegalBasis(node.path("legalBasis").asText(""));
                if (!r.getDescription().isBlank()) {
                    risks.add(r);
                }
            }
            return risks.isEmpty() ? null : risks;
        } catch (Exception e) {
            return null;
        }
    }

    /** 关键词规则引擎，返回结构化风险点 */
    private List<ContractReviewRisk> heuristicRisks(String text, String dimension) {
        String t = text == null ? "" : text;
        List<ContractReviewRisk> risks = new ArrayList<>();
        addRuleRisk(risks, t, "违约金", "MEDIUM",
                "合同约定了违约金，请核对违约金比例是否过高。",
                "若违约金超过实际损失的 30%，可主张法院予以调减。",
                "《民法典》第 585 条");
        addRuleRisk(risks, t, "解除", "MEDIUM",
                "存在合同解除条款，需确认解除条件与通知期限是否明确。",
                "建议明确解除权的行使条件、通知期限与善后安排。",
                "《民法典》第 563 条");
        addRuleRisk(risks, t, "知识产权", "HIGH",
                "涉及知识产权，权属与使用范围可能存在争议。",
                "建议明确知识产权的权属、使用范围与开发成果归属。",
                "《民法典》第 123 条");
        addRuleRisk(risks, t, "保密", "MEDIUM",
                "存在保密条款，需关注保密范围与期限。",
                "建议明确保密范围、保密期限与违约责任。",
                "《民法典》第 501 条");
        addRuleRisk(risks, t, "竞业", "HIGH",
                "存在竞业限制，需核对补偿标准与期限。",
                "建议按法律规定约定竞业限制的补偿与期限。",
                "《劳动合同法》第 23、24 条");
        addRuleRisk(risks, t, "争议", "LOW",
                "争议解决条款可能约定不明。",
                "建议明确约定管辖法院或仲裁机构。",
                "《民事诉讼法》第 34 条");
        addRuleRisk(risks, t, "付款", "MEDIUM",
                "付款节点与逾期责任可能约定不明。",
                "建议明确付款节点、方式与逾期付款责任。",
                "《民法典》第 626 条");
        addRuleRisk(risks, t, "交付", "MEDIUM",
                "交付时间与验收标准可能约定不明。",
                "建议明确交付时间、地点、验收标准与迟延责任。",
                "《民法典》第 604 条");
        return risks;
    }

    private void addRuleRisk(List<ContractReviewRisk> risks, String text, String keyword,
                             String level, String description, String suggestion, String basis) {
        if (text.contains(keyword)) {
            ContractReviewRisk r = new ContractReviewRisk();
            r.setLevel(level);
            r.setClause(keyword);
            r.setDescription(description);
            r.setSuggestion(suggestion);
            r.setLegalBasis(basis);
            risks.add(r);
        }
    }

    private String normalizeLevel(String level) {
        String l = level == null ? "LOW" : level.toUpperCase();
        if ("HIGH".equals(l) || "MEDIUM".equals(l)) return l;
        return "LOW";
    }

    private String evaluateRiskLevel(List<ContractReviewRisk> risks) {
        int high = 0, medium = 0;
        for (ContractReviewRisk r : risks) {
            if ("HIGH".equals(r.getLevel())) high++;
            else if ("MEDIUM".equals(r.getLevel())) medium++;
        }
        if (high > 0) return "高";
        if (medium >= 2) return "中";
        if (medium >= 1) return "中";
        return risks.isEmpty() ? "低" : "低";
    }

    private String buildReportText(List<ContractReviewRisk> risks) {
        if (risks.isEmpty()) {
            return "已完成基础审查，未发现明显高风险条款。";
        }
        StringBuilder sb = new StringBuilder("已完成审查，识别到以下风险点：\n");
        for (int i = 0; i < risks.size(); i++) {
            ContractReviewRisk r = risks.get(i);
            sb.append(i + 1).append(". [").append(riskLevelText(r.getLevel())).append("] ")
                    .append(r.getDescription()).append("\n");
        }
        return sb.toString();
    }

    private String riskLevelText(String level) {
        if ("HIGH".equals(level)) return "高风险";
        if ("MEDIUM".equals(level)) return "中风险";
        return "低风险";
    }
}
