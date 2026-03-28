package com.daram.nutcracker

import kotlin.test.Test
import kotlin.test.assertEquals

/** §2.5 두벌식 검증 케이스 */
class DubeolsikTest {

    private fun seq(vararg keys: Char) = processSequence(dubeolsik, *keys)

    @Test fun `초성+중성`() {
        val (c, p) = seq('r', 'k')
        assertEquals("", c); assertEquals("가", p)
    }

    @Test fun `초성+중성+종성`() {
        val (c, p) = seq('r', 'k', 's')
        assertEquals("", c); assertEquals("간", p)
    }

    @Test fun `종성이_다음_초성으로`() {
        val (c, p) = seq('r', 'k', 's', 'k')
        assertEquals("가", c); assertEquals("나", p)
    }

    @Test fun `겹받침_ㄺ`() {
        val (c, p) = seq('e', 'k', 'f', 'r')
        assertEquals("", c); assertEquals("닭", p)
    }

    @Test fun `겹받침_분리후_다음글자`() {
        val (c, p) = seq('e', 'k', 'f', 'r', 'k')
        assertEquals("달", c); assertEquals("가", p)
    }

    @Test fun `복합중성_ㅘ`() {
        val (c, p) = seq('d', 'h', 'k')
        assertEquals("", c); assertEquals("와", p)
    }

    @Test fun `복합중성_ㅙ`() {
        val (c, p) = seq('d', 'h', 'o')
        assertEquals("", c); assertEquals("왜", p)
    }

    @Test fun `BS_종성제거`() {
        val (c, p) = seq('r', 'k', 's', '\u0008')
        assertEquals("", c); assertEquals("가", p)
    }

    @Test fun `BS_중성제거`() {
        val (c, p) = seq('r', 'k', '\u0008')
        assertEquals("", c); assertEquals("ㄱ", p)
    }

    @Test fun `BS_초성제거`() {
        val (c, p) = seq('r', '\u0008')
        assertEquals("", c); assertEquals("", p)
    }

    @Test fun `겹받침에서_BS`() {
        val (c, p) = seq('e', 'k', 'f', 'r', '\u0008')
        assertEquals("", c); assertEquals("달", p)
    }

    @Test fun `겹받침에서_BS_BS`() {
        val (c, p) = seq('e', 'k', 'f', 'r', '\u0008', '\u0008')
        assertEquals("", c); assertEquals("다", p)
    }

    @Test fun `쌍자음_Shift_R`() {
        val (c, p) = seq('R', 'k')
        assertEquals("", c); assertEquals("까", p)
    }

    @Test fun `쌍자음_Shift_T`() {
        val (c, p) = seq('T', 'k')
        assertEquals("", c); assertEquals("싸", p)
    }

    @Test fun `모음단독_BS`() {
        val (c, p) = seq('k', '\u0008')
        assertEquals("", c); assertEquals("", p)
    }

    @Test fun `여러글자_확정`() {
        val (c, p) = seq('g', 'k', 's', 'r', 'm', 'f')
        assertEquals("한", c); assertEquals("글", p)
    }
}
