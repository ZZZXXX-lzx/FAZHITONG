package com.fabaotong.document.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fabaotong.common.dto.PageParam;
import com.fabaotong.common.dto.PageResult;
import com.fabaotong.document.entity.DocumentTemplate;
import com.fabaotong.document.entity.DocumentRecord;
import com.fabaotong.document.entity.DocumentCategory;
import com.fabaotong.document.mapper.DocumentTemplateMapper;
import com.fabaotong.document.mapper.DocumentRecordMapper;
import com.fabaotong.document.mapper.DocumentCategoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentTemplateMapper templateMapper;
    private final DocumentRecordMapper recordMapper;
    private final DocumentCategoryMapper categoryMapper;

    public List<DocumentCategory> listCategories() {
        return categoryMapper.selectList(
                new LambdaQueryWrapper<DocumentCategory>().orderByAsc(DocumentCategory::getSort));
    }

    public PageResult<DocumentTemplate> listTemplates(String category, String keyword, PageParam pageParam) {
        LambdaQueryWrapper<DocumentTemplate> wrapper = new LambdaQueryWrapper<>();
        if (category != null) wrapper.eq(DocumentTemplate::getCategory, category);
        if (keyword != null) wrapper.like(DocumentTemplate::getName, keyword);
        wrapper.eq(DocumentTemplate::getStatus, 1);
        Page<DocumentTemplate> page = templateMapper.selectPage(
                new Page<>(pageParam.getPage(), pageParam.getSize()), wrapper);
        return PageResult.of(page.getRecords(), page.getTotal(), (int) page.getCurrent(), (int) page.getSize());
    }

    public DocumentTemplate getTemplate(Long id) {
        return templateMapper.selectById(id);
    }

    public DocumentTemplate createTemplate(DocumentTemplate template) {
        if (template.getStatus() == null) template.setStatus(1);
        templateMapper.insert(template);
        return template;
    }

    public DocumentTemplate updateTemplate(DocumentTemplate template) {
        templateMapper.updateById(template);
        return templateMapper.selectById(template.getId());
    }

    public void updateTemplateStatus(Long id, int status) {
        DocumentTemplate template = templateMapper.selectById(id);
        if (template != null) {
            template.setStatus(status);
            templateMapper.updateById(template);
        }
    }

    public DocumentRecord generate(Long userId, Long templateId, Map<String, String> fields) {
        DocumentTemplate template = templateMapper.selectById(templateId);
        String content = template.getContent();
        for (Map.Entry<String, String> entry : fields.entrySet()) {
            content = content.replace("${" + entry.getKey() + "}", entry.getValue());
        }
        DocumentRecord record = new DocumentRecord();
        record.setUserId(userId);
        record.setTemplateId(templateId);
        record.setData(content);
        record.setStatus(1);
        recordMapper.insert(record);
        return record;
    }

    public List<DocumentRecord> myRecords(Long userId) {
        return recordMapper.selectList(
                new LambdaQueryWrapper<DocumentRecord>()
                        .eq(DocumentRecord::getUserId, userId)
                        .orderByDesc(DocumentRecord::getCreateTime));
    }
}
