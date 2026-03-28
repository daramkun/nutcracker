package `in`.daram.nutcracker.prediction.mapper

import `in`.daram.nutcracker.prediction.KeyMapper

/**
 * 영어(QWERTY) 역매핑.
 * 영문자와 숫자/기호는 물리키와 동일하므로 1:1 매핑.
 */
class QwertyKeyMapper : KeyMapper {
    override val layoutName = "QWERTY"

    override fun charToKeySequences(char: Char): List<List<Char>> {
        // 영문 대문자는 Shift+소문자로 표현하지만 하이라이트 목적상 소문자 키로 반환
        val key = char.lowercaseChar()
        return if (key.isLetterOrDigit() || key in ".,!?'\"- ") listOf(listOf(key))
        else emptyList()
    }
}
