package `in`.daram.nutcracker

import kotlin.test.Test
import kotlin.test.assertEquals

/** §5.9 KT 나랏글 검증 케이스 */
class NaratgeulTest {

    private fun seq(vararg keys: Char) = processSequence(naratgeul, *keys)

    @Test fun `ㄱ_ㅏ`() {
        val (c, p) = seq('1', '3')
        assertEquals("", c); assertEquals("가", p)
    }

    @Test fun `ㄱ_ㅓ`() {
        val (c, p) = seq('1', '3', '3')
        assertEquals("", c); assertEquals("거", p)
    }

    @Test fun `ㄱ_ㅑ`() {
        // ㅏ 후 * = ㅑ
        val (c, p) = seq('1', '3', KeyInput.Special(SpecialKey.STROKE_ADD).let { '*' })
        assertEquals("", c); assertEquals("갸", p)
    }

    @Test fun `ㄱ_ㅕ`() {
        val (c, p) = seq('1', '3', '3', '*')
        assertEquals("", c); assertEquals("겨", p)
    }

    @Test fun `ㄱ_ㅛ`() {
        val (c, p) = seq('1', '6', '*')
        assertEquals("", c); assertEquals("교", p)
    }

    @Test fun `ㄱ_ㅠ`() {
        val (c, p) = seq('1', '6', '6', '*')
        assertEquals("", c); assertEquals("규", p)
    }

    @Test fun `ㅋ_ㅏ`() {
        // 1(ㄱ) + * = ㅋ
        val (c, p) = seq('1', '*', '3')
        assertEquals("", c); assertEquals("카", p)
    }

    @Test fun `ㄷ_ㅏ`() {
        val (c, p) = seq('2', '*', '3')
        assertEquals("", c); assertEquals("다", p)
    }

    @Test fun `ㅌ_ㅏ`() {
        val (c, p) = seq('2', '*', '*', '3')
        assertEquals("", c); assertEquals("타", p)
    }

    @Test fun `ㅎ_ㅏ`() {
        val (c, p) = seq('8', '*', '3')
        assertEquals("", c); assertEquals("하", p)
    }

    @Test fun `ㄲ_ㅏ`() {
        val (c, p) = seq('1', '#', '3')
        assertEquals("", c); assertEquals("까", p)
    }

    @Test fun `ㅉ_ㅏ`() {
        // ㅅ→ㅈ(*)→ㅉ(#)
        val (c, p) = seq('7', '*', '#', '3')
        assertEquals("", c); assertEquals("짜", p)
    }

    @Test fun `복합중성_ㅘ`() {
        val (c, p) = seq('1', '6', '3')
        assertEquals("", c); assertEquals("과", p)
    }

    @Test fun `ㅐ`() {
        // ㅇ + ㅏ + ㅣ = ㅐ
        val (c, p) = seq('8', '3', '9')
        assertEquals("", c); assertEquals("애", p)
    }

    @Test fun `ㅒ`() {
        val (c, p) = seq('8', '3', '*', '9')
        assertEquals("", c); assertEquals("얘", p)
    }
}
