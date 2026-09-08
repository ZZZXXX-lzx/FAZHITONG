package com.fazhitong.casemgt.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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

@Service
@RequiredArgsConstructor
public class RegulationService {

    private final RegulationMapper regulationMapper;
    private final RegulationArticleMapper articleMapper;

    /**
     * 法规检索：命中标题/关键词/内容摘要，或命中任意条文的正文。
     */
    public PageResult<Regulation> search(String keyword, String lawType, PageParam pageParam) {
        boolean hasKeyword = keyword != null && !keyword.isBlank();

        final Set<Long> articleRegulationIds = new HashSet<>();
        if (hasKeyword) {
            articleMapper.selectList(
                            new LambdaQueryWrapper<RegulationArticle>()
                                    .like(RegulationArticle::getContent, keyword)
                                    .select(RegulationArticle::getRegulationId))
                    .forEach(a -> {
                        if (a.getRegulationId() != null) {
                            articleRegulationIds.add(a.getRegulationId());
                        }
                    });
        }

        LambdaQueryWrapper<Regulation> wrapper = new LambdaQueryWrapper<>();
        if (hasKeyword) {
            wrapper.and(w -> w.like(Regulation::getTitle, keyword)
                    .or().like(Regulation::getKeywords, keyword)
                    .or().like(Regulation::getContent, keyword)
                    .or(!articleRegulationIds.isEmpty(), w2 -> w2.in(Regulation::getId, articleRegulationIds)));
        }
        if (lawType != null && !lawType.isBlank()) {
            wrapper.eq(Regulation::getLawType, lawType);
        }
        wrapper.orderByDesc(Regulation::getPublishDate).orderByDesc(Regulation::getId);
        Page<Regulation> page = regulationMapper.selectPage(
                new Page<>(pageParam.getPage(), pageParam.getSize()), wrapper);
        return PageResult.of(page.getRecords(), page.getTotal(), (int) page.getCurrent(), (int) page.getSize());
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