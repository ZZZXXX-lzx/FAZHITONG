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
 * 法律知识图谱服务。
 * 基于法规库与知识库动态构建“法律领域 — 法规 — 法律概念”三层图谱：
 *   - 领域节点：民事/劳动/商事/知产/刑事/道路交通/民间借贷等法律部门
 *   - 法规节点：已入库的法律、行政法规、司法解释
 *   - 概念节点：从法规关键词与知识文章标签中提取的法律概念（同名合并）
 * 边关系：领域—包含→法规、法规—涉及→概念、概念—相关→概念（同法规共现）。
 * 图谱数据实时从数据库构建，无需额外建表。
 */
@Service
@RequiredArgsConstructor
public class KnowledgeGraphService {

    private final RegulationMapper regulationMapper;
    private final KnowledgeArticleMapper articleMapper;

    /** 领域 → 所属法规标题（前缀/包含匹配规则） */
    private static final Map<String, List<String>> DOMAIN_RULES = new LinkedHashMap<>();

    static {
        DOMAIN_RULES.put("民事法律", List.of("民法典", "民事诉讼法"));
        DOMAIN_RULES.put("劳动人事", List.of("劳动法", "劳动合同法"));
        DOMAIN_RULES.put("商事法律", List.of("公司法", "消费者权益保护法"));
        DOMAIN_RULES.put("知识产权", List.of("商标法", "专利法", "著作权法"));
        DOMAIN_RULES.put("刑事法律", List.of("刑法", "治安管理处罚法"));
        DOMAIN_RULES.put("道路交通", List.of("道路交通安全法"));
        DOMAIN_RULES.put("民间借贷", List.of("民间借贷"));
    }

