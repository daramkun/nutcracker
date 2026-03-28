package com.daram.nutcracker

import kotlin.test.Test
import kotlin.test.assertEquals

/** §3.7 단모음 검증 케이스 */
class DanmoemTest {

    private fun seq(vararg keys: Char) = processSequence(danmoem, *keys)

    @Test fun `기본_초성중성`() {
        val (c, p) = seq('r', 'j')
        assertEquals("", c); assertEquals("가", p)
    }

    @Test fun `쌍자음_RR`() {
        val (c, p) = seq('r', 'r', 'j')
        assertEquals("", c); assertEquals("까", p)
    }

    @Test fun `쌍자음_QQ`() {
        val (c, p) = seq('q', 'q', 'j')
        assertEquals("", c); assertEquals("빠", p)
    }

    @Test fun `쌍자음_TT`() {
        val (c, p) = seq('t', 't', 'j')
        assertEquals("", c); assertEquals("싸", p)
    }

    @Test fun `이중모음_JJ`() {
        val (c, p) = seq('d', 'j', 'j')
        assertEquals("", c); assertEquals("야", p)
    }

    @Test fun `이중모음_HH`() {
        val (c, p) = seq('d', 'h', 'h')
        assertEquals("", c); assertEquals("여", p)
    }

    @Test fun `이중모음_YY`() {
        val (c, p) = seq('d', 'y', 'y')
        assertEquals("", c); assertEquals("요", p)
    }

    @Test fun `이중모음_BB`() {
        val (c, p) = seq('d', 'b', 'b')
        assertEquals("", c); assertEquals("유", p)
    }

    @Test fun `이중모음_UU`() {
        val (c, p) = seq('d', 'u', 'u')
        assertEquals("", c); assertEquals("얘", p)
    }

    @Test fun `쌍자음_이중모음_조합`() {
        val (c, p) = seq('r', 'r', 'j', 'j')
        assertEquals("", c); assertEquals("꺄", p)
    }

    @Test fun `복합중성_ㅘ`() {
        val (c, p) = seq('r', 'y', 'j')
        assertEquals("", c); assertEquals("과", p)
    }

    @Test fun `세번째R은새글자`() {
        val (c, p) = seq('r', 'r', 'r', 'j')
        assertEquals("ㄲ", c); assertEquals("가", p)
    }

    @Test fun `쌍자음없는자음두번`() {
        val (c, p) = seq('s', 's', 'j')
        assertEquals("ㄴ", c); assertEquals("나", p)
    }

    @Test fun `이중모음없는모음두번`() {
        val (c, p) = seq('k', 'k')
        assertEquals("이", c); assertEquals("이", p)
    }

    @Test fun `BS_종성제거`() {
        val (c, p) = seq('r', 'j', 's', '\u0008')
        assertEquals("", c); assertEquals("가", p)
    }

    @Test fun `BS_중성제거`() {
        val (c, p) = seq('r', 'j', '\u0008')
        assertEquals("", c); assertEquals("ㄱ", p)
    }

    @Test fun `쌍자음후_BS_중성제거`() {
        val (c, p) = seq('r', 'r', 'j', '\u0008')
        assertEquals("", c); assertEquals("ㄲ", p)
    }

    @Test fun `이중모음후_BS`() {
        // §9.4: 이중모음은 단일 자모로 FSM 진입 → BS는 통째로 삭제
        val (c, p) = seq('d', 'j', 'j', '\u0008')
        assertEquals("", c); assertEquals("ㅇ", p)
    }

    @Test fun `겹받침_BS`() {
        val (c, p) = seq('e', 'j', 'f', 'r', '\u0008')
        assertEquals("", c); assertEquals("달", p)
    }
}
