package com.fazhitong.document.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fazhitong.common.ai.AiClient;
import com.fazhitong.common.dto.PageParam;
import com.fazhitong.common.dto.PageResult;
import com.fazhitong.document.entity.DocumentTemplate;
import com.fazhitong.document.entity.DocumentRecord;
import com.fazhitong.document.entity.DocumentCategory;
import com.fazhitong.document.mapper.DocumentTemplateMapper;
import com.fazhitong.document.mapper.DocumentRecordMapper;
import com.fazhitong.document.mapper.DocumentCategoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private static final String DRAFT_SYSTEM_PROMPT =
            "你是一位专业法律文书撰写助手。请根据用户给出的文书类型和需求描述，"
                    + "生成一份结构完整、格式规范、可直接使用的中文法律文书草稿，"
                    + "包含标题、当事人信息、正文、落款等必要要素，并在关键信息处用占位符提示补充。";

    private final DocumentTemplateMapper templateMapper;
    private final DocumentRecordMapper recordMapper;
    private final DocumentCategoryMapper categoryMapper;

    public List<DocumentCategory> listCategories() {
        return categoryMapper.selectList(
                new LambdaQueryWrapper<DocumentCategory>().orderByAsc(DocumentCategory::getSort));
    }

    public PageResult<DocumentTemplate> listTemplates(String category, String keyword, PageParam pageParam) {
        LambdaQueryWrapper<DocumentTemplate> wrapper = new LambdaQueryWrapper<>();
        if (category != null && !category.isBlank()) wrapper.eq(DocumentTemplate::getType, category);
        if (keyword != null && !keyword.isBlank()) wrapper.like(DocumentTemplate::getName, keyword);
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

    /**
     * AI 文书起草：优先调用大模型，未配置或失败时降级为从模板库智能匹配。
     */
    public String aiDraft(String type, String description) {
        String draft = null;
        if (AiClient.isEnabled()) {
            draft = AiClient.chat(DRAFT_SYSTEM_PROMPT, "文书类型：" + type + "\n需求描述：" + description);
        }
        if (draft != null && !draft.isBlank()) {
            return draft;
        }
        // 降级：从模板库匹配最相关的模板
        String matched = matchTemplateDraft(type, description);
        if (matched != null) {
            return matched;
        }
        return fallbackDraft(type, description);
    }

    /**
     * 从模板库按文书类型与需求描述匹配最相关模板，返回模板内容（含占位符）。
     * 匹配策略：先按类型关键词匹配模板名称/分类，再按需求描述关键词兜底。
     */
    private String matchTemplateDraft(String type, String description) {
        List<DocumentTemplate> templates = templateMapper.selectList(
                new LambdaQueryWrapper<DocumentTemplate>().eq(DocumentTemplate::getStatus, 1));
        if (templates == null || templates.isEmpty()) {
            return null;
        }
        String query = (type == null ? "" : type) + " " + (description == null ? "" : description);
        String best = null;
        int bestScore = -1;
        for (DocumentTemplate t : templates) {
            int score = matchScore(query, t.getName()) * 3 + matchScore(query, t.getCategory());
            if (score > bestScore) {
                bestScore = score;
                best = t.getContent();
            }
        }
        if (best == null || best.isBlank() || bestScore <= 0) {
            return null;
        }
        return best + "\n\n（提示：已根据「" + type + "」匹配最接近的文书模板，填写其中的占位符即可使用；配置大模型后可获得更贴合描述的定制草稿。）";
    }

    /** 计算查询串与文本的匹配得分：命中关键词数量 */
    private int matchScore(String query, String text) {
        if (text == null || text.isBlank() || query == null || query.isBlank()) return 0;
        int score = 0;
        for (String term : query.split("[\\s，。、；：,.;:!?！？()（）]+")) {
            if (term.isBlank()) continue;
            if (text.contains(term)) score++;
            // 长词做二元切分提升命中
            if (term.length() >= 2) {
                for (int i = 0; i + 2 <= term.length(); i++) {
                    if (text.contains(term.substring(i, i + 2))) score++;
                }
            }
        }
        return score;
    }

    private String fallbackDraft(String type, String description) {
        String docType = (type == null || type.isBlank()) ? "法律文书" : type;
        return "【" + docType + "】\n\n"
                + "一、当事人信息\n  甲方：\n  乙方：\n\n"
                + "二、事实与理由\n  需求描述：" + (description == null ? "" : description) + "\n\n"
                + "三、请求事项\n  （请根据实际情况补充）\n\n"
                + "四、落款\n  具状人/签署方：\n  日期：____年__月__日\n\n"
                + "（提示：配置大模型后可根据描述智能生成完整、规范的法律文书内容。）";
    }
}
