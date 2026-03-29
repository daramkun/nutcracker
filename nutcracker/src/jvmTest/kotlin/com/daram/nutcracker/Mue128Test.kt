package com.daram.nutcracker

import com.daram.nutcracker.automata.Mue128Automata
import kotlin.test.Test
import kotlin.test.assertEquals

/** §8-7 무이128 검증 케이스 */
class Mue128Test {

    private val mue128 = Mue128Automata()
    private fun seq(vararg keys: Char) = processSequence(mue128, *keys)

    // --- 기본 초성+중성 ---

    @Test fun `ㄱ_ㅏ`() {
        val (c, p) = seq('ㄱ', 'ㅏ')
        assertEquals("", c); assertEquals("가", p)
    }

    @Test fun `ㄴ_ㅏ`() {
        val (c, p) = seq('ㄴ', 'ㅏ')
        assertEquals("", c); assertEquals("나", p)
    }

    @Test fun `ㅇ_ㅏ`() {
        val (c, p) = seq('ㅇ', 'ㅏ')
        assertEquals("", c); assertEquals("아", p)
    }

    // --- 자음 멀티탭 ---

    @Test fun `ㄲ_ㄱ_2탭_ㅏ`() {
        val (c, p) = seq('ㄱ', 'ㄱ', 'ㅏ')
        assertEquals("", c); assertEquals("까", p)
    }

    @Test fun `ㄸ_ㄷ_2탭_ㅏ`() {
        val (c, p) = seq('ㄷ', 'ㄷ', 'ㅏ')
        assertEquals("", c); assertEquals("따", p)
    }

    @Test fun `ㅌ_1탭_ㅏ`() {
        val (c, p) = seq('ㅌ', 'ㅏ')
        assertEquals("", c); assertEquals("타", p)
    }

    @Test fun `ㅊ_ㅌ_2탭_ㅏ`() {
        val (c, p) = seq('ㅌ', 'ㅌ', 'ㅏ')
        assertEquals("", c); assertEquals("차", p)
    }

    @Test fun `ㅎ_1탭_ㅏ`() {
        val (c, p) = seq('ㅎ', 'ㅏ')
        assertEquals("", c); assertEquals("하", p)
    }

    @Test fun `ㅍ_ㅎ_2탭_ㅏ`() {
        val (c, p) = seq('ㅎ', 'ㅎ', 'ㅏ')
        assertEquals("", c); assertEquals("파", p)
    }

    @Test fun `ㅋ_단독키_ㅏ`() {
        val (c, p) = seq('ㅋ', 'ㅏ')
        assertEquals("", c); assertEquals("카", p)
    }

    // --- 모음 멀티탭 ---

    @Test fun `ㅑ_ㅏ_2탭`() {
        val (c, p) = seq('ㄱ', 'ㅏ', 'ㅏ')
        assertEquals("", c); assertEquals("갸", p)
    }

    @Test fun `ㅕ_ㅓ_2탭`() {
        val (c, p) = seq('ㄱ', 'ㅓ', 'ㅓ')
        assertEquals("", c); assertEquals("겨", p)
    }

    @Test fun `ㅛ_ㅗ_2탭`() {
        val (c, p) = seq('ㄱ', 'ㅗ', 'ㅗ')
        assertEquals("", c); assertEquals("교", p)
    }

    @Test fun `ㅠ_ㅜ_2탭`() {
        val (c, p) = seq('ㄱ', 'ㅜ', 'ㅜ')
        assertEquals("", c); assertEquals("규", p)
    }

    // --- 복합 모음 이어치기 ---

    @Test fun `복합중성_ㅘ_ㅗ+ㅏ`() {
        val (c, p) = seq('ㄱ', 'ㅗ', 'ㅏ')
        assertEquals("", c); assertEquals("과", p)
    }

    @Test fun `복합중성_ㅝ_ㅜ+ㅓ`() {
        val (c, p) = seq('ㄱ', 'ㅜ', 'ㅓ')
        assertEquals("", c); assertEquals("궈", p)
    }

    @Test fun `복합중성_ㅢ_ㅡ+ㅣ`() {
        val (c, p) = seq('ㄱ', 'ㅡ', 'ㅣ')
        assertEquals("", c); assertEquals("긔", p)
    }

    @Test fun `복합중성_ㅙ_ㅗ+ㅐ`() {
        val (c, p) = seq('ㄱ', 'ㅗ', 'ㅐ')
        assertEquals("", c); assertEquals("괘", p)
    }

    // --- 초성+중성+종성 ---

    @Test fun `초성중성종성_각`() {
        val (c, p) = seq('ㄱ', 'ㅏ', 'ㄱ')
        assertEquals("", c); assertEquals("각", p)
    }

    @Test fun `겹받침_ㄺ`() {
        val (c, p) = seq('ㄷ', 'ㅏ', 'ㄹ', 'ㄱ')
        assertEquals("", c); assertEquals("닭", p)
    }

    // --- Backspace ---

    @Test fun `BS_종성제거`() {
        val (c, p) = seq('ㄱ', 'ㅏ', 'ㄱ', '\u0008')
        assertEquals("", c); assertEquals("가", p)
    }

    @Test fun `BS_중성제거`() {
        val (c, p) = seq('ㄱ', 'ㅏ', '\u0008')
        assertEquals("", c); assertEquals("ㄱ", p)
    }

    @Test fun `BS_초성제거`() {
        val (c, p) = seq('ㄱ', '\u0008')
        assertEquals("", c); assertEquals("", p)
    }

    @Test fun `BS_복합중성_ㅘ_단계적_제거`() {
        // 과 → BS → 고 (ㅘ→ㅗ)
        val (c, p) = seq('ㄱ', 'ㅗ', 'ㅏ', '\u0008')
        assertEquals("", c); assertEquals("고", p)
    }

    // --- 연속 입력 ---

    @Test fun `연속입력_가나다`() {
        val (c, p) = seq('ㄱ', 'ㅏ', 'ㄴ', 'ㅏ', 'ㄷ', 'ㅏ')
        assertEquals("가나", c); assertEquals("다", p)
    }

    @Test fun `자음_단독_후_모음`() {
        // ㄱ 종성 → ㅏ 입력 시 "가" 시작
        val (c, p) = seq('ㄱ', 'ㅏ', 'ㄱ', 'ㅏ')
        assertEquals("가", c); assertEquals("가", p)
    }
}
