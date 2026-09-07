package com.fazhitong.casemgt.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fazhitong.casemgt.entity.Regulation;
import com.fazhitong.casemgt.entity.RegulationArticle;
import com.fazhitong.casemgt.mapper.RegulationArticleMapper;
import com.fazhitong.casemgt.mapper.RegulationMapper;
import com.fazhitong.common.dto.PageParam;
import com.fazhitong.common.dto.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
}