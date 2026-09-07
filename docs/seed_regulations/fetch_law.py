# -*- coding: utf-8 -*-
"""下载一部法律的权威全文 HTML 并清洗为纯文本（存 .txt）。
用法: python fetch_law.py <url> <out.txt> [是否保留章节目录]
"""
import re
import sys
import json
import gzip
import urllib.request
import html as _h

UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0 Safari/537.36'


def fetch(url):
    req = urllib.request.Request(url, headers={'User-Agent': UA, 'Accept': 'text/html,*/*',
                                              'Accept-Language': 'zh-CN,zh;q=0.9'})
    data = urllib.request.urlopen(req, timeout=60).read()
    for enc in ('utf-8', 'gb18030', 'gbk'):
        try:
            return data.decode(enc)
        except (UnicodeDecodeError, LookupError):
            continue
    return data.decode('utf-8', 'ignore')


def subst_text(url):
    try:
        h = fetch(url)
    except Exception as e:
        raise RuntimeError(f"下载失败 {url}: {e}")
    h = re.sub(r'<script.*?</script>', ' ', h, flags=re.S | re.I)
    h = re.sub(r'<style.*?</style>', ' ', h, flags=re.S | re.I)
    h = re.sub(r'<!--.*?-->', ' ', h, flags=re.S)
    # 块标签换行
    h = re.sub(r'<(br|li|/p|p|/div|div|/h[1-6]|h[1-6]|/td|td|/tr|tr)[^>]*>', '\n', h, flags=re.I)
    h = re.sub(r'<[^>]+>', '', h)
    h = _h.unescape(h)
    h = re.sub(r'[ \t\u00a0\u3000\ufeff]+', ' ', h)
    h = re.sub(r'\n\s*\n+', '\n', h)
    lines = [ln.strip() for ln in h.split('\n')]
    return '\n'.join(ln for ln in lines if ln)


def main():
    url, out = sys.argv[1], sys.argv[2]
    text = subst_text(url)
    # 去掉导航/页脚噪音：保留从第一个"第X章"或首个条文开始的正文
    m = re.search(r'第[一二三四五六七八九十百千0-9]+[章]{1}.*', text)
    start = 0
    if m:
        # 尽量从标题行开始；找不到标题就从前言起
        title = re.search(r'(中华人民共和国.{0,30}(法|条例|规定|办法))', text)
        if title and title.start() >= 0 and title.start() < (m.start() + 200):
            start = title.start()
        else:
            start = m.start()
    body = text[start:] if start else text
    open(out, 'w', encoding='utf-8').write(body)
    nhead = len(re.findall(r'^第[一二三四五六七八九十百千0-9]+条', body, flags=re.M))
    nmax = re.findall(r'第([一二三四五六七八九十百千0-9]+)条', body)
    print(f"已写出: {out}  条文数≈{nhead}  文本长度={len(body)}")


if __name__ == '__main__':
    main()