package com.daram.nutcracker.prediction

/**
 * 플랫폼별 사전 리소스 로딩.
 * dictionary.tsv 파일을 UTF-8 문자열로 반환합니다.
 */
internal expect fun loadDictionaryText(): String
