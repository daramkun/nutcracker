package com.daram.nutcracker.prediction.mapper

import com.daram.nutcracker.prediction.KeyMapper

/**
 * KT 나랏글 역매핑.
 *
 * 키 구성:
 *   1=ㄱ, 2=ㄴ, 4=ㄹ, 5=ㅁ, 7=ㅅ, 8=ㅇ
 *   3=ㅏ(1회)/ㅓ(2회), 6=ㅗ(1회)/ㅜ(2회), 9=ㅣ, 0=ㅡ
 *   * = 획추가, # = 쌍자음
 *
 * 파생 자음 (획추가 *)
 *   ㄱ→ㅋ: ['1','*']
 *   ㄴ→ㄷ: ['2','*'],  ㄷ→ㅌ: ['2','*','*']
 *   ㅁ→ㅂ: ['5','*'],  ㅂ→ㅍ: ['5','*','*']
 *   ㅅ→ㅈ: ['7','*'],  ㅈ→ㅊ: ['7','*','*']
 *   ㅇ→ㅎ: ['8','*']
 *
 * 쌍자음 (#)
 *   ㄲ: ['1','#'],  ㄸ: ['2','*','#'],  ㅃ: ['5','*','#']
 *   ㅆ: ['7','#'],  ㅉ: ['7','*','#']
 *
 * 이중모음 (획추가 *)
 *   ㅑ: ['3','*'],  ㅕ: ['3','3','*'],  ㅛ: ['6','*'],  ㅠ: ['6','6','*']
 *
 * 나랏글 복합 중성 (ㅣ 후속 입력)
 *   ㅐ: ['3','9'],  ㅔ: ['3','3','9'],  ㅒ: ['3','*','9'],  ㅖ: ['3','3','*','9']
 */
class NaratgeulKeyMapper : KeyMapper {
    override val layoutName = "KT 나랏글"

    companion object {
        private val JAMO_TO_SEQUENCES: Map<Char, List<List<Char>>> = mapOf(
            // 기본 자음
            'ㄱ' to listOf(listOf('1')),
            'ㄴ' to listOf(listOf('2')),
            'ㄹ' to listOf(listOf('4')),
            'ㅁ' to listOf(listOf('5')),
            'ㅅ' to listOf(listOf('7')),
            'ㅇ' to listOf(listOf('8')),
            // 획추가 파생 자음
            'ㅋ' to listOf(listOf('1', '*')),
            'ㄷ' to listOf(listOf('2', '*')),
            'ㅌ' to listOf(listOf('2', '*', '*')),
            'ㅂ' to listOf(listOf('5', '*')),
            'ㅍ' to listOf(listOf('5', '*', '*')),
            'ㅈ' to listOf(listOf('7', '*')),
            'ㅊ' to listOf(listOf('7', '*', '*')),
            'ㅎ' to listOf(listOf('8', '*')),
            // 쌍자음 (#)
            'ㄲ' to listOf(listOf('1', '#')),
            'ㄸ' to listOf(listOf('2', '*', '#')),
            'ㅃ' to listOf(listOf('5', '*', '#')),
            'ㅆ' to listOf(listOf('7', '#')),
            'ㅉ' to listOf(listOf('7', '*', '#')),
            // 기본 모음
            'ㅏ' to listOf(listOf('3')),
            'ㅓ' to listOf(listOf('3', '3')),
            'ㅗ' to listOf(listOf('6')),
            'ㅜ' to listOf(listOf('6', '6')),
            'ㅣ' to listOf(listOf('9')),
            'ㅡ' to listOf(listOf('0')),
            // 이중모음 (획추가)
            'ㅑ' to listOf(listOf('3', '*')),
            'ㅕ' to listOf(listOf('3', '3', '*')),
            'ㅛ' to listOf(listOf('6', '*')),
            'ㅠ' to listOf(listOf('6', '6', '*')),
            // 나랏글 복합 중성
            'ㅐ' to listOf(listOf('3', '9')),
            'ㅔ' to listOf(listOf('3', '3', '9')),
            'ㅒ' to listOf(listOf('3', '*', '9')),
            'ㅖ' to listOf(listOf('3', '3', '*', '9')),
            // 일반 복합 중성 (ㅗ/ㅜ 계열 - 표준 §1.5)
            'ㅘ' to listOf(listOf('6', '3')),
            'ㅙ' to listOf(listOf('6', '3', '9')),
            'ㅚ' to listOf(listOf('6', '9')),
            'ㅝ' to listOf(listOf('6', '6', '3', '3')),
            'ㅞ' to listOf(listOf('6', '6', '3', '3', '9')),
            'ㅟ' to listOf(listOf('6', '6', '9')),
            'ㅢ' to listOf(listOf('0', '9')),
        )
    }

    override fun charToKeySequences(char: Char): List<List<Char>> =
        JAMO_TO_SEQUENCES[char] ?: emptyList()
}
