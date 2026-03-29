#!/usr/bin/env python3
"""
영어 단어 사전 생성 스크립트

hermitdave/FrequencyWords (OpenSubtitles 기반) 에서 고빈도 영어 단어를 추출합니다.
별도 다운로드 불필요 - GitHub Raw에서 자동으로 가져옵니다.

실행:
    python build_en_dict.py [--output data/en_words.tsv] [--max-words 50000]
"""

import argparse
import re
import sys
import urllib.request
from pathlib import Path

# hermitdave/FrequencyWords - OpenSubtitles 기반, 공개 도메인
FREQ_WORDS_URL = (
    'https://raw.githubusercontent.com/hermitdave/FrequencyWords'
    '/master/content/2018/en/en_50k.txt'
)

RE_VALID = re.compile(r"^[a-z][a-z'\-]{1,28}[a-z]$")

STOPWORDS = {
    'a', 'an', 'the', 'and', 'or', 'but', 'in', 'on', 'at', 'to', 'for',
    'of', 'with', 'by', 'from', 'is', 'are', 'was', 'were', 'be', 'been',
    'being', 'have', 'has', 'had', 'do', 'does', 'did', 'will', 'would',
    'could', 'should', 'may', 'might', 'shall', 'can', 'not', 'no',
    'i', 'you', 'he', 'she', 'it', 'we', 'they', 'me', 'him', 'her', 'us',
    'them', 'my', 'your', 'his', 'its', 'our', 'their', 'this', 'that',
    'these', 'those', 'what', 'which', 'who', 'whom', 'whose',
}


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


def parse(text: str, max_words: int) -> list[tuple[str, int]]:
    # 포맷: "word count" (공백 구분, 한 줄에 하나)
    result = []
    seen: set[str] = set()
    for line in text.splitlines():
        line = line.strip()
        if not line:
            continue
        parts = line.split()
        if len(parts) < 2:
            continue
        word = parts[0].lower()
        try:
            freq = int(parts[1])
        except ValueError:
            continue
        if word in STOPWORDS or word in seen:
            continue
        if not RE_VALID.match(word):
            continue
        seen.add(word)
        result.append((word, freq))
        if len(result) >= max_words:
            break
    return result


def main():
    parser = argparse.ArgumentParser(description='FrequencyWords에서 영어 단어 사전 생성')
    parser.add_argument('--output', default='data/en_words.tsv')
    parser.add_argument('--max-words', type=int, default=50000)
    parser.add_argument('--cache', default='data/en_50k.txt')
    args = parser.parse_args()

    output_path = Path(args.output)
    output_path.parent.mkdir(parents=True, exist_ok=True)

    text = download(FREQ_WORDS_URL, Path(args.cache))
    entries = parse(text, args.max_words)

    with output_path.open('w', encoding='utf-8') as f:
        for word, freq in entries:
            f.write(f'{word}\t{freq}\ten\n')

    print(f"완료: {len(entries)}개 → {output_path}", file=sys.stderr)


if __name__ == '__main__':
    main()
