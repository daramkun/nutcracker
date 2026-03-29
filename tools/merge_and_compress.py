#!/usr/bin/env python3
"""
한국어·영어 TSV 병합 및 정제 스크립트

build_ko_dict.py, build_en_dict.py 결과물을 병합하여
라이브러리 리소스용 dictionary.tsv 를 생성합니다.

실행:
    python merge_and_compress.py \
        [--ko data/ko_words.tsv] [--en data/en_words.tsv] \
        [--output ../nutcracker/src/commonMain/resources/dictionary.tsv] \
        [--max-total 100000]
"""

import argparse
import re
import sys
from pathlib import Path

RE_KO_WORD = re.compile(r'^[가-힣]+([ ][가-힣]+)*$')
RE_EN_WORD = re.compile(r"^[a-zA-Z][a-zA-Z'\-]*(?:[ ][a-zA-Z][a-zA-Z'\-]*)*$")


def load_tsv(path: Path) -> list[tuple[str, int, str]]:
    entries = []
    with path.open(encoding='utf-8') as f:
        for line in f:
            line = line.rstrip('\n')
            parts = line.split('\t')
            if len(parts) != 3:
                continue
            word, freq_str, lang = parts
            try:
                freq = int(freq_str)
            except ValueError:
                continue
            if freq > 0 and word:
                entries.append((word, freq, lang))
    return entries


def validate(word: str, lang: str) -> bool:
    if lang == 'ko':
        return bool(RE_KO_WORD.match(word)) and 1 < len(word) <= 20
    elif lang == 'en':
        return bool(RE_EN_WORD.match(word)) and 1 < len(word) <= 30
    return False


def normalize_freq(entries: list[tuple[str, int, str]]) -> list[tuple[str, int, str]]:
    """언어별로 빈도를 정규화하여 한국어/영어 간 스케일 차이를 줄임"""
    ko = [(w, f, l) for w, f, l in entries if l == 'ko']
    en = [(w, f, l) for w, f, l in entries if l == 'en']

    def normalize(group):
        if not group:
            return group
        max_f = max(f for _, f, _ in group)
        return [(w, int(f / max_f * 100000), l) for w, f, l in group]

    return normalize(ko) + normalize(en)


def main():
    parser = argparse.ArgumentParser(description='한국어·영어 TSV 병합 및 정제')
    parser.add_argument('--ko', default='data/ko_words.tsv', help='한국어 TSV 경로')
    parser.add_argument('--en', default='data/en_words.tsv', help='영어 TSV 경로')
    parser.add_argument('--output',
                        default='../nutcracker/src/commonMain/resources/dictionary.tsv',
                        help='출력 TSV 경로')
    parser.add_argument('--max-total', type=int, default=100000, help='최대 전체 항목 수')
    args = parser.parse_args()

    ko_path = Path(args.ko)
    en_path = Path(args.en)
    output_path = Path(args.output)

    all_entries: list[tuple[str, int, str]] = []

    if ko_path.exists():
        ko_entries = load_tsv(ko_path)
        print(f"한국어 로드: {len(ko_entries)}개", file=sys.stderr)
        all_entries.extend(ko_entries)
    else:
        print(f"경고: 한국어 파일 없음 ({ko_path})", file=sys.stderr)

    if en_path.exists():
        en_entries = load_tsv(en_path)
        print(f"영어 로드: {len(en_entries)}개", file=sys.stderr)
        all_entries.extend(en_entries)
    else:
        print(f"경고: 영어 파일 없음 ({en_path})", file=sys.stderr)

    if not all_entries:
        print("오류: 처리할 데이터가 없습니다.", file=sys.stderr)
        sys.exit(1)

    # 유효성 검사
    valid = [(w, f, l) for w, f, l in all_entries if validate(w, l)]
    print(f"유효 항목: {len(valid)}개 (제거: {len(all_entries) - len(valid)}개)", file=sys.stderr)

    # 중복 제거 (동일 단어+언어)
    seen: set[tuple[str, str]] = set()
    deduped = []
    for w, f, l in valid:
        key = (w.lower() if l == 'en' else w, l)
        if key not in seen:
            seen.add(key)
            deduped.append((w, f, l))
    print(f"중복 제거 후: {len(deduped)}개", file=sys.stderr)

    # 언어별 빈도 정규화
    normalized = normalize_freq(deduped)

    # 빈도 내림차순 정렬 후 상위 max_total개
    normalized.sort(key=lambda x: -x[1])
    final = normalized[:args.max_total]

    ko_count = sum(1 for _, _, l in final if l == 'ko')
    en_count = sum(1 for _, _, l in final if l == 'en')
    print(f"최종: {len(final)}개 (한국어: {ko_count}, 영어: {en_count})", file=sys.stderr)

    output_path.parent.mkdir(parents=True, exist_ok=True)
    with output_path.open('w', encoding='utf-8') as f:
        for word, freq, lang in final:
            f.write(f'{word}\t{freq}\t{lang}\n')

    size_kb = output_path.stat().st_size // 1024
    print(f"완료: {output_path} ({size_kb}KB)", file=sys.stderr)


if __name__ == '__main__':
    main()
