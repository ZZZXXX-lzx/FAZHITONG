package com.fabaotong.casemgt.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fabaotong.casemgt.entity.KnowledgeArticle;
import com.fabaotong.casemgt.entity.KnowledgeCategory;
import com.fabaotong.casemgt.mapper.KnowledgeArticleMapper;
import com.fabaotong.casemgt.mapper.KnowledgeCategoryMapper;
import com.fabaotong.common.dto.PageParam;
import com.fabaotong.common.dto.PageResult;
import com.fabaotong.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class KnowledgeService {

    private final KnowledgeCategoryMapper categoryMapper;
    private final KnowledgeArticleMapper articleMapper;

    /**
     * 查全部分类(status=1, orderByAsc sort)
     */
    public List<KnowledgeCategory> listCategories() {
        return categoryMapper.selectList(
                new LambdaQueryWrapper<KnowledgeCategory>()
                        .eq(KnowledgeCategory::getStatus, 1)
                        .orderByAsc(KnowledgeCategory::getSort));
    }

    /**
     * 分页查询文章(status=1, categoryId可选, keyword模糊搜索title/summary/tags, orderByDesc isTop+createTime)
     */
    public PageResult<KnowledgeArticle> listArticles(Long categoryId, String keyword, PageParam pageParam) {
        LambdaQueryWrapper<KnowledgeArticle> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KnowledgeArticle::getStatus, 1);
        if (categoryId != null) {
            wrapper.eq(KnowledgeArticle::getCategoryId, categoryId);
        }
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w
                    .like(KnowledgeArticle::getTitle, keyword)
                    .or().like(KnowledgeArticle::getSummary, keyword)
                    .or().like(KnowledgeArticle::getTags, keyword));
        }
        wrapper.orderByDesc(KnowledgeArticle::getIsTop)
               .orderByDesc(KnowledgeArticle::getCreateTime);

        Page<KnowledgeArticle> page = articleMapper.selectPage(
                new Page<>(pageParam.getPage(), pageParam.getSize()), wrapper);
        return PageResult.of(page.getRecords(), page.getTotal(),
                (int) page.getCurrent(), (int) page.getSize());
    }

    /**
     * 查详情, 检查null抛BusinessException, 同时 viewCount+1
     */
    public KnowledgeArticle getArticle(Long id) {
        KnowledgeArticle article = articleMapper.selectById(id);
        if (article == null) {
            throw new BusinessException("文章不存在");
        }
        article.setViewCount(article.getViewCount() == null ? 1 : article.getViewCount() + 1);
        articleMapper.updateById(article);
        return article;
    }

    /**
     * 等同listArticles但固定categoryId
     */
    public PageResult<KnowledgeArticle> listArticlesByCategory(Long categoryId, PageParam pageParam) {
        return listArticles(categoryId, null, pageParam);
    }

    /**
     * 管理员查询(不过滤status, orderByDesc createTime)
     */
    public PageResult<KnowledgeArticle> adminListArticles(String keyword, Integer status, PageParam pageParam) {
        LambdaQueryWrapper<KnowledgeArticle> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(KnowledgeArticle::getStatus, status);
        }
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w
                    .like(KnowledgeArticle::getTitle, keyword)
                    .or().like(KnowledgeArticle::getSummary, keyword)
                    .or().like(KnowledgeArticle::getTags, keyword));
        }
        wrapper.orderByDesc(KnowledgeArticle::getCreateTime);

        Page<KnowledgeArticle> page = articleMapper.selectPage(
                new Page<>(pageParam.getPage(), pageParam.getSize()), wrapper);
        return PageResult.of(page.getRecords(), page.getTotal(),
                (int) page.getCurrent(), (int) page.getSize());
    }

    /**
     * 创建文章
     */
    public KnowledgeArticle createArticle(KnowledgeArticle article) {
        if (article.getStatus() == null) {
            article.setStatus(1);
        }
        if (article.getViewCount() == null) {
            article.setViewCount(0);
        }
        if (article.getIsTop() == null) {
            article.setIsTop(0);
        }
        articleMapper.insert(article);
        return article;
    }

    /**
     * 更新文章
     */
    public KnowledgeArticle updateArticle(KnowledgeArticle article) {
        KnowledgeArticle existing = articleMapper.selectById(article.getId());
        if (existing == null) {
            throw new BusinessException("文章不存在");
        }
        articleMapper.updateById(article);
        return article;
    }

    /**
     * 上下架
     */
    public void toggleStatus(Long id, Integer status) {
        KnowledgeArticle article = articleMapper.selectById(id);
        if (article == null) {
            throw new BusinessException("文章不存在");
        }
        article.setStatus(status);
        articleMapper.updateById(article);
    }
}