    /**
     * 构建完整知识图谱。
     *
     * @return { nodes: [{id,name,type,category}], links: [{source,target,relation}], stats: {nodeCount, linkCount} }
     */
    public Map<String, Object> graph(String domainFilter) {
        List<Regulation> regulations = regulationMapper.selectList(null);
        List<KnowledgeArticle> articles = articleMapper.selectList(
                new LambdaQueryWrapper<KnowledgeArticle>().eq(KnowledgeArticle::getStatus, 1));

        List<Map<String, Object>> nodes = new ArrayList<>();
        List<Map<String, Object>> links = new ArrayList<>();
        // 名称 -> 节点id，用于同名合并
        Map<String, String> nodeIndex = new HashMap<>();
        Set<String> usedDomains = new HashSet<>();

        // 1) 领域节点（category 0）
        int domainSeq = 0;
        for (String domain : DOMAIN_RULES.keySet()) {
            if (domainFilter != null && !domainFilter.isBlank() && !domain.equals(domainFilter)) {
                continue;
            }
            String id = "d" + (domainSeq++);
            nodes.add(node(id, domain, "domain", 0));
            nodeIndex.put(domain, id);
            usedDomains.add(domain);
        }

        // 2) 法规节点（category 1）与 领域—法规 边
        int regSeq = 0;
        Map<Long, String> regNodeId = new HashMap<>();
        for (Regulation r : regulations) {
            String domain = matchDomain(r.getTitle());
            if (domain == null || !usedDomains.contains(domain)) continue;

            String id = "r" + (regSeq++);
            Map<String, Object> n = node(id, r.getTitle(), "regulation", 1);
            n.put("lawType", r.getLawType());
            n.put("refId", r.getId());
            nodes.add(n);
            regNodeId.put(r.getId(), id);

            links.add(link(nodeIndex.get(domain), id, "包含"));
        }

        // 3) 概念节点（category 2）与 法规—概念 边；记录概念共现以构建概念关联
        int conceptSeq = 0;
        // 概念名 -> { nodeId, 出现的法规id列表 }
        Map<String, String> conceptNodeId = new HashMap<>();
        Map<String, Set<Long>> conceptRegs = new HashMap<>();

        for (Regulation r : regulations) {
            String domain = matchDomain(r.getTitle());
            if (domain == null || !usedDomains.contains(domain)) continue;
            Set<String> concepts = extractConcepts(r.getKeywords());
            for (String c : concepts) {
                String cid = conceptNodeId.get(c);
                if (cid == null) {
                    cid = "c" + (conceptSeq++);
                    nodes.add(node(cid, c, "concept", 2));
                    conceptNodeId.put(c, cid);
                    conceptRegs.put(c, new HashSet<>());
                }
                conceptRegs.get(c).add(r.getId());
                links.add(link(regNodeId.get(r.getId()), cid, "涉及"));
            }
        }

        // 4) 从知识文章标签补充概念节点（category 2）
        for (KnowledgeArticle a : articles) {
            if (a.getTags() == null) continue;
            for (String t : a.getTags().split("[,，]")) {
                String tag = t.trim();
                if (tag.isBlank() || conceptNodeId.containsKey(tag)) continue;
                String cid = "c" + (conceptSeq++);
                nodes.add(node(cid, tag, "concept", 2));
                conceptNodeId.put(tag, cid);
                conceptRegs.put(tag, new HashSet<>());
            }
        }

        // 5) 概念—相关→概念 边（共享同一部法规的共现概念）
        Set<String> conceptLinks = new HashSet<>();
        for (Regulation r : regulations) {
            String domain = matchDomain(r.getTitle());
            if (domain == null || !usedDomains.contains(domain)) continue;
            List<String> concepts = new ArrayList<>(extractConcepts(r.getKeywords()));
            for (int i = 0; i < concepts.size(); i++) {
                for (int j = i + 1; j < concepts.size(); j++) {
                    String a = concepts.get(i), b = concepts.get(j);
                    String key = a.compareTo(b) < 0 ? a + "|" + b : b + "|" + a;
                    if (!conceptLinks.contains(key)
                            && conceptNodeId.containsKey(a)
                            && conceptNodeId.containsKey(b)) {
                        conceptLinks.add(key);
                        links.add(link(conceptNodeId.get(a), conceptNodeId.get(b), "相关"));
                    }
                }
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("nodes", nodes);
        result.put("links", links);
        Map<String, Object> stats = new HashMap<>();
        stats.put("nodeCount", nodes.size());
        stats.put("linkCount", links.size());
        stats.put("domainCount", usedDomains.size());
        result.put("stats", stats);
        result.put("domains", new ArrayList<>(usedDomains));
        return result;
    }

    /** 领域列表 */
    public List<String> domains() {
        return new ArrayList<>(DOMAIN_RULES.keySet());
    }

    /** 按关键词查询关联实体：返回命中的法规与概念 */
    public Map<String, Object> related(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return Map.of("regulations", Collections.emptyList(), "concepts", Collections.emptyList());
        }
        List<Regulation> regulations = regulationMapper.selectList(null);
        List<Map<String, Object>> hitRegs = new ArrayList<>();
        for (Regulation r : regulations) {
            String hay = (r.getTitle() == null ? "" : r.getTitle())
                    + (r.getKeywords() == null ? "" : r.getKeywords());
            if (hay.contains(keyword)) {
                Map<String, Object> m = new HashMap<>();
                m.put("id", r.getId());
                m.put("title", r.getTitle());
                m.put("lawType", r.getLawType());
                m.put("domain", matchDomain(r.getTitle()));
                m.put("keywords", r.getKeywords());
                hitRegs.add(m);
            }
        }
        // 概念命中
        Set<String> concepts = new TreeSet<>();
        for (Regulation r : regulations) {
            for (String c : extractConcepts(r.getKeywords())) {
                if (c.contains(keyword) || keyword.contains(c)) {
                    concepts.add(c);
                }
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("regulations", hitRegs);
        result.put("concepts", new ArrayList<>(concepts));
        return result;
    }

    /** 根据法规标题匹配所属领域 */
    private String matchDomain(String title) {
        if (title == null) return null;
        for (Map.Entry<String, List<String>> e : DOMAIN_RULES.entrySet()) {
            for (String kw : e.getValue()) {
                if (title.contains(kw)) return e.getKey();
            }
        }
        return null;
    }

    /** 从关键词串提取概念（去重、去空） */
    private Set<String> extractConcepts(String keywords) {
        if (keywords == null || keywords.isBlank()) return Collections.emptySet();
        return Arrays.stream(keywords.split("[,，]"))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private Map<String, Object> node(String id, String name, String type, int category) {
        Map<String, Object> n = new HashMap<>();
        n.put("id", id);
        n.put("name", name);
        n.put("type", type);
        n.put("category", category);
        return n;
    }

    private Map<String, Object> link(String source, String target, String relation) {
        Map<String, Object> l = new HashMap<>();
        l.put("source", source);
        l.put("target", target);
        l.put("relation", relation);
        return l;
    }
}
