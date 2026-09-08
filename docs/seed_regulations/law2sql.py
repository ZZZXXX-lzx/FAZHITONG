# -*- coding: utf-8 -*-
"""
法智通 —— 权威法规全文入库流水线（阶段五）
将一部法律的正式全文（政府网 HTML）清洗为条文，并生成可重复执行的 SQL。
用法:
    python law2sql.py 源文件.txt 目标.sql
输入源文件为已清洗好的纯文本（每段一行，"第X条"开头），
SQL 依据标题 upsert 到 regulation 表并重建其 regulation_article。
"""
import os
import json
import re
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
HEAD = re.compile(r'^第[一二三四五六七八九十百千万零〇0-9]+条(?:之[一二三四五六七八九十]+)?')


def strip_html(h: str) -> str:
    h = re.sub(r'<script.*?</script>', ' ', h, flags=re.S | re.I)
    h = re.sub(r'<style.*?</style>', ' ', h, flags=re.S | re.I)
    # 块级标签换行，保证"第X条"在行首
    h = re.sub(r'<(br|/p|p|/div|div|/h\d|h\d|/li|li|/tr|tr)[^>]*>', '\n', h, flags=re.I)
    h = re.sub(r'<[^>]+>', '', h)
    import html as _h
    h = _h.unescape(h)
    h = re.sub(r'[ \t\u00a0\u3000]+', ' ', h)
    h = re.sub(r'\n\s*\n+', '\n', h)
    lines = [ln.strip() for ln in h.split('\n')]
    return '\n'.join(ln for ln in lines if ln)


def split_articles(text: str):
    """按行切条：仅行首/段落边界出现"第X条"才算新条文起点。"""
    arts = []
    cur = None
    for raw in text.split('\n'):
        t = raw.strip()
        if not t:
            continue
        if HEAD.match(t):
            m = HEAD.match(t)
            no = m.group(0)
            content = t[m.end():].strip()
            cur = {'no': no, 'content': content}
            arts.append(cur)
        elif re.match(r'^第[一二三四五六七八九十百千万零〇0-9]+[章节]', t):
            # 章/节标题行（含章名，如"第二章 劳动合同的订立"）：仅作结构分隔，不并入条文
            cur = None
        else:
            if cur is not None:
                cur['content'] = (cur['content'] + '\n' + t) if cur['content'] else t
    return [a for a in arts if a['no']]


def esc(s: str) -> str:
    return str(s).replace('\\', '\\\\').replace("'", "''")


def build_sql(meta: dict, arts: list) -> str:
    parts = []
    parts.append(f"-- ==== {meta['title']} ====")
    parts.append(f"SET @lid := (SELECT id FROM regulation WHERE title = '{esc(meta['title'])}' LIMIT 1);")
    parts.append(
        "INSERT INTO regulation (title, law_type, issuing_authority, publish_date, effective_date, "
        "status, content, keywords) "
        f"SELECT '{esc(meta['title'])}','{esc(meta['law_type'])}','{esc(meta['issuing'])}',"
        f"'{esc(meta['publish'].strip() or '')}','{esc(meta['effective'].strip() or '')}',"
        f"'{esc(meta['status'])}',"
        f"'{esc(arts[0]['content'][:400] if arts else meta['title'])}',"
        f"'{esc(meta['keywords'])}' WHERE @lid IS NULL;"
    )
    parts.append("SET @lid := IFNULL(@lid, LAST_INSERT_ID());")
    parts.append("DELETE FROM regulation_article WHERE regulation_id = @lid;")
    rows = []
    for a in arts:
        rows.append(f"(@lid, '{esc(a['no'])}', '{esc(a['content'])}')")
    if rows:
        parts.append("INSERT INTO regulation_article (regulation_id, article_no, content) VALUES\n" + ",\n".join(rows) + ";")
    else:
        parts.append("-- 无条文")
    parts.append("")
    return '\n'.join(parts)


def main():
    if len(sys.argv) < 3:
        print(__doc__)
        sys.exit(1)
    src, dst = sys.argv[1], sys.argv[2]
    law_name = sys.argv[3] if len(sys.argv) > 3 else None
    with open(os.path.join(HERE, 'manifest.json'), encoding='utf-8') as f:
        manifest = json.load(f)
    meta = manifest.get(law_name, {})
    text = open(src, encoding='utf-8').read()
    arts = split_articles(text)
    print(f"[{law_name or src}] 切分到 {len(arts)} 条")
    if not arts:
        print("  ! 未切到条文，跳过")
        sys.exit(0)
    print(" 首条:", arts[0]['no'], arts[0]['content'][:26])
    print(" 末条:", arts[-1]['no'])
    meta = {**{
        'title': law_name or open(src, encoding='utf-8').readline().strip() or '未命名',
        'law_type': '法律', 'issuing': '', 'publish': '', 'effective': '',
        'status': '现行有效', 'keywords': '',
    }, **meta}
    out = build_sql(meta, arts)
    open(dst, 'w', encoding='utf-8').write(out)
    print(" 已写出:", dst)


if __name__ == '__main__':
    main()