#!/usr/bin/env python3
"""
한국어 단어 사전 생성 스크립트

기본: hermitdave/FrequencyWords (OpenSubtitles 기반, 자동 다운로드)
대안: 위키피디아 한국어 덤프 (--input 옵션으로 지정 시 사용)

실행:
    # 기본 (자동 다운로드)
    python build_ko_dict.py

    # 위키피디아 덤프 사용 (더 많은 단어, 처리 시간 수 분)
    curl -L https://dumps.wikimedia.org/kowiki/latest/kowiki-latest-pages-articles.xml.bz2 \
         -o data/kowiki.xml.bz2
    python build_ko_dict.py --input data/kowiki.xml.bz2
"""

import argparse
import bz2
import re
import sys
import urllib.request
import xml.etree.ElementTree as ET
from collections import Counter
from pathlib import Path

FREQ_WORDS_URL = (
    'https://raw.githubusercontent.com/hermitdave/FrequencyWords'
    '/master/content/2018/ko/ko_50k.txt'
)

RE_KO_WORD = re.compile(r'^[가-힣]{2,15}$')

# 위키마크업 정리
RE_TEMPLATE = re.compile(r'\{\{.*?\}\}', re.DOTALL)
RE_LINK = re.compile(r'\[\[(?:[^|\]]*\|)?([^\]]+)\]\]')
RE_HTML_TAG = re.compile(r'<[^>]+>')
RE_HEADING = re.compile(r'={2,}[^=]+=={2,}')
RE_WORD = re.compile(r'[가-힣]{2,10}')
RE_PHRASE_2 = re.compile(r'[가-힣]{2,8}\s[가-힣]{2,8}')
RE_PHRASE_3 = re.compile(r'[가-힣]{2,6}\s[가-힣]{2,6}\s[가-힣]{2,6}')

STOPWORDS = {
    '있는', '없는', '하는', '되는', '있다', '없다', '하다', '되다',
    '이다', '아니다', '같은', '이런', '그런', '저런', '어떤', '모든',
    '그리고', '그러나', '하지만', '그래서', '따라서', '또한', '즉',
    '이것', '그것', '저것', '여기', '거기', '저기',
    '때문', '경우', '이후', '이전', '현재', '당시',
}


# ── FrequencyWords 방식 ──────────────────────────────────────────────────────

def download(url: str, cache_path: Path) -> str:
    if cache_path.exists():
        print(f"캐시 사용: {cache_path}", file=sys.stderr)
        return cache_path.read_text(encoding='utf-8')

    cache_path.parent.mkdir(parents=True, exist_ok=True)
    print(f"다운로드 중: {url}", file=sys.stderr)
    try:
        req = urllib.request.Request(url, headers={'User-Agent': 'Mozilla/5.0'})
        with urllib.request.urlopen(req, timeout=60) as resp:
            text = resp.read().decode('utf-8', errors='ignore')
        cache_path.write_text(text, encoding='utf-8')
        print(f"완료 ({len(text) // 1024}KB)", file=sys.stderr)
        return text
    except Exception as e:
        print(f"다운로드 실패: {e}", file=sys.stderr)
        sys.exit(1)


def parse_freq_words(text: str, max_words: int) -> list[tuple[str, int]]:
    result = []
    seen: set[str] = set()
    for line in text.splitlines():
        parts = line.strip().split()
        if len(parts) < 2:
            continue
        word = parts[0]
        try:
            freq = int(parts[1])
        except ValueError:
            continue
        if word in STOPWORDS or word in seen:
            continue
        if RE_KO_WORD.match(word):
            seen.add(word)
            result.append((word, freq))
        if len(result) >= max_words:
            break
    return result


# ── 위키피디아 덤프 방식 ─────────────────────────────────────────────────────

def clean_wiki_text(text: str) -> str:
    text = RE_TEMPLATE.sub(' ', text)
    text = RE_LINK.sub(r'\1', text)
    text = RE_HTML_TAG.sub(' ', text)
    text = RE_HEADING.sub(' ', text)
    text = text.replace('|', ' ').replace('*', ' ').replace('#', ' ')
    return text


def extract_from_dump(input_path: Path, max_words: int, min_freq: int) -> list[tuple[str, int]]:
    word_counter: Counter = Counter()
    phrase_counter: Counter = Counter()

    print(f"덤프 처리 중: {input_path}", file=sys.stderr)
    open_fn = bz2.open if input_path.suffix == '.bz2' else open
    article_count = 0

    with open_fn(input_path, 'rt', encoding='utf-8', errors='ignore') as f:
        for event, elem in ET.iterparse(f, events=('end',)):
            tag = elem.tag.split('}')[-1] if '}' in elem.tag else elem.tag
            if tag == 'text' and elem.text:
                text = clean_wiki_text(elem.text)
                for m in RE_PHRASE_3.finditer(text):
                    p = m.group(0).strip()
                    parts = p.split()
                    if all(len(x) >= 2 for x in parts) and not any(x in STOPWORDS for x in parts):
                        phrase_counter[p] += 1
                for m in RE_PHRASE_2.finditer(text):
                    p = m.group(0).strip()
                    parts = p.split()
                    if all(len(x) >= 2 for x in parts) and not any(x in STOPWORDS for x in parts):
                        phrase_counter[p] += 1
                for m in RE_WORD.finditer(text):
                    w = m.group(0)
                    if w not in STOPWORDS:
                        word_counter[w] += 1
                article_count += 1
                if article_count % 10000 == 0:
                    print(f"  {article_count}개 문서, 단어: {len(word_counter)}, 구: {len(phrase_counter)}", file=sys.stderr)
            elem.clear()

    print(f"{article_count}개 문서 처리 완료", file=sys.stderr)

    fw = [(w, f) for w, f in word_counter.items() if f >= min_freq]
    fp = [(p, f) for p, f in phrase_counter.items() if f >= min_freq]
    fw.sort(key=lambda x: -x[1])
    fp.sort(key=lambda x: -x[1])

    wt = int(max_words * 0.7)
    result = fw[:wt] + fp[:max_words - wt]
    result.sort(key=lambda x: -x[1])
    return result


# ── 메인 ─────────────────────────────────────────────────────────────────────

def main():
    parser = argparse.ArgumentParser(description='한국어 단어 사전 생성')
    parser.add_argument('--input', default=None, help='위키피디아 덤프 경로 (.xml.bz2). 미지정 시 FrequencyWords 사용')
    parser.add_argument('--output', default='data/ko_words.tsv')
    parser.add_argument('--max-words', type=int, default=50000)
    parser.add_argument('--min-freq', type=int, default=3, help='위키피디아 덤프 사용 시 최소 빈도')
    parser.add_argument('--cache', default='data/ko_50k.txt', help='FrequencyWords 캐시 경로')
    args = parser.parse_args()

    output_path = Path(args.output)
    output_path.parent.mkdir(parents=True, exist_ok=True)

    if args.input:
        input_path = Path(args.input)
        if not input_path.exists():
            print(f"오류: {input_path} 없음", file=sys.stderr)
            sys.exit(1)
        entries = extract_from_dump(input_path, args.max_words, args.min_freq)
    else:
        text = download(FREQ_WORDS_URL, Path(args.cache))
        entries = parse_freq_words(text, args.max_words)

    with output_path.open('w', encoding='utf-8') as f:
        for word, freq in entries:
            f.write(f'{word}\t{freq}\tko\n')

    print(f"완료: {len(entries)}개 → {output_path}", file=sys.stderr)


if __name__ == '__main__':
    main()
