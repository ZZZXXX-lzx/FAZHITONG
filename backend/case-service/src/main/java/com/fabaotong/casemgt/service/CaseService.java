package com.fabaotong.casemgt.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fabaotong.common.dto.PageParam;
import com.fabaotong.common.dto.PageResult;
import com.fabaotong.casemgt.entity.CaseGovernment;
import com.fabaotong.casemgt.mapper.CaseGovernmentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CaseService {

    private final CaseGovernmentMapper caseMapper;

    public PageResult<CaseGovernment> search(String keyword, String causeName,
                                              String courtName, String caseYear,
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
        if (courtName != null && !courtName.isBlank()) wrapper.eq(CaseGovernment::getCourtName, courtName);
        if (caseYear != null && !caseYear.isBlank()) wrapper.eq(CaseGovernment::getCaseYear, caseYear);

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
        return caseMapper.selectById(id);
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
