package com.fazhitong.casemgt.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fazhitong.casemgt.entity.Regulation;
import com.fazhitong.casemgt.entity.RegulationArticle;
import com.fazhitong.casemgt.mapper.RegulationArticleMapper;
import com.fazhitong.casemgt.mapper.RegulationMapper;
import com.fazhitong.casemgt.dto.RegulationImportRow;
import com.fazhitong.casemgt.dto.ImportResult;
import com.fazhitong.common.dto.PageParam;
import com.fazhitong.common.dto.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RegulationService {

    private final RegulationMapper regulationMapper;
    private final RegulationArticleMapper articleMapper;

    /**
     * 法规检索：多关键词（空格分隔）AND 召回——
     * 命中：标题/关键词/内容摘要，或任意条文的正文。
     * 返回时填充该法规的条文总数（articleCount）与命中的条文片段（matchArticles，最多3条）。
     */
    public PageResult<Regulation> search(String keyword, String lawType, PageParam pageParam) {
        List<String> terms = splitTerms(keyword);

        // 1) 逐关键词求命中法规 id，多关键词取交集（AND）
        Set<Long> hitIds = null;
        for (String term : terms) {
            Set<Long> forTerm = matchRegulationIds(term);
            hitIds = (hitIds == null) ? forTerm : intersect(hitIds, forTerm);
        }

        // 2) 分页查询
        QueryWrapper<Regulation> qw = new QueryWrapper<>();
        if (hitIds != null) {
            if (hitIds.isEmpty()) {
                return PageResult.of(Collections.emptyList(), 0L, pageParam.getPage(), pageParam.getSize());
            }
            qw.in("id", hitIds);
        }
        if (lawType != null && !lawType.isBlank()) {
            qw.eq("law_type", lawType);
        }
        qw.orderByDesc("publish_date").orderByDesc("id");
        long total = regulationMapper.selectCount(qw);
        Page<Regulation> page = regulationMapper.selectPage(
                new Page<>(pageParam.getPage(), pageParam.getSize()), qw);
        List<Regulation> records = page.getRecords();

        if (!records.isEmpty()) {
            List<Long> ids = records.stream().map(Regulation::getId).collect(Collectors.toList());
            Map<Long, Long> countMap = new HashMap<>();
            // 用 selectMaps 取聚合
            for (Map<String, Object> row : articleMapper.selectMaps(new QueryWrapper<RegulationArticle>()
                    .select("regulation_id", "count(*) as cnt")
                    .in("regulation_id", ids)
                    .groupBy("regulation_id"))) {
                Object rid = row.get("regulation_id");
                Object c = row.get("cnt");
                if (rid != null) {
                    countMap.put(((Number) rid).longValue(), c == null ? 0L : ((Number) c).longValue());
                }
            }
            for (Regulation r : records) {
                r.setArticleCount(countMap.getOrDefault(r.getId(), 0L));
            }
            // 命中条文片段
            if (!terms.isEmpty()) {
                Map<Long, List<RegulationArticle>> hits = matchArticles(ids, terms);
                for (Regulation r : records) {
                    r.setMatchArticles(hits.getOrDefault(r.getId(), Collections.emptyList()));
                }
            }
        }

        return PageResult.of(records, total, (int) page.getCurrent(), (int) page.getSize());
    }

    /** 空白分词 */
    private List<String> splitTerms(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(keyword.trim().split("[\\s,，、;；]+"))
                .filter(t -> !t.isBlank())
                .map(this::escapeLike)
                .collect(Collectors.toList());
    }

    /** 转义 LIKE 通配符 */
    private String escapeLike(String s) {
        return s.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }

    /** 单个关键词命中的法规 id：标题/关键词/摘要，或任意条文正文 */
    private Set<Long> matchRegulationIds(String term) {
        Set<Long> ids = new HashSet<>();
        for (Regulation r : regulationMapper.selectList(new LambdaQueryWrapper<Regulation>()
                .select(Regulation::getId)
                .like(Regulation::getTitle, term)
                .or().like(Regulation::getKeywords, term)
                .or().like(Regulation::getContent, term))) {
            ids.add(r.getId());
        }
        for (RegulationArticle a : articleMapper.selectList(new LambdaQueryWrapper<RegulationArticle>()
                .select(RegulationArticle::getRegulationId)
                .like(RegulationArticle::getContent, term))) {
            if (a.getRegulationId() != null) {
                ids.add(a.getRegulationId());
            }
        }
        return ids;
    }

    /** 某法规命中的条文片段（命中任一关键词，最多3条） */
    private Map<Long, List<RegulationArticle>> matchArticles(List<Long> regulationIds, List<String> terms) {
        Map<Long, List<RegulationArticle>> result = new HashMap<>();
        LambdaQueryWrapper<RegulationArticle> aw = new LambdaQueryWrapper<>();
        aw.in(RegulationArticle::getRegulationId, regulationIds);
        aw.and(w -> {
            boolean first = true;
            for (String t : terms) {
                if (first) {
                    w.like(RegulationArticle::getContent, t);
                    first = false;
                } else {
                    w.or().like(RegulationArticle::getContent, t);
                }
            }
        });
        // 只取号与正文，浅列；按法规分组后截取前3
        for (RegulationArticle a : articleMapper.selectList(aw
                .select(RegulationArticle::getRegulationId, RegulationArticle::getArticleNo, RegulationArticle::getContent)
                .orderByAsc(RegulationArticle::getId))) {
            result.computeIfAbsent(a.getRegulationId(), k -> new ArrayList<>()).add(a);
        }
        result.replaceAll((k, v) -> v.size() > 3 ? v.subList(0, 3) : v);
        return result;
    }

    private Set<Long> intersect(Set<Long> a, Set<Long> b) {
        Set<Long> out = new HashSet<>(a);
        out.retainAll(b);
        return out;
    }

    public Regulation getById(Long id) {
        return regulationMapper.selectById(id);
    }

    /** 法规详情（含全部条文） */
    public Regulation getDetail(Long id) {
        Regulation regulation = regulationMapper.selectById(id);
        if (regulation != null) {
            regulation.setArticles(listArticles(id));
        }
        return regulation;
    }

    public List<RegulationArticle> listArticles(Long regulationId) {
        return articleMapper.selectList(
                new LambdaQueryWrapper<RegulationArticle>()
                        .eq(RegulationArticle::getRegulationId, regulationId)
                        .orderByAsc(RegulationArticle::getId));
    }

    // ---------- 法条引用解析（案例判决依据 → 法规库条文联动） ----------

    private static final Pattern LAW_REF = Pattern.compile("《([^》]+)》");
    private static final Pattern ART_REF =
            Pattern.compile("第[0-9〇零一二三四五六七八九十百千]{1,12}条(?:之[一二三四五六七八九十]+)?");
    private static final Pattern ART_TAIL = Pattern.compile("第[一二三四五六七八九十]+(?:款|项|目)$");

    /**
     * 解析一段"判决依据/法条引用"文本，返回可跳转的条文列表。
     * 识别格式如《中华人民共和国民法典》第五百七十七条、第五百八十五条……
     * 每条引用解析出法规标题、法规 id、条文号、条文正文（未收录则 matched=false）。
     */
    public List<Map<String, Object>> resolveRefs(String quote) {
        List<Map<String, Object>> out = new ArrayList<>();
        if (quote == null || quote.isBlank()) {
            return out;
        }
        Matcher lm = LAW_REF.matcher(quote);
        String prevLaw = null;
        int prevEnd = 0;
        while (lm.find()) {
            if (prevLaw != null) {
                resolveSegment(out, prevLaw, quote.substring(prevEnd, lm.start()));
            }
            prevLaw = lm.group(1);
            prevEnd = lm.end();
        }
        if (prevLaw != null) {
            resolveSegment(out, prevLaw, quote.substring(prevEnd));
        }
        // 保留原文中可能独立出现的引用：无书名号但形如"民法典第xxx条"
        if (out.isEmpty()) {
            resolveWithoutBook(out, quote);
        }
        return out.size() > 40 ? out.subList(0, 40) : out;
    }

    private void resolveSegment(List<Map<String, Object>> out, String lawName, String seg) {
        if (seg == null) return;
        Regulation reg = regulationMapper.selectOne(
                new LambdaQueryWrapper<Regulation>().eq(Regulation::getTitle, lawName)
                        .last("limit 1"));
        if (reg == null) {
            // 标题不完全一致时退化为 LIKE 兜底
            reg = regulationMapper.selectOne(
                    new LambdaQueryWrapper<Regulation>().like(Regulation::getTitle, lawName)
                            .last("limit 1"));
        }
        Matcher am = ART_REF.matcher(seg);
        Set<String> seen = new LinkedHashSet<>();
        while (am.find()) {
            String no = am.group();
            if (!seen.add(no)) continue;
            addRef(out, lawName, reg, no);
        }
    }

    private void addRef(List<Map<String, Object>> out, String lawName, Regulation reg, String no) {
        String clean = ART_TAIL.matcher(no).replaceFirst("");
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("lawTitle", lawName);
        item.put("articleNo", no);
        if (reg != null) {
            item.put("regulationId", reg.getId());
            RegulationArticle art = articleMapper.selectOne(
                    new LambdaQueryWrapper<RegulationArticle>()
                            .eq(RegulationArticle::getRegulationId, reg.getId())
                            .eq(RegulationArticle::getArticleNo, clean)
                            .last("limit 1"));
            if (art != null) {
                item.put("matched", true);
                item.put("content", art.getContent());
            } else {
                item.put("matched", false);
            }
        } else {
            item.put("matched", false);
        }
        out.add(item);
    }

    /** 无书名号引用（形如"民法典第三百四十条"）：尝试按"法律简称+第X条"解析 */
    private void resolveWithoutBook(List<Map<String, Object>> out, String quote) {
        String[] lines = quote.split("[；;，,、\n]+");
        for (String seg : lines) {
            String trimmed = seg.trim();
            if (trimmed.isEmpty()) continue;
            // 找"第X条"前的名称片段
            java.util.regex.Matcher am0 = ART_REF.matcher(trimmed);
            if (!am0.find()) continue;
            String namePart = trimmed.substring(0, am0.start()).trim();
            if (namePart.length() < 2 || namePart.length() > 40) continue;
            namePart = namePart.replaceAll("^依据|根据|依照|按照", "").trim();
            Regulation reg = regulationMapper.selectOne(
                    new LambdaQueryWrapper<Regulation>()
                            .like(Regulation::getTitle, namePart)
                            .or().like(Regulation::getKeywords, namePart)
                            .last("limit 1"));
            if (reg == null) continue;
            for (String no : extractArticleNos(trimmed, namePart.length())) {
                addRef(out, reg.getTitle(), reg, no);
            }
        }
    }

    private List<String> extractArticleNos(String seg, int from) {
        List<String> nos = new ArrayList<>();
        Matcher m = ART_REF.matcher(seg.substring(from));
        Set<String> seen = new LinkedHashSet<>();
        while (m.find()) {
            String no = m.group();
            if (seen.add(no)) nos.add(no);
        }
        return nos;
    }

    public Regulation create(Regulation regulation) {
        // 只保存法规主表信息，新建后由专门接口追加条文
        regulation.setId(null);
        regulationMapper.insert(regulation);
        return regulation;
    }

    public Regulation update(Regulation regulation) {
        regulationMapper.updateById(regulation);
        return regulation;
    }

    @Transactional
    public void delete(Long id) {
        articleMapper.delete(new LambdaQueryWrapper<RegulationArticle>()
                .eq(RegulationArticle::getRegulationId, id));
        regulationMapper.deleteById(id);
    }

    public void createArticle(RegulationArticle article) {
        article.setId(null);
        articleMapper.insert(article);
    }

    public void updateArticle(RegulationArticle article) {
        articleMapper.updateById(article);
    }

    @Transactional
    public void deleteArticle(Long articleId) {
        articleMapper.deleteById(articleId);
    }

    /** 批量导入法规，支持每条法规内嵌 articles 条文列表 */
    @Transactional
    public int importRegulations(List<Regulation> list) {
        int count = 0;
        for (Regulation regulation : list) {
            List<RegulationArticle> articles = regulation.getArticles();
            regulation.setId(null);
            regulation.setArticles(null);
            regulationMapper.insert(regulation);
            if (articles != null) {
                for (RegulationArticle article : articles) {
                    article.setId(null);
                    article.setRegulationId(regulation.getId());
                    articleMapper.insert(article);
                }
            }
            count++;
        }
        return count;
    }

    /** 表格/文件导入：按法规名称分组，新建或追加条文 */
    @Transactional
    public ImportResult importRows(List<RegulationImportRow> rows) {
        ImportResult result = new ImportResult();
        if (rows == null || rows.isEmpty()) {
            return result;
        }
        // 按法规名称分组（保持文件顺序）
        Map<String, List<RegulationImportRow>> grouped = new LinkedHashMap<>();
        for (RegulationImportRow row : rows) {
            if (row.getTitle() == null || row.getTitle().isBlank()) continue;
            grouped.computeIfAbsent(row.getTitle().trim(), k -> new ArrayList<>()).add(row);
        }

        for (Map.Entry<String, List<RegulationImportRow>> e : grouped.entrySet()) {
            List<RegulationImportRow> lawRows = e.getValue();
            RegulationImportRow first = lawRows.get(0);

            // 解析出该法规的全部条文
            List<RegulationArticle> articles = new ArrayList<>();
            for (RegulationImportRow row : lawRows) {
                String body = row.getArticleContent();
                if (body == null || body.isBlank()) {
                    result.setSkipped(result.getSkipped() + 1);
                    continue;
                }
                body = body.trim();
                String no = row.getArticleNo();
                if (no != null && !no.isBlank()) {
                    articles.add(buildArticle(no.trim(), body));
                } else {
                    articles.addAll(splitArticles(body));
                }
            }

            Regulation existing = regulationMapper.selectOne(
                    new LambdaQueryWrapper<Regulation>().eq(Regulation::getTitle, e.getKey()));
            if (existing == null) {
                Regulation reg = buildRegulation(first);
                reg.setId(null);
                regulationMapper.insert(reg);
                for (RegulationArticle a : articles) {
                    a.setId(null);
                    a.setRegulationId(reg.getId());
                    articleMapper.insert(a);
                }
                result.setCreatedRegulations(result.getCreatedRegulations() + 1);
                result.setProvisions(result.getProvisions() + articles.size());
            } else {
                for (RegulationArticle a : articles) {
                    a.setId(null);
                    a.setRegulationId(existing.getId());
                    articleMapper.insert(a);
                }
                result.setAppendedRegulations(result.getAppendedRegulations() + 1);
                result.setProvisions(result.getProvisions() + articles.size());
            }
        }
        return result;
    }

    private Regulation buildRegulation(RegulationImportRow row) {
        Regulation reg = new Regulation();
        reg.setTitle(row.getTitle());
        reg.setLawType(row.getLawType() == null ? "法律" : row.getLawType());
        reg.setIssuingAuthority(row.getIssuingAuthority());
        reg.setPublishDate(row.getPublishDate());
        reg.setEffectiveDate(row.getEffectiveDate());
        reg.setStatus(row.getStatus() == null ? "现行有效" : row.getStatus());
        reg.setKeywords(row.getKeywords());
        reg.setContent(row.getOverview());
        return reg;
    }

    private RegulationArticle buildArticle(String no, String body) {
        RegulationArticle a = new RegulationArticle();
        a.setArticleNo(no);
        a.setContent(body);
        return a;
    }

    private static final Pattern ARTICLE_HEAD = Pattern.compile("^(第[一二三四五六七八九十百零0-9〇]+条(?:之[一二三四五六七八九十]+)?)(?=[\\s\u3000。，、：:；;．.~～-]|$)");

    /** 全文文本切条：仅当一行以"第X条"开头时视为新条文，避免正文中"第X条"引用被误切 */
    private List<RegulationArticle> splitArticles(String text) {
        List<RegulationArticle> out = new ArrayList<>();
        RegulationArticle cur = null;
        for (String raw : text.split("\r?\n")) {
            String t = raw.trim();
            if (t.isEmpty()) continue;
            Matcher m = ARTICLE_HEAD.matcher(t);
            if (m.find()) {
                cur = new RegulationArticle();
                cur.setArticleNo(m.group(1));
                cur.setContent(t.substring(m.group(1).length()).trim());
                out.add(cur);
            } else {
                if (cur == null) {
                    cur = new RegulationArticle();
                    cur.setArticleNo("");
                    cur.setContent("");
                    out.add(cur);
                }
                cur.setContent(cur.getContent() + (cur.getContent().isEmpty() ? "" : "\n") + t);
            }
        }
        boolean hasHead = out.stream().anyMatch(a -> a.getArticleNo() != null && !a.getArticleNo().isEmpty());
        if (!hasHead) {
            out.clear();
            RegulationArticle a = new RegulationArticle();
            a.setContent(text.trim());
            out.add(a);
        }
        return out;
    }
}