package com.fazhitong.casemgt.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fazhitong.common.ai.AiClient;
import com.fazhitong.common.dto.PageParam;
import com.fazhitong.common.dto.PageResult;
import com.fazhitong.casemgt.entity.CaseGovernment;
import com.fazhitong.casemgt.mapper.CaseGovernmentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CaseService {

    private static final String FOCUS_SYSTEM =
            "你是法律案例分析师。请根据裁判文书内容提炼两个要点："
                    + "1）争议焦点；2）判决依据（法院援引的法条或裁判规则）。"
                    + "严格按 JSON 输出：{\"focus\":\"争议焦点\",\"basis\":\"判决依据\"}，"
                    + "不要输出多余文字，无法提炼的字段填空字符串。";

    private final CaseGovernmentMapper caseMapper;

    public PageResult<CaseGovernment> search(String keyword, String causeName,
                                              String courtName, String caseYear,
                                              String lawArticle, String courtLevel,
                                              PageParam pageParam) {
        LambdaQueryWrapper<CaseGovernment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CaseGovernment::getStatus, 1);
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w
                    .like(CaseGovernment::getCauseName, keyword)
                    .or().like(CaseGovernment::getKeywords, keyword)
                    .or().like(CaseGovernment::getAbstractText, keyword)
                    .or().like(CaseGovernment::getFocusPoints, keyword)
                    .or().like(CaseGovernment::getFullText, keyword));
        }
        if (causeName != null && !causeName.isBlank()) wrapper.eq(CaseGovernment::getCauseName, causeName);
        if (courtName != null && !courtName.isBlank()) wrapper.like(CaseGovernment::getCourtName, courtName);
        if (caseYear != null && !caseYear.isBlank()) wrapper.eq(CaseGovernment::getCaseYear, caseYear);
        // 法条筛选：匹配全文或判决依据
        if (lawArticle != null && !lawArticle.isBlank()) {
            wrapper.and(w -> w
                    .like(CaseGovernment::getJudgmentBasis, lawArticle)
                    .or().like(CaseGovernment::getFullText, lawArticle));
        }
        // 法院层级筛选：按法院名称关键词匹配
        if (courtLevel != null && !courtLevel.isBlank()) {
            String prefix = levelPrefix(courtLevel);
            if (prefix != null) {
                wrapper.like(CaseGovernment::getCourtName, prefix);
            }
        }

        // Use database-level pagination for efficiency
        Page<CaseGovernment> page = caseMapper.selectPage(
                new Page<>(pageParam.getPage(), pageParam.getSize()), wrapper);

        List<CaseGovernment> records = page.getRecords();

        // Score results on current page for relevance ranking
        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.toLowerCase();
            for (CaseGovernment cg : records) {
                int count = 0;
                count += countOccurrences(cg.getFullText(), kw);
                count += countOccurrences(cg.getAbstractText(), kw);
                count += countOccurrences(cg.getFocusPoints(), kw);
                count += countOccurrences(cg.getCauseName(), kw);
                count += countOccurrences(cg.getKeywords(), kw);
                count += countOccurrences(cg.getJudgmentResult(), kw);
                cg.setScore((double) count);
            }
            records.sort(Comparator.comparingDouble(
                    (CaseGovernment cg) -> cg.getScore() != null ? cg.getScore() : 0).reversed());
        } else {
            records.forEach(cg -> cg.setScore(0.0));
        }

        return PageResult.of(records, page.getTotal(), (int) page.getCurrent(), (int) page.getSize());
    }

    public CaseGovernment getById(Long id) {
        CaseGovernment cg = caseMapper.selectById(id);
        if (cg == null) {
            return null;
        }
        // 判决依据为空时，尝试用大模型提炼并缓存
        if ((cg.getJudgmentBasis() == null || cg.getJudgmentBasis().isBlank())
                && cg.getFullText() != null && !cg.getFullText().isBlank()
                && AiClient.isEnabled()) {
            String[] extracted = extractFocusAndBasis(cg.getFullText());
            if (extracted != null) {
                if (extracted[0] != null && !extracted[0].isBlank()) {
                    cg.setFocusPoints(extracted[0]);
                }
                if (extracted[1] != null && !extracted[1].isBlank()) {
                    cg.setJudgmentBasis(extracted[1]);
                    caseMapper.updateById(cg);
                }
            }
        }
        return cg;
    }

    /** 法院层级 → 法院名称关键词 */
    private String levelPrefix(String courtLevel) {
        switch (courtLevel.toUpperCase()) {
            case "SUPREME": return "最高";
            case "HIGH": return "高级";
            case "INTERMEDIATE": return "中级";
            case "BASE": return "基层";
            default: return null;
        }
    }

    /** 提炼争议焦点与判决依据，返回 [focus, basis]，失败返回 null */
    private String[] extractFocusAndBasis(String fullText) {
        String raw = AiClient.chat(FOCUS_SYSTEM, fullText);
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode node = mapper.readTree(raw.trim());
            String focus = node.path("focus").asText("");
            String basis = node.path("basis").asText("");
            return new String[]{focus, basis};
        } catch (Exception e) {
            return null;
        }
    }

    private int countOccurrences(String text, String keyword) {
        if (text == null || keyword.isEmpty()) return 0;
        int count = 0;
        int idx = 0;
        String lower = text.toLowerCase();
        while ((idx = lower.indexOf(keyword, idx)) != -1) {
            count++;
            idx += keyword.length();
        }
        return count;
    }
}
