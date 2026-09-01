package com.fazhitong.casemgt.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fazhitong.casemgt.entity.KnowledgeArticle;
import com.fazhitong.casemgt.entity.Regulation;
import com.fazhitong.casemgt.mapper.KnowledgeArticleMapper;
import com.fazhitong.casemgt.mapper.RegulationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * RAG 检索增强底座的知识检索服务。
 * 当前以 MySQL 关键词检索作为检索后端（降级路径），后续可接入向量数据库实现语义检索，
 * 接口结构保持不变。检索结果附带来源引用，供咨询与审查模块复用。
 */
@Service
@RequiredArgsConstructor
public class KbService {

    private final KnowledgeArticleMapper articleMapper;
    private final RegulationMapper regulationMapper;

    /**
     * 检索知识片段。返回 Top-K 结果，每项含内容片段、来源类型/标题/引用、相关度评分。
     */
    public Map<String, Object> retrieve(String query, int topK) {
        if (query == null || query.isBlank()) {
            Map<String, Object> empty = new HashMap<>();
            empty.put("chunks", Collections.emptyList());
            empty.put("degraded", true);
            return empty;
        }
        int k = Math.max(1, Math.min(topK, 10));
        List<String> terms = tokenize(query);

        List<Map<String, Object>> chunks = new ArrayList<>();

        // 知识库文章召回
        List<KnowledgeArticle> articles = articleMapper.selectList(
                new LambdaQueryWrapper<KnowledgeArticle>().eq(KnowledgeArticle::getStatus, 1));
        for (KnowledgeArticle a : articles) {
            String haystack = (a.getTitle() == null ? "" : a.getTitle())
                    + " " + (a.getSummary() == null ? "" : a.getSummary())
                    + " " + (a.getContent() == null ? "" : a.getContent());
            double score = scoreOf(terms, haystack);
            if (score <= 0) continue;
            Map<String, Object> chunk = new HashMap<>();
            chunk.put("content", snippetOf(haystack, terms, 160));
            Map<String, Object> source = new HashMap<>();
            source.put("type", "KNOWLEDGE");
            source.put("title", a.getTitle());
            source.put("ref", "知识库文章 #" + a.getId());
            chunk.put("source", source);
            chunk.put("score", score);
            chunks.add(chunk);
        }

        // 法规召回
        List<Regulation> regs = regulationMapper.selectList(null);
        for (Regulation r : regs) {
            String haystack = (r.getTitle() == null ? "" : r.getTitle())
                    + " " + (r.getKeywords() == null ? "" : r.getKeywords())
                    + " " + (r.getContent() == null ? "" : r.getContent());
            double score = scoreOf(terms, haystack);
            if (score <= 0) continue;
            Map<String, Object> chunk = new HashMap<>();
            chunk.put("content", snippetOf(haystack, terms, 160));
            Map<String, Object> source = new HashMap<>();
            source.put("type", "REGULATION");
            source.put("title", r.getTitle());
            source.put("ref", r.getTitle());
            chunk.put("source", source);
            chunk.put("score", score);
            chunks.add(chunk);
        }

        chunks.sort((a, b) -> Double.compare((double) b.get("score"), (double) a.get("score")));
        chunks = chunks.stream().limit(k).collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("chunks", chunks);
        result.put("degraded", true); // 当前为关键词检索降级实现
        return result;
    }

    /** 分词：按非中文字符与常用标点切分 */
    private List<String> tokenize(String query) {
        List<String> terms = new ArrayList<>();
        for (String s : query.split("[\\s，。、；：,.;:!?！？()（）]+")) {
            if (s.isBlank()) continue;
            terms.add(s);
            // 长词做二元切分，提升中文匹配召回
            if (s.length() >= 3) {
                for (int i = 0; i + 2 <= s.length(); i++) {
                    terms.add(s.substring(i, i + 2));
                }
            }
        }
        return terms.stream().distinct().collect(Collectors.toList());
    }

    /** 相关度评分：命中词条数与权重 */
    private double scoreOf(List<String> terms, String haystack) {
        if (haystack == null || haystack.isBlank()) return 0;
        double score = 0;
        for (String t : terms) {
            if (haystack.contains(t)) score += 1;
        }
        return score;
    }

    /** 截取首个命中词附近的片段 */
    private String snippetOf(String haystack, List<String> terms, int maxLen) {
        int firstHit = -1;
        for (String t : terms) {
            int idx = haystack.indexOf(t);
            if (idx >= 0 && (firstHit == -1 || idx < firstHit)) {
                firstHit = idx;
            }
        }
        if (firstHit < 0) return truncate(haystack, maxLen);
        int start = Math.max(0, firstHit - 20);
        return "..." + truncate(haystack.substring(start), maxLen) + "...";
    }

    private String truncate(String s, int maxLen) {
        return s.length() > maxLen ? s.substring(0, maxLen) : s;
    }
}
