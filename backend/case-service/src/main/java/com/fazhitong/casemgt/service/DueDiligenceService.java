package com.fazhitong.casemgt.service;

import com.fazhitong.common.ai.AiClient;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 尽职调查助手：根据目标企业名称与尽调重点，生成结构化尽调报告。
 * 优先调用大模型，未配置或失败时降级为模板报告。
 */
@Service
public class DueDiligenceService {

    private static final String DD_SYSTEM =
            "你是资深尽调律师。请根据目标企业名称与尽调重点，输出以下 JSON："
                    + "{\"summary\":\"整体结论\",\"company\":{\"name\":\"\",\"creditCode\":\"\",\"registeredCapital\":\"\",\"legalRepresentative\":\"\"},"
                    + "\"risks\":[{\"category\":\"工商/诉讼/经营/合规\",\"level\":\"高/中/低\",\"desc\":\"风险描述\",\"suggestion\":\"建议\"}],"
                    + "\"litigation\":[{\"type\":\"诉讼类型\",\"count\":\"数量\",\"note\":\"说明\"}],"
                    + "\"advice\":[\"尽调建议\"]}"
                    + "无法获取的真实数据填\"待核实\"，不要编造企业信息。";

    public Map<String, Object> generate(String companyName, String focus) {
        Map<String, Object> result;
        if (AiClient.isEnabled()) {
            Map<String, Object> ai = tryAi(companyName, focus);
            if (ai != null) return ai;
        }
        return ruleBased(companyName, focus);
    }

    private Map<String, Object> tryAi(String companyName, String focus) {
        String raw = AiClient.chat(DD_SYSTEM,
                "目标企业：" + (companyName == null ? "" : companyName)
                        + "\n尽调重点：" + (focus == null || focus.isBlank() ? "全面尽调" : focus));
        if (raw == null || raw.isBlank()) return null;
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            String json = raw.trim();
            int s = json.indexOf('{');
            int e = json.lastIndexOf('}');
            if (s < 0 || e <= s) return null;
            com.fasterxml.jackson.databind.JsonNode node = mapper.readTree(json.substring(s, e + 1));
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("summary", node.path("summary").asText(""));
            result.put("company", jsonToMap(node.path("company")));
            result.put("risks", jsonToList(node.path("risks")));
            result.put("litigation", jsonToList(node.path("litigation")));
            result.put("advice", jsonToList(node.path("advice")));
            result.put("mode", "AI");
            return result;
        } catch (Exception ex) {
            return null;
        }
    }

    private Map<String, Object> jsonToMap(com.fasterxml.jackson.databind.JsonNode node) {
        Map<String, Object> m = new LinkedHashMap<>();
        if (node.isObject()) {
            node.fields().forEachRemaining(f -> m.put(f.getKey(), f.getValue().asText()));
        }
        return m;
    }

    private List<Object> jsonToList(com.fasterxml.jackson.databind.JsonNode node) {
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

    private Map<String, Object> ruleBased(String companyName, String focus) {
        String name = companyName == null || companyName.isBlank() ? "目标企业" : companyName;
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("summary", "已完成针对「" + name + "」的基础尽职调查（模板模式）。"
                + "由于未接入企业征信与大模型数据源，部分信息需人工核实，以下为按尽调重点生成的风险清单与建议。");

        Map<String, Object> company = new LinkedHashMap<>();
        company.put("name", name);
        company.put("creditCode", "待核实");
        company.put("registeredCapital", "待核实");
        company.put("legalRepresentative", "待核实");
        result.put("company", company);

        List<Object> risks = new ArrayList<>();
        risks.add(risk("工商", "中", "需核实企业工商登记信息、股权结构与是否存在股权质押、冻结。",
                "通过国家企业信用信息公示系统核查工商登记与股权出质情况。"));
        risks.add(risk("诉讼", "中", "需核查企业是否存在作为被告的未决诉讼、被执行信息或失信记录。",
                "查询裁判文书、执行信息、失信被执行人名单。"));
        risks.add(risk("经营", "中", "需了解企业主营业务、资质许可与经营持续性。",
                "核查行业资质、经营许可及近年经营状况。"));
        risks.add(risk("合规", "中", "需评估企业在劳动、税务、环保、数据等方面的合规状况。",
                "审查劳动合同、纳税记录、环保处罚与数据合规制度。"));
        result.put("risks", risks);

        List<Object> litigation = new ArrayList<>();
        litigation.add(mapOf("type", "涉诉情况", "count", "待核实", "note", "建议通过裁判文书网与执行信息公开网核查"));
        litigation.add(mapOf("type", "被执行情况", "count", "待核实", "note", "建议核查失信与限制高消费记录"));
        result.put("litigation", litigation);

        List<Object> advice = new ArrayList<>();
        advice.add("委托专业机构调取工商内档、不动产、涉诉等公开信息");
        advice.add("要求目标企业提供财务报表、纳税证明与重大合同");
        advice.add("对核心资产、知识产权、重大债权债务进行专项核查");
        advice.add("结合尽调重点（" + (focus == null || focus.isBlank() ? "全面" : focus) + "）出具正式尽调报告");
        result.put("advice", advice);
        result.put("mode", "RULE");
        return result;
    }

    private Map<String, Object> risk(String category, String level, String desc, String suggestion) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("category", category);
        m.put("level", level);
        m.put("desc", desc);
        m.put("suggestion", suggestion);
        return m;
    }

    private Map<String, Object> mapOf(String k1, String v1, String k2, String v2, String k3, String v3) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put(k1, v1);
        m.put(k2, v2);
        m.put(k3, v3);
        return m;
    }
}
