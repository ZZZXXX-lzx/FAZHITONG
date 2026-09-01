package com.fazhitong.casemgt.service;

import com.fazhitong.common.ai.AiClient;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 诉讼智能助手：案件分析、诉讼时间轴、庭审提纲、质证要点、判决预测。
 * 优先调用大模型，未配置或失败时降级为规则模板。
 */
@Service
public class LitigationService {

    private static final String LITIGATION_SYSTEM =
            "你是资深诉讼律师。请根据用户提供的案由与案情描述，输出以下内容的 JSON："
                    + "{\"focus\":[\"争议焦点1\",\"争议焦点2\"],\"evidence\":[\"证据建议1\",\"证据建议2\"],"
                    + "\"timeline\":[{\"stage\":\"阶段名\",\"desc\":\"说明\"}],"
                    + "\"trialOutline\":[\"庭审提纲要点\"],\"crossExam\":[\"质证要点\"],"
                    + "\"prediction\":{\"outcome\":\"胜诉/败诉/部分胜诉/无法判断\",\"probability\":\"高/中/低\",\"reason\":\"理由\"}}"
                    + "不要编造法条，无法判断的填保守值。";

    public Map<String, Object> analyze(String cause, String description) {
        Map<String, Object> result;
        if (AiClient.isEnabled()) {
            Map<String, Object> ai = tryAi(cause, description);
            if (ai != null) return ai;
        }
        return ruleBased(cause, description);
    }

