package com.fazhitong.casemgt.util;

import com.fazhitong.casemgt.dto.RegulationImportRow;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 解析法规导入文件：支持 .xlsx / .xls / .csv
 * 约定列名（表头任意顺序），常见别名均支持：
 *   法规名称/法规名/名称/title
 *   法规类型/类型/lawType
 *   制定机关/颁布机关/issuingAuthority
 *   发布日期/发布/发布时间/publishDate
 *   施行日期/施行/effectiveDate
 *   效力状态/状态/status
 *   关键词/关键字/keywords
 *   法规简介/简介/全文概述/overview/content
 *   条文序号/条文编号/条款/articleNo
 *   条文内容/条文/条款内容/articleContent
 */
public final class RegulationFileParser {

    private RegulationFileParser() {}

    private static final Map<String, String> ALIAS = new HashMap<>();
    static {
        ALIAS.put("法规名称", "title"); ALIAS.put("法规名", "title"); ALIAS.put("名称", "title"); ALIAS.put("title", "title");
        ALIAS.put("法规类型", "lawType"); ALIAS.put("类型", "lawType"); ALIAS.put("lawType", "lawType");
        ALIAS.put("制定机关", "issuingAuthority"); ALIAS.put("颁布机关", "issuingAuthority"); ALIAS.put("issuingAuthority", "issuingAuthority");
        ALIAS.put("发布日期", "publishDate"); ALIAS.put("发布", "publishDate"); ALIAS.put("publishDate", "publishDate");
        ALIAS.put("施行日期", "effectiveDate"); ALIAS.put("施行", "effectiveDate"); ALIAS.put("effectiveDate", "effectiveDate");
        ALIAS.put("效力状态", "status"); ALIAS.put("状态", "status"); ALIAS.put("status", "status");
        ALIAS.put("关键词", "keywords"); ALIAS.put("关键字", "keywords"); ALIAS.put("keywords", "keywords");
        ALIAS.put("法规简介", "overview"); ALIAS.put("简介", "overview"); ALIAS.put("全文概述", "overview"); ALIAS.put("overview", "overview"); ALIAS.put("content", "overview");
        ALIAS.put("条文序号", "articleNo"); ALIAS.put("条文编号", "articleNo"); ALIAS.put("条款", "articleNo"); ALIAS.put("articleNo", "articleNo");
        ALIAS.put("条文内容", "articleContent"); ALIAS.put("条文", "articleContent"); ALIAS.put("条款内容", "articleContent"); ALIAS.put("articleContent", "articleContent");
    }

    public static List<RegulationImportRow> parse(String filename, InputStream in) throws IOException {
        String lower = filename.toLowerCase();
        if (lower.endsWith(".csv")) {
            return parseCsv(in);
        }
        return parseExcel(in);
    }

    private static List<RegulationImportRow> parseExcel(InputStream in) throws IOException {
        List<List<String>> grid = new ArrayList<>();
        try (Workbook wb = new XSSFWorkbook(in)) {
            Sheet sheet = wb.getSheetAt(0);
            DataFormatter fmt = new DataFormatter();
            for (Row row : sheet) {
                List<String> cells = new ArrayList<>();
                for (Cell cell : row) {
                    cells.add(fmt.formatCellValue(cell).trim());
                }
                grid.add(cells);
            }
        }
        return gridToRows(grid);
    }

    private static List<RegulationImportRow> parseCsv(InputStream in) throws IOException {
        String text = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        if (text.startsWith("\uFEFF")) {
            text = text.substring(1);
        }
        List<List<String>> grid = parseCsvGrid(text);
        return gridToRows(grid);
    }

    /** 遍历全文切分，正确处理引号包裹字段内的逗号与换行 */
    private static List<List<String>> parseCsvGrid(String text) {
        List<List<String>> rows = new ArrayList<>();
        List<String> cur = new ArrayList<>();
        StringBuilder cell = new StringBuilder();
        boolean inQ = false;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (inQ) {
                if (c == '"') {
                    if (i + 1 < text.length() && text.charAt(i + 1) == '"') {
                        cell.append('"'); i++;
                    } else {
                        inQ = false;
                    }
                } else {
                    cell.append(c);
                }
            } else if (c == '"') {
                inQ = true;
            } else if (c == ',') {
                cur.add(cell.toString().trim()); cell.setLength(0);
            } else if (c == '\n' || c == '\r') {
                if (c == '\r' && i + 1 < text.length() && text.charAt(i + 1) == '\n') {
                    i++;
                }
                cur.add(cell.toString().trim()); cell.setLength(0);
                rows.add(cur); cur = new ArrayList<>();
            } else {
                cell.append(c);
            }
        }
        if (cell.length() > 0 || !cur.isEmpty()) {
            cur.add(cell.toString().trim());
            rows.add(cur);
        }
        return rows;
    }

    private static List<RegulationImportRow> gridToRows(List<List<String>> grid) {
        List<RegulationImportRow> rows = new ArrayList<>();
        if (grid.isEmpty()) return rows;
        List<String> header = grid.get(0);
        // 表头列名 -> 字段名
        Map<Integer, String> colField = new HashMap<>();
        for (int i = 0; i < header.size(); i++) {
            String h = header.get(i);
            if (h == null || h.isBlank()) continue;
            String f = ALIAS.getOrDefault(h.trim(), h.trim());
            colField.put(i, f);
        }
        for (int r = 1; r < grid.size(); r++) {
            List<String> cells = grid.get(r);
            RegulationImportRow row = new RegulationImportRow();
            for (Map.Entry<Integer, String> e : colField.entrySet()) {
                int idx = e.getKey();
                if (idx >= cells.size()) continue;
                String v = cells.get(idx);
                if (v == null) continue;
                setField(row, e.getValue(), v);
            }
            rows.add(row);
        }
        return rows;
    }

    private static void setField(RegulationImportRow row, String field, String value) {
        switch (field) {
            case "title": row.setTitle(trim(value)); break;
            case "lawType": row.setLawType(trim(value)); break;
            case "issuingAuthority": row.setIssuingAuthority(trim(value)); break;
            case "publishDate": row.setPublishDate(trim(value)); break;
            case "effectiveDate": row.setEffectiveDate(trim(value)); break;
            case "status": row.setStatus(trim(value)); break;
            case "keywords": row.setKeywords(trim(value)); break;
            case "overview": row.setOverview(trim(value)); break;
            case "articleNo": row.setArticleNo(trim(value)); break;
            case "articleContent": row.setArticleContent(trim(value)); break;
            default: break;
        }
    }

    private static String trim(String v) {
        if (v == null) return null;
        String t = v.trim();
        return t.isEmpty() ? null : t;
    }
}