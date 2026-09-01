package com.fazhitong.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fazhitong.common.exception.BusinessException;
import com.fazhitong.user.entity.ComplianceAnswer;
import com.fazhitong.user.entity.ComplianceQuestion;
import com.fazhitong.user.entity.ComplianceReport;
import com.fazhitong.user.mapper.ComplianceAnswerMapper;
import com.fazhitong.user.mapper.ComplianceQuestionMapper;
import com.fazhitong.user.mapper.ComplianceReportMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ComplianceService {

    private final ComplianceQuestionMapper questionMapper;
    private final ComplianceAnswerMapper answerMapper;
    private final ComplianceReportMapper reportMapper;
    private final ObjectMapper objectMapper;

    /** 领域枚举与中文名映射 */
    public static final Map<String, String> DOMAIN_NAMES = new LinkedHashMap<>();
    static {
        DOMAIN_NAMES.put("LABOR", "劳动用工");
        DOMAIN_NAMES.put("CONTRACT", "合同管理");
        DOMAIN_NAMES.put("IP", "知识产权");
        DOMAIN_NAMES.put("DATA", "数据合规");
        DOMAIN_NAMES.put("TAX", "财税合规");
    }

    /** 领域对应的基础整改建议（得分低时给出） */
    private static final Map<String, String> DOMAIN_SUGGESTIONS = new LinkedHashMap<>();
    static {
        DOMAIN_SUGGESTIONS.put("LABOR", "规范劳动合同签订与社保缴纳，建立完善的人事管理制度，避免用工纠纷。");
        DOMAIN_SUGGESTIONS.put("CONTRACT", "完善合同签订审批流程，加强履约跟踪与档案管理，防范违约风险。");
        DOMAIN_SUGGESTIONS.put("IP", "梳理商标、专利、版权等知识产权资产，及时登记确权并监控侵权。");
        DOMAIN_SUGGESTIONS.put("DATA", "建立个人信息保护与数据安全制度，完善隐私政策与数据分级管理。");
        DOMAIN_SUGGESTIONS.put("TAX", "规范纳税申报与发票管理，合理进行税务筹划，确保依法合规纳税。");
    }

    /**
     * 获取问卷：按领域分组返回启用的题目，附带已有草稿答案。
     */
    public Map<String, Object> getQuestions(Long enterpriseId) {
        List<ComplianceQuestion> questions = questionMapper.selectList(
                new LambdaQueryWrapper<ComplianceQuestion>()
                        .eq(ComplianceQuestion::getStatus, 1)
                        .orderByAsc(ComplianceQuestion::getSort)
                        .orderByAsc(ComplianceQuestion::getId));

        Map<Long, String> existing = new HashMap<>();
        if (enterpriseId != null) {
            List<ComplianceAnswer> answers = answerMapper.selectList(
                    new LambdaQueryWrapper<ComplianceAnswer>()
                            .eq(ComplianceAnswer::getEnterpriseId, enterpriseId)
                            .eq(ComplianceAnswer::getStatus, 0));
            for (ComplianceAnswer a : answers) {
                existing.put(a.getQuestionId(), a.getAnswer());
            }
        }

        List<Map<String, Object>> domains = new ArrayList<>();
        Map<String, List<ComplianceQuestion>> grouped = questions.stream()
                .collect(Collectors.groupingBy(
                        q -> q.getDomain() == null ? "LABOR" : q.getDomain(),
                        LinkedHashMap::new, Collectors.toList()));

        for (Map.Entry<String, List<ComplianceQuestion>> entry : grouped.entrySet()) {
            String domain = entry.getKey();
            List<Map<String, Object>> items = entry.getValue().stream().map(q -> {
                Map<String, Object> m = new HashMap<>();
                m.put("id", q.getId());
                m.put("content", q.getContent());
                m.put("weight", q.getWeight());
                m.put("answer", existing.getOrDefault(q.getId(), null));
                return m;
            }).collect(Collectors.toList());

            Map<String, Object> d = new HashMap<>();
            d.put("domain", domain);
            d.put("name", DOMAIN_NAMES.getOrDefault(domain, domain));
            d.put("questions", items);
            domains.add(d);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("domains", domains);
        result.put("totalQuestions", questions.size());
        return result;
    }

    /**
     * 保存答案。submit=false 存草稿，submit=true 触发评分并生成报告。
     */
    public Map<String, Object> submitAnswers(Long enterpriseId, List<Map<String, String>> answers, boolean submit) {
        if (enterpriseId == null) {
            throw new BusinessException("企业信息缺失");
        }
        int targetStatus = submit ? 1 : 0;
        for (Map<String, String> item : answers) {
            Long questionId = Long.valueOf(item.get("questionId"));
            String answer = item.get("answer");
            if (!"YES".equals(answer) && !"NO".equals(answer) && !"NA".equals(answer)) {
                throw new BusinessException("非法的答案选项");
            }
            ComplianceAnswer existing = answerMapper.selectOne(
                    new LambdaQueryWrapper<ComplianceAnswer>()
                            .eq(ComplianceAnswer::getEnterpriseId, enterpriseId)
                            .eq(ComplianceAnswer::getQuestionId, questionId));
            if (existing != null) {
                existing.setAnswer(answer);
                existing.setStatus(targetStatus);
                answerMapper.updateById(existing);
            } else {
                ComplianceAnswer a = new ComplianceAnswer();
                a.setEnterpriseId(enterpriseId);
                a.setQuestionId(questionId);
                a.setAnswer(answer);
                a.setStatus(targetStatus);
                answerMapper.insert(a);
            }
        }

        Map<String, Object> result = new HashMap<>();
        if (submit) {
            result.put("report", generateReport(enterpriseId));
        } else {
            result.put("saved", true);
        }
        return result;
    }

    /**
     * 生成合规报告：按领域评分、判定等级、汇总高风险项与建议，并落库。
     */
    public Map<String, Object> generateReport(Long enterpriseId) {
        List<ComplianceQuestion> questions = questionMapper.selectList(
                new LambdaQueryWrapper<ComplianceQuestion>().eq(ComplianceQuestion::getStatus, 1));
        if (questions.isEmpty()) {
            throw new BusinessException("题库为空，无法生成报告");
        }

        List<ComplianceAnswer> answers = answerMapper.selectList(
                new LambdaQueryWrapper<ComplianceAnswer>()
                        .eq(ComplianceAnswer::getEnterpriseId, enterpriseId)
                        .eq(ComplianceAnswer::getStatus, 1));
        Map<Long, String> answerMap = answers.stream()
                .collect(Collectors.toMap(ComplianceAnswer::getQuestionId, ComplianceAnswer::getAnswer, (a, b) -> b));

        // 按领域聚合得分
        Map<String, int[]> domainAgg = new LinkedHashMap<>(); // domain -> [得分, 满分]
        Map<String, List<Map<String, Object>>> domainHighRisk = new LinkedHashMap<>();
        for (ComplianceQuestion q : questions) {
            String domain = q.getDomain() == null ? "LABOR" : q.getDomain();
            int weight = q.getWeight() == null ? 5 : q.getWeight();
            String ans = answerMap.get(q.getId());
            int[] agg = domainAgg.computeIfAbsent(domain, k -> new int[2]);
            if ("NA".equals(ans)) {
                // 不适用不计入分母
                continue;
            }
            agg[1] += weight;
            if ("YES".equals(ans)) {
                agg[0] += weight;
            } else if ("NO".equals(ans) || ans == null) {
                // 未作答按否处理，计入高风险项
                Map<String, Object> item = new HashMap<>();
                item.put("content", q.getContent());
                item.put("domain", domain);
                item.put("domainName", DOMAIN_NAMES.getOrDefault(domain, domain));
                domainHighRisk.computeIfAbsent(domain, k -> new ArrayList<>()).add(item);
            }
        }

        List<Map<String, Object>> domainScores = new ArrayList<>();
        double totalWeighted = 0;
        int scoredDomains = 0;
        for (Map.Entry<String, int[]> e : domainAgg.entrySet()) {
            String domain = e.getKey();
            int[] agg = e.getValue();
            int score = agg[1] == 0 ? 100 : (int) Math.round(agg[0] * 100.0 / agg[1]);
            Map<String, Object> d = new HashMap<>();
            d.put("domain", domain);
            d.put("name", DOMAIN_NAMES.getOrDefault(domain, domain));
            d.put("score", score);
            d.put("level", levelOf(score));
            domainScores.add(d);
            totalWeighted += score;
            scoredDomains++;
        }
        int totalScore = scoredDomains == 0 ? 0 : (int) Math.round(totalWeighted / scoredDomains);

        // 汇总高风险项与建议
        List<Map<String, Object>> highRiskItems = new ArrayList<>();
        List<String> suggestions = new ArrayList<>();
        for (Map.Entry<String, List<Map<String, Object>>> e : domainHighRisk.entrySet()) {
            String domain = e.getKey();
            highRiskItems.addAll(e.getValue());
            if (!suggestions.contains(DOMAIN_SUGGESTIONS.getOrDefault(domain, ""))) {
                suggestions.add(DOMAIN_SUGGESTIONS.getOrDefault(domain, "建议针对该领域进行专项合规整改。"));
            }
        }
        if (suggestions.isEmpty()) {
            suggestions.add("各项合规指标表现良好，建议持续保持并定期复查。");
        }

        Map<String, Object> report = new HashMap<>();
        report.put("totalScore", totalScore);
        report.put("level", levelOf(totalScore));
        report.put("domains", domainScores);
        report.put("highRiskItems", highRiskItems);
        report.put("suggestions", suggestions);

        // 落库
        try {
            ComplianceReport cr = new ComplianceReport();
            cr.setEnterpriseId(enterpriseId);
            cr.setReportType("QUESTIONNAIRE");
            cr.setScore(totalScore);
            cr.setReportData(objectMapper.writeValueAsString(report));
            cr.setStatus(1);
            reportMapper.insert(cr);
        } catch (Exception ignored) {
            // 落库失败不影响返回报告
        }
        return report;
    }

    /**
     * 获取最近一次报告
     */
    public Map<String, Object> latestReport(Long enterpriseId) {
        ComplianceReport report = reportMapper.selectOne(
                new LambdaQueryWrapper<ComplianceReport>()
                        .eq(ComplianceReport::getEnterpriseId, enterpriseId)
                        .eq(ComplianceReport::getStatus, 1)
                        .orderByDesc(ComplianceReport::getCreateTime)
                        .last("LIMIT 1"));
        if (report == null) {
            throw new BusinessException("暂无合规报告");
        }
        try {
            Map<String, Object> data = objectMapper.readValue(report.getReportData(), Map.class);
            data.put("createTime", report.getCreateTime() == null ? "" : report.getCreateTime().toString());
            return data;
        } catch (Exception e) {
            throw new BusinessException("报告数据解析失败");
        }
    }

    private String levelOf(int score) {
        if (score >= 80) return "LOW";
        if (score >= 60) return "MEDIUM";
        return "HIGH";
    }
}