    private Map<String, Object> tryAi(String cause, String description) {
        String raw = AiClient.chat(LITIGATION_SYSTEM,
                "案由：" + (cause == null ? "" : cause) + "\n案情描述：" + (description == null ? "" : description));
        if (raw == null || raw.isBlank()) return null;
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            String json = raw.trim();
            int s = json.indexOf('{');
            int e = json.lastIndexOf('}');
            if (s < 0 || e <= s) return null;
            com.fasterxml.jackson.databind.JsonNode node = mapper.readTree(json.substring(s, e + 1));
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("focus", toList(node.path("focus")));
            result.put("evidence", toList(node.path("evidence")));
            result.put("timeline", toList(node.path("timeline")));
            result.put("trialOutline", toList(node.path("trialOutline")));
            result.put("crossExam", toList(node.path("crossExam")));
            Map<String, Object> prediction = new LinkedHashMap<>();
            prediction.put("outcome", node.path("prediction").path("outcome").asText("无法判断"));
            prediction.put("probability", node.path("prediction").path("probability").asText("中"));
            prediction.put("reason", node.path("prediction").path("reason").asText(""));
            result.put("prediction", prediction);
            result.put("mode", "AI");
            return result;
        } catch (Exception ex) {
            return null;
        }
    }

    private List<Object> toList(com.fasterxml.jackson.databind.JsonNode node) {
        List<Object> list = new ArrayList<>();
        if (node.isArray()) {
            for (com.fasterxml.jackson.databind.JsonNode item : node) {
                if (item.isObject()) {
                    Map<String, Object> m = new LinkedHashMap<>();
                    item.fields().forEachRemaining(f -> m.put(f.getKey(), f.getValue().asText()));
                    list.add(m);
                } else {
                    list.add(item.asText());
                }
            }
        }
        return list;
    }

    /** 规则降级：按案由关键词生成结构化建议 */
    private Map<String, Object> ruleBased(String cause, String description) {
        String c = cause == null ? "" : cause;
        String d = description == null ? "" : description;
        String category = classify(c, d);

        Map<String, Object> result = new LinkedHashMap<>();

        result.put("focus", focusOf(category));
        result.put("evidence", evidenceOf(category));
        result.put("timeline", buildTimeline());
        result.put("trialOutline", buildTrialOutline(category));
        result.put("crossExam", crossExamOf(category));

        Map<String, Object> prediction = new LinkedHashMap<>();
        prediction.put("outcome", "无法判断");
        prediction.put("probability", "中");
        prediction.put("reason", "缺少关键证据与具体事实，暂无法判断胜诉概率。建议补充证据后由专业律师评估。");
        result.put("prediction", prediction);
        result.put("mode", "RULE");
        return result;
    }

    private String classify(String cause, String description) {
        String all = cause + " " + description;
        if (containsAny(all, "合同", "违约", "货款", "买卖", "租赁")) return "合同纠纷";
        if (containsAny(all, "劳动", "工资", "加班", "辞退", "工伤", "社保", "解雇")) return "劳动争议";
        if (containsAny(all, "离婚", "抚养", "继承", "彩礼", "婚姻")) return "婚姻家庭";
        if (containsAny(all, "交通事故", "车祸", "撞", "交强险")) return "交通事故";
        if (containsAny(all, "借款", "借条", "欠款", "利息", "担保", "民间借贷")) return "民间借贷";
        if (containsAny(all, "商标", "专利", "著作权", "侵权", "知识产权")) return "知识产权";
        if (containsAny(all, "房产", "物业", "买房", "租房", "烂尾")) return "房产纠纷";
        return "民事纠纷";
    }

    private List<Object> focusOf(String category) {
        List<Object> list = new ArrayList<>();
        switch (category) {
            case "合同纠纷" -> { list.add("合同是否成立并生效"); list.add("是否存在违约行为及违约责任的认定"); list.add("损失金额的计算依据"); }
            case "劳动争议" -> { list.add("劳动关系是否成立"); list.add("解除或拖欠行为的合法性"); list.add("经济补偿或赔偿金的计算"); }
            case "婚姻家庭" -> { list.add("感情是否确已破裂"); list.add("共同财产的范围与分割"); list.add("子女抚养权的归属"); }
            case "交通事故" -> { list.add("事故责任划分"); list.add("各项损失的合理性"); list.add("保险赔付范围"); }
            case "民间借贷" -> { list.add("借贷关系是否成立"); list.add("借款本金与利息的认定"); list.add("担保责任的范围"); }
            default -> { list.add("法律关系的性质认定"); list.add("各方权利义务的界定"); list.add("损失与责任的因果关系"); }
        }
        return list;
    }

    private List<Object> evidenceOf(String category) {
        List<Object> list = new ArrayList<>();
        list.add("能够证明法律关系成立的书面凭证（合同、协议、单据等）");
        list.add("沟通记录（微信、短信、邮件、通话录音等）");
        list.add("款项往来凭证（转账记录、收据、发票等）");
        if ("交通事故".equals(category)) list.add("事故认定书、医疗票据、修车单据等");
        if ("劳动争议".equals(category)) list.add("劳动合同、考勤记录、工资流水、社保记录等");
        list.add("相关主体身份信息（身份证、营业执照等）");
        return list;
    }

    private List<Object> buildTimeline() {
        List<Object> list = new ArrayList<>();
        String[][] stages = {
                {"立案", "向有管辖权的法院提交起诉材料，缴纳诉讼费"},
                {"送达", "法院向被告送达起诉状副本与应诉通知"},
                {"举证", "双方在举证期限内提交证据，可申请延期或调查取证"},
                {"开庭", "法庭调查、举证质证、法庭辩论"},
                {"判决", "法院作出判决，当事人可上诉"},
                {"执行", "生效判决未履行的，申请强制执行"}
        };
        for (String[] s : stages) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("stage", s[0]);
            m.put("desc", s[1]);
            list.add(m);
        }
        return list;
    }

    private List<Object> buildTrialOutline(String category) {
        List<Object> list = new ArrayList<>();
        list.add("陈述诉讼请求与事实依据");
        list.add("围绕争议焦点逐项举证");
        list.add("针对对方证据发表质证意见");
        list.add("就" + category + "的争议焦点展开辩论");
        list.add("作最后陈述，重申诉讼请求");
        return list;
    }

    private List<Object> crossExamOf(String category) {
        List<Object> list = new ArrayList<>();
        list.add("核对对方证据的真实性、合法性与关联性（三性）");
        list.add("对无原件或来源不明的证据主张不予认可");
        list.add("对证明目的与待证事实不匹配的证据提出异议");
        if ("合同纠纷".equals(category)) list.add("核对合同签署主体、签字盖章的真实性");
        if ("民间借贷".equals(category)) list.add("核对借条形成时间与款项实际交付情况");
        return list;
    }

    private boolean containsAny(String text, String... keywords) {
        for (String k : keywords) {
            if (text.contains(k)) return true;
        }
        return false;
    }
}
