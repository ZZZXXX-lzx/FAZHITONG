package com.fazhitong.consultation.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fazhitong.common.ai.AiClient;
import com.fazhitong.common.dto.PageParam;
import com.fazhitong.common.dto.PageResult;
import com.fazhitong.common.exception.BusinessException;
import com.fazhitong.consultation.entity.Consultation;
import com.fazhitong.consultation.mapper.ConsultationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ConsultationService {

    private static final String LEGAL_SYSTEM_PROMPT =
            "你是一位专业的中国法律顾问。请用简洁、通俗的中文回答用户的法律问题，"
                    + "给出法律依据和可操作建议。涉及诉讼等复杂事务时，提示用户咨询执业律师。请勿编造法条。";

    private final ConsultationMapper consultationMapper;

    public Consultation create(Long userId, String title, String question, String type) {
        Consultation c = new Consultation();
        c.setUserId(userId);
        c.setTitle(title);
        c.setQuestion(question);
        c.setConsultationType(type);
        c.setStatus(0);
        consultationMapper.insert(c);
        // AI 咨询自动生成参考回复（优先真实模型，未配置时关键词降级）
        if ("AI".equalsIgnoreCase(type)) {
            c.setAnswer(answerWithAi(question));
            c.setStatus(1);
            c.setAnswerTime(LocalDateTime.now());
            consultationMapper.updateById(c);
        }
        return c;
    }

    private String answerWithAi(String question) {
        String ai = AiClient.chat(LEGAL_SYSTEM_PROMPT, question);
        return ai != null ? ai : generateAiAnswer(question);
    }

    /** 基于关键词的轻量 AI 参考回复（无需外部模型，保证离线可用） */
    private String generateAiAnswer(String question) {
        String q = question == null ? "" : question;
        String category = "民事法律问题";
        if (containsAny(q, "劳动", "工资", "加班", "辞退", "工伤", "社保")) {
            category = "劳动争议";
        } else if (containsAny(q, "离婚", "结婚", "抚养", "继承", "财产分割", "彩礼")) {
            category = "婚姻家庭";
        } else if (containsAny(q, "合同", "违约", "货款", "定金", "解除")) {
            category = "合同纠纷";
        } else if (containsAny(q, "交通事故", "车祸", "撞", "交强险")) {
            category = "交通事故";
        } else if (containsAny(q, "借款", "借条", "欠款", "利息", "担保")) {
            category = "民间借贷";
        } else if (containsAny(q, "商标", "专利", "著作权", "侵权")) {
            category = "知识产权";
        } else if (containsAny(q, "买房", "租房", "房产", "物业", "烂尾")) {
            category = "房产纠纷";
        } else if (containsAny(q, "消费", "网购", "退一赔三", "欺诈", "假货")) {
            category = "消费维权";
        }
        return "您好，我是法智通 AI 法律顾问。根据您的描述，该问题初步判断属于【" + category + "】。\n\n"
                + "建议您：\n"
                + "1. 收集并保存相关证据（合同、聊天记录、转账凭证、票据等）；\n"
                + "2. 明确争议焦点与诉求，优先尝试与对方协商解决；\n"
                + "3. 协商不成的，可通过人民调解、仲裁或诉讼等途径维权。\n\n"
                + "（本回复为 AI 生成的参考意见，不构成正式法律意见，如需精准判断建议咨询专业律师。）";
    }

    private boolean containsAny(String text, String... keywords) {
        for (String k : keywords) {
            if (text.contains(k)) {
                return true;
            }
        }
        return false;
    }

    public Consultation answer(Long id, Long lawyerId, String answer) {
        Consultation c = consultationMapper.selectById(id);
        if (c == null) {
            throw new BusinessException("咨询记录不存在");
        }
        c.setLawyerId(lawyerId);
        c.setAnswer(answer);
        c.setStatus(1);
        c.setAnswerTime(LocalDateTime.now());
        consultationMapper.updateById(c);
        return c;
    }

    public PageResult<Consultation> listByUser(Long userId, PageParam pageParam) {
        Page<Consultation> page = consultationMapper.selectPage(
                new Page<>(pageParam.getPage(), pageParam.getSize()),
                new LambdaQueryWrapper<Consultation>()
                        .eq(Consultation::getUserId, userId)
                        .orderByDesc(Consultation::getCreateTime));
        return PageResult.of(page.getRecords(), page.getTotal(), (int) page.getCurrent(), (int) page.getSize());
    }

    public PageResult<Consultation> listByLawyer(Long lawyerId, PageParam pageParam) {
        Page<Consultation> page = consultationMapper.selectPage(
                new Page<>(pageParam.getPage(), pageParam.getSize()),
                new LambdaQueryWrapper<Consultation>()
                        .eq(Consultation::getLawyerId, lawyerId)
                        .orderByDesc(Consultation::getCreateTime));
        return PageResult.of(page.getRecords(), page.getTotal(), (int) page.getCurrent(), (int) page.getSize());
    }

    public PageResult<Consultation> listPending(PageParam pageParam) {
        Page<Consultation> page = consultationMapper.selectPage(
                new Page<>(pageParam.getPage(), pageParam.getSize()),
                new LambdaQueryWrapper<Consultation>()
                        .eq(Consultation::getStatus, 0)
                        .orderByDesc(Consultation::getCreateTime));
        return PageResult.of(page.getRecords(), page.getTotal(), (int) page.getCurrent(), (int) page.getSize());
    }

    public Consultation getById(Long id) {
        return consultationMapper.selectById(id);
    }

    public PageResult<Consultation> listAll(PageParam pageParam) {
        Page<Consultation> page = consultationMapper.selectPage(
                new Page<>(pageParam.getPage(), pageParam.getSize()),
                new LambdaQueryWrapper<Consultation>()
                        .orderByDesc(Consultation::getCreateTime));
        return PageResult.of(page.getRecords(), page.getTotal(), (int) page.getCurrent(), (int) page.getSize());
    }
}
