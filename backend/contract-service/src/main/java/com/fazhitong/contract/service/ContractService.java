package com.fazhitong.contract.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fazhitong.common.ai.AiClient;
import com.fazhitong.common.dto.PageParam;
import com.fazhitong.common.dto.PageResult;
import com.fazhitong.common.exception.BusinessException;
import com.fazhitong.contract.entity.ContractRecord;
import com.fazhitong.contract.entity.EnterpriseContract;
import com.fazhitong.contract.mapper.ContractRecordMapper;
import com.fazhitong.contract.mapper.EnterpriseContractMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ContractService {

    private static final String CONTRACT_REVIEW_SYSTEM =
            "你是资深企业法务合同审查专家。请对用户提供的合同文本进行风险审查，"
                    + "输出：1）整体风险评价；2）逐条列出风险点（条款位置、风险描述、修改建议、法律依据）；"
                    + "3）其他注意事项。用简洁的中文分条输出，不要编造法律条文。";

    private final ContractRecordMapper recordMapper;
    private final EnterpriseContractMapper enterpriseContractMapper;

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
        enterpriseContractMapper.insert(contract);
        return contract;
    }

    public PageResult<EnterpriseContract> listEnterpriseContracts(Long enterpriseId, PageParam pageParam) {
        Page<EnterpriseContract> page = enterpriseContractMapper.selectPage(
                new Page<>(pageParam.getPage(), pageParam.getSize()),
                new LambdaQueryWrapper<EnterpriseContract>()
                        .eq(EnterpriseContract::getEnterpriseId, enterpriseId)
                        .orderByDesc(EnterpriseContract::getCreateTime));
        return PageResult.of(page.getRecords(), page.getTotal(), (int) page.getCurrent(), (int) page.getSize());
    }

    /**
     * 合同智能审查：优先调用大模型，未配置或失败时降级为关键词规则审查。
     */
    public Map<String, Object> aiReview(String contractText) {
        String report = null;
        if (AiClient.isEnabled()) {
            report = AiClient.chat(CONTRACT_REVIEW_SYSTEM, contractText);
        }
        if (report == null || report.isBlank()) {
            report = heuristicReview(contractText);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("riskLevel", evaluateRiskLevel(report));
        result.put("report", report);
        return result;
    }

    private String heuristicReview(String text) {
        String t = text == null ? "" : text;
        List<String> risks = new ArrayList<>();
        addIf(risks, t, "违约金", "合同约定了违约金，请核对违约金比例是否过高（超过实际损失30%可能被法院调减）。");
        addIf(risks, t, "解除", "存在合同解除条款，请确认解除条件、通知期限与善后安排是否明确。");
        addIf(risks, t, "知识产权", "涉及知识产权，请确认权属、使用范围与后续开发成果归属是否清晰。");
        addIf(risks, t, "保密", "存在保密条款，请关注保密范围、期限与违约责任。");
        addIf(risks, t, "竞业", "存在竞业限制，请核对补偿标准与期限是否符合法律规定。");
        addIf(risks, t, "争议", "争议解决条款建议明确约定管辖法院或仲裁机构，避免管辖不明。");
        addIf(risks, t, "付款", "请确认付款节点、方式与逾期付款责任是否明确。");
        addIf(risks, t, "交付", "请确认交付时间、地点、验收标准与迟延交付责任。");
        if (risks.isEmpty()) {
            return "已完成基础审查，未发现明显高风险条款。建议仍由专业律师结合具体交易背景进一步审核。";
        }
        StringBuilder sb = new StringBuilder("已完成基础审查，识别到以下风险点：\n");
        for (int i = 0; i < risks.size(); i++) {
            sb.append(i + 1).append(". ").append(risks.get(i)).append("\n");
        }
        sb.append("\n（提示：以上为关键词规则识别结果，配置大模型后可得更精准意见。）");
        return sb.toString();
    }

    private void addIf(List<String> risks, String text, String keyword, String advice) {
        if (text.contains(keyword)) {
            risks.add(advice);
        }
    }

    private String evaluateRiskLevel(String report) {
        String r = report == null ? "" : report;
        int score = 0;
        for (String k : new String[]{"高风险", "严重", "重大", "无效", "违法", "禁止"}) {
            if (r.contains(k)) score += 3;
        }
        // 风险点数量（识别编号列表 1. / 1、 / 1））
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("(?m)^\\s*\\d+[.、）)]").matcher(r);
        while (m.find()) score++;
        for (String k : new String[]{"风险", "建议", "不明确", "缺失", "注意"}) {
            if (r.contains(k)) score += 1;
        }
        return score >= 6 ? "高" : (score >= 3 ? "中" : "低");
    }
}
