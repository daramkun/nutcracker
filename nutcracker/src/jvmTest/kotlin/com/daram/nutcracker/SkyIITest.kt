package com.daram.nutcracker

import kotlin.test.Test
import kotlin.test.assertEquals

/** §6.7 SKY-II 검증 케이스 */
class SkyIITest {

    private fun seq(vararg keys: Char) = processSequence(skyII, *keys)

    @Test fun `ㄱ_ㅏ`() {
        val (c, p) = seq('1', '3')
        assertEquals("", c); assertEquals("가", p)
    }

    @Test fun `ㅋ_ㅏ`() {
        val (c, p) = seq('1', '1', '3')
        assertEquals("", c); assertEquals("카", p)
    }

    @Test fun `ㄲ_ㅏ`() {
        val (c, p) = seq('1', '1', '1', '3')
        assertEquals("", c); assertEquals("까", p)
    }

    @Test fun `ㄷ_ㅏ`() {
        val (c, p) = seq('4', '3')
        assertEquals("", c); assertEquals("다", p)
    }

    @Test fun `ㅌ_ㅏ`() {
        val (c, p) = seq('4', '4', '3')
        assertEquals("", c); assertEquals("타", p)
    }

    @Test fun `ㄸ_ㅏ`() {
        val (c, p) = seq('4', '4', '4', '3')
        assertEquals("", c); assertEquals("따", p)
    }

    @Test fun `ㄴ_ㅏ`() {
        val (c, p) = seq('5', '3')
        assertEquals("", c); assertEquals("나", p)
    }

    @Test fun `ㄹ_ㅏ`() {
        val (c, p) = seq('5', '5', '3')
        assertEquals("", c); assertEquals("라", p)
    }

    @Test fun `ㄴ_순환`() {
        // ㄴ→ㄹ→ㄴ (5키 3회=순환, ㄴ으로 돌아와 ㅏ입력)
        val (c, p) = seq('5', '5', '5', '3')
        assertEquals("", c); assertEquals("나", p)
    }

    @Test fun `ㅇ_ㅏ`() {
        val (c, p) = seq('0', '3')
        assertEquals("", c); assertEquals("아", p)
    }

    @Test fun `ㅎ_ㅏ`() {
        val (c, p) = seq('0', '0', '3')
        assertEquals("", c); assertEquals("하", p)
    }

    @Test fun `ㅑ`() {
        // 3키 2회 = ㅑ
        val (c, p) = seq('1', '3', '3')
        assertEquals("", c); assertEquals("갸", p)
    }

    @Test fun `복합중성_ㅘ`() {
        val (c, p) = seq('0', '9', '3')
        assertEquals("", c); assertEquals("와", p)
    }

    @Test fun `BS_종성제거`() {
        val (c, p) = seq('1', '3', '5', '\u0008')
        assertEquals("", c); assertEquals("가", p)
    }

    @Test fun `BS_중성제거`() {
        val (c, p) = seq('1', '3', '\u0008')
        assertEquals("", c); assertEquals("ㄱ", p)
    }

    @Test fun `ㄲ_중성제거`() {
        val (c, p) = seq('1', '1', '1', '3', '\u0008')
        assertEquals("", c); assertEquals("ㄲ", p)
    }

    @Test fun `ㅡ_확정후_ㅏ`() {
        // 2키 2회=ㅡ 확정 후 ㅏ 시작 (ㅡ+ㅏ 는 복합중성 없음 → 별개)
        val (c, p) = seq('0', '2', '2', '3')
        assertEquals("으", c); assertEquals("아", p)
    }
}
